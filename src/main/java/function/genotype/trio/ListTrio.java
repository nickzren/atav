package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import static function.genotype.trio.TrioManager.COMP_HET_FLAG;
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
import utils.FormatManager;
import utils.ThirdPartyToolManager;

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
            bwDenovo.write(TrioManager.getTitle4Denovo());
            bwDenovo.newLine();

            bwCompHet = new BufferedWriter(new FileWriter(compHetFilePath));
            bwCompHet.write(TrioManager.getTitle4CompHet());
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
        if (TrioCommand.isRunTier) {
            ThirdPartyToolManager.runTrioDenovoTier(denovoFilePath);

            ThirdPartyToolManager.runTrioCompHetTier(compHetFilePath);
        }
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
                        outputDenovo(output1);

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

    private void outputDenovo(TrioOutput output) throws Exception {
        if (!output.denovoFlag.equals("NO FLAG") && !output.denovoFlag.equals(Data.STRING_NA)) {
            StringBuilder sb = new StringBuilder();
            sb.append(output.child.getFamilyId()).append(",");
            sb.append(output.motherName).append(",");
            sb.append(output.fatherName).append(",");
            sb.append(output.toString());
            bwDenovo.write(sb.toString());
            bwDenovo.newLine();
        }
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

        StringBuilder sb = new StringBuilder();
        sb.append(output1.child.getFamilyId()).append(",");
        sb.append(output1.motherName).append(",");
        sb.append(output1.fatherName).append(",");
        sb.append(flag).append(",");
        sb.append(FormatManager.getFloat(coFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(coFreq[Index.CTRL])).append(",");

        sb.append(output1.toString());
        sb.append(output2.toString());

        bw.write(sb.toString());
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
