package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import static function.cohort.trio.TrioManager.COMP_HET_FLAG;
import function.variant.base.Output;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ListTrio extends AnalysisBase4CalledVar {

    BufferedWriter bwTrioGeno = null;
    final String trioGenoFilePath = CommonCommand.outputPath + "trio_genotypes.csv";

    HashMap<String, List<TrioOutput>> geneVariantListMap = new HashMap<>();
    // avoid output duplicate carrier (comp var & single var)
    HashSet<String> outputCarrierSet = new HashSet<>();

    @Override
    public void initOutput() {
        try {
            bwTrioGeno = new BufferedWriter(new FileWriter(trioGenoFilePath));
            bwTrioGeno.write(TrioManager.getHeader4Denovo());
            bwTrioGeno.newLine();
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
            bwTrioGeno.flush();
            bwTrioGeno.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        Output.logTierVariantCount();
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
            outputCarrierSet.clear();

            for (int i = 0; i < geneOutputList.size(); i++) {
                TrioOutput output1 = geneOutputList.get(i);

                for (Trio trio : TrioManager.getList()) {
                    output1.initTrioData(trio);

                    if (output1.isQualifiedGeno(output1.cGeno)) {
                        output1.initDenovoFlag(trio.getChild());
                        output1.initTierFlag4SingleVar();

                        for (int j = i + 1; j < geneOutputList.size(); j++) {
                            TrioOutput output2 = geneOutputList.get(j);
                            output2.initTrioData(trio);

                            if (output2.isQualifiedGeno(output2.cGeno)) {
                                // init variant denovo flag for finding potential comp het
                                output2.initDenovoFlag(trio.getChild());
                                output2.initTierFlag4SingleVar();

                                outputCompHet(output1, output2);
                            }
                        }

                        outputDenovoOrHomOrInheritedVar(output1);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputDenovoOrHomOrInheritedVar(TrioOutput output) throws Exception {
        if (TrioCommand.isExcludeNoFlag
                && output.getTierFlag4SingleVar() == Data.BYTE_NA
                && !output.isFlag()) {
            return;
        }
        
        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());

        if (outputCarrierSet.contains(carrierIDSB.toString())) {
            return;
        }

        output.countSingleVar();
        
        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getName());
        sj.add(output.motherName);
        sj.add(output.fatherName);
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());

        bwTrioGeno.write(sj.toString());
        bwTrioGeno.newLine();
    }

    private void outputCompHet(TrioOutput output1, TrioOutput output2) throws Exception {
        String compHetFlag = getTrioCompHetFlag(output1, output2);

        if (!compHetFlag.equals(COMP_HET_FLAG[2])) { // no flag
            doCompHetOutput(compHetFlag, output1, output2);
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

    private void doCompHetOutput(String compHetFlag, TrioOutput output1, TrioOutput output2) throws Exception {
        // apply tier rules
        byte tierFlag4CompVar = Data.BYTE_NA;

        // Restrict to High or Moderate impact or TraP >= 0.4 variants
        if (output1.getCalledVariant().isImpactHighOrModerate()
                && output2.getCalledVariant().isImpactHighOrModerate()) {
            // tier 1
            if (output1.isParentsNotHom() && output2.isParentsNotHom()
                    // for both variants, genotype is not observed in Hemizygous or Homozygous from IGM default controls and gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isNotObservedInHomAmongControl() && output2.getCalledVariant().isNotObservedInHomAmongControl()
                    // for both variants, max 0.5% AF to IGM default controls and gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isControlAFValid() && output2.getCalledVariant().isControlAFValid()) {
                tierFlag4CompVar = 1;
                Output.tier1CompoundVarCount++;
            } else if ( // tier 2
                    // if one of the variant meets tier 2 inclusion criteria
                    (output1.getCalledVariant().isMetTier2InclusionCriteria() || output2.getCalledVariant().isMetTier2InclusionCriteria())
                    // for both variants, less than 10 homozygous observed from IGM default controls + gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isNHomFromControlsValid(10) && output2.getCalledVariant().isNHomFromControlsValid(10)) {
                tierFlag4CompVar = 2;
                Output.tier2CompoundVarCount++;
            }
        }

        StringBuilder compHetVarSB = new StringBuilder();
        compHetVarSB.append(output1.getCalledVariant().getVariantIdStr());
        compHetVarSB.append("&");
        compHetVarSB.append(output2.getCalledVariant().getVariantIdStr());
        
        String compHetVar1 = compHetVarSB.toString() + "#1";
        String compHetVar2 = compHetVarSB.toString() + "#2";

        // output as single var if compound var not tier 1 or 2 when --exclude-no-flag used 
        if (TrioCommand.isExcludeNoFlag
                && tierFlag4CompVar == Data.BYTE_NA) {
            compHetFlag = Data.STRING_NA;
            compHetVar1 = Data.STRING_NA;
            compHetVar2 = Data.STRING_NA;
        }
        
        doCompHetOutput(tierFlag4CompVar, compHetFlag, output1, compHetVar1);
        doCompHetOutput(tierFlag4CompVar, compHetFlag, output2, compHetVar2);
    }

    private void doCompHetOutput(byte tierFlag4CompVar, String compHetFlag, TrioOutput output,
            String compHetVar) throws Exception {
        if (TrioCommand.isExcludeNoFlag
                && tierFlag4CompVar == Data.BYTE_NA
                && output.getTierFlag4SingleVar() == Data.BYTE_NA
                && !output.isFlag()) {
            return;
        }
        
        output.countSingleVar();
        
        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());
        outputCarrierSet.add(carrierIDSB.toString());

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getName());
        sj.add(output.motherName);
        sj.add(output.fatherName);
        sj.add(compHetFlag);
        sj.add(compHetVar);
        sj.add(FormatManager.getByte(tierFlag4CompVar));
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());

        bwTrioGeno.write(sj.toString());
        bwTrioGeno.newLine();
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio function...";
    }
}
