package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import static function.cohort.trio.TrioManager.COMP_HET_FLAG;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ListTrio extends AnalysisBase4CalledVar {

    BufferedWriter bwDenovo = null;
    final String denovoFilePath = CommonCommand.outputPath + "denovoandhom.csv";

    BufferedWriter bwCompHet = null;
    final String compHetFilePath = CommonCommand.outputPath + "comphet.csv";

    HashMap<String, List<TrioOutput>> geneVariantListMap = new HashMap<>();

    @Override
    public void initOutput() {
        try {
            bwDenovo = new BufferedWriter(new FileWriter(denovoFilePath));
            bwDenovo.write(TrioManager.getHeader4Denovo());
            bwDenovo.newLine();

            bwCompHet = new BufferedWriter(new FileWriter(compHetFilePath));
            bwCompHet.write(TrioManager.getHeader4CompHet());
            bwCompHet.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
        outputDenovoAndCompHets();

        clearList();
    }

    @Override
    public void closeOutput() {
        try {
            bwDenovo.flush();
            bwDenovo.close();

            bwCompHet.flush();
            bwCompHet.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        TrioManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            TrioOutput output = new TrioOutput(calledVar);

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addVariantToGeneList(TrioOutput output) {
        List<TrioOutput> geneOutputList
                = geneVariantListMap.get(output.getCalledVariant().getGeneName());

        if (geneOutputList == null) {
            geneOutputList = new ArrayList<>();
            geneOutputList.add(output);
            geneVariantListMap.put(output.getCalledVariant().getGeneName(), geneOutputList);
        } else {
            geneOutputList.add(output);
        }
    }

    private void outputDenovoAndCompHets() {
        if (geneVariantListMap.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, List<TrioOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                doOutput(entry.getValue());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<TrioOutput> geneOutputList) {
        try {
            for (int i = 0; i < geneOutputList.size(); i++) {
                TrioOutput output1 = geneOutputList.get(i);

                for (Trio trio : TrioManager.getList()) {
                    output1.initTrioData(trio);

                    if (output1.isQualifiedGeno(output1.cGeno)) {
                        output1.initDenovoFlag(trio.getChild());
                        outputDenovoOrHom(output1);

                        for (int j = i + 1; j < geneOutputList.size(); j++) {
                            TrioOutput output2 = geneOutputList.get(j);
                            output2.initTrioData(trio);

                            if (output2.isQualifiedGeno(output2.cGeno)) {
                                // init variant denovo flag for finding potential comp het
                                output2.initDenovoFlag(trio.getChild());

                                outputCompHet(output1, output2);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputDenovoOrHom(TrioOutput output) throws Exception {
        byte tierFlag = Data.BYTE_NA;

        // denovo or hom
        if (!output.denovoFlag.equals("NO FLAG") && !output.denovoFlag.equals(Data.STRING_NA)) {
            if (output.isDenovoTier1()
                    || output.isHomozygousTier1()
                    || output.isHemizygousTier1()) {
                tierFlag = 1;
            } else if (output.getCalledVariant().isMetTier2InclusionCriteria()
                    && (output.isDenovoTier2()
                    || output.isHomozygousTier2()
                    || output.isHemizygousTier2())) {
                tierFlag = 2;
            }
        } else { // child variant
            tierFlag = output.getCalledVariant().isMetTier2InclusionCriteria() ? 2 : Data.BYTE_NA;
        }

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getFamilyId());
        sj.add(output.motherName);
        sj.add(output.fatherName);
        sj.add(FormatManager.getByte(tierFlag));
        sj.add(output.toString());
        bwDenovo.write(sj.toString());
        bwDenovo.newLine();
    }

    private void outputCompHet(TrioOutput output1, TrioOutput output2) throws Exception {
        String compHetFlag = getTrioCompHetFlag(output1, output2);

        if (!compHetFlag.equals(COMP_HET_FLAG[2])) { // no flag
            doCompHetOutput(bwCompHet, compHetFlag, output1, output2);
        }
    }

    private String getTrioCompHetFlag(TrioOutput output1, TrioOutput output2) {
        String flag = TrioManager.getCompHetFlag(
                output1.cGeno, output1.mGeno, output1.fGeno,
                output2.cGeno, output2.mGeno, output2.fGeno);

        flag = TrioManager.getCompHetFlagByDenovo(flag,
                output1.cGeno, output1.mGeno, output1.fGeno, output1.denovoFlag,
                output2.cGeno, output2.mGeno, output2.fGeno, output2.denovoFlag);

        return flag;
    }

    private void doCompHetOutput(BufferedWriter bw, String flag, TrioOutput output1, TrioOutput output2) throws Exception {
        float[] coFreq = TrioManager.getCoOccurrenceFreq(output1, output2);

        // apply tier rules
        byte tierFlag = Data.BYTE_NA;

        // tier 1
        if ( // neither parent is hom alt
                output1.isParentsNotHom() && output2.isParentsNotHom()
                // co-occurance freq in controls is 0
                && coFreq[1] == 0
                // for both variants, genotype is not observed in Hemizygous or Homozygous from IGM default controls and gnomAD (WES & WGS) controls
                && output1.isNotObservedInHomAmongControl() && output2.isNotObservedInHomAmongControl()
                // for both variants, max 0.5% AF to IGM default controls and gnomAD (WES & WGS) controls
                && output1.isControlAFValid() && output2.isControlAFValid()) {
            tierFlag = 1;
        } else if (// if one of the variant meets tier 2 inclusion criteria
                (output1.getCalledVariant().isMetTier2InclusionCriteria() || output2.getCalledVariant().isMetTier2InclusionCriteria())
                // for both variants, less than 10 homozygous observed from IGM default controls + gnomAD (WES & WGS) controls
                && output1.isNHomFromControlsValid(10) && output2.isNHomFromControlsValid(10)) {
            tierFlag = 2;
        }

        StringJoiner sj = new StringJoiner(",");
        sj.add(output1.child.getFamilyId());
        sj.add(output1.motherName);
        sj.add(output1.fatherName);
        sj.add(flag);
        sj.add(FormatManager.getFloat(coFreq[Index.CASE]));
        sj.add(FormatManager.getFloat(coFreq[Index.CTRL]));
        sj.add(FormatManager.getByte(tierFlag));
        sj.add(output1.toString());
        sj.add(output2.toString());

        bw.write(sj.toString());
        bw.newLine();
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio function...";
    }
}
