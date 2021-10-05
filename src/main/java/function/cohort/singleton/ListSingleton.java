package function.cohort.singleton;

import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import static function.cohort.singleton.SingletonManager.COMP_HET_FLAG;
import function.variant.base.Output;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListSingleton extends AnalysisBase4CalledVar {

    BufferedWriter bwSingletonGeno = null;
    final String genotypesFilePath = CommonCommand.outputPath + "singleton_genotypes.csv";

    BufferedWriter bwSingletonGenoNoFlag = null;
    final String genotypesFilePathNoFlag = CommonCommand.outputPath + "singleton_genotypes_noflag.csv";

    HashMap<String, List<SingletonOutput>> geneVariantListMap = new HashMap<>();
    // avoid output duplicate carrier (comp var & single var)
    HashSet<String> outputCarrierSet = new HashSet<>();

    @Override
    public void initOutput() {
        try {
            bwSingletonGeno = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwSingletonGeno.write(SingletonOutput.getHeader());
            bwSingletonGeno.newLine();

            bwSingletonGenoNoFlag = new BufferedWriter(new FileWriter(genotypesFilePathNoFlag));
            bwSingletonGenoNoFlag.write(SingletonOutput.getHeader());
            bwSingletonGenoNoFlag.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
        outputSingleVarAndCompVar();

        clearList();
    }

    @Override
    public void closeOutput() {
        try {
            bwSingletonGeno.flush();
            bwSingletonGeno.close();

            bwSingletonGenoNoFlag.flush();
            bwSingletonGenoNoFlag.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (SingletonCommand.isMannWhitneyTest) {
            ThirdPartyToolManager.runMannWhitneyTest(genotypesFilePath);
        }

        if (CommonCommand.gzip) {
            ThirdPartyToolManager.gzipFile(genotypesFilePath);
        }

        Output.logTierVariantCount();
    }

    @Override
    public void beforeProcessDatabaseData() {
        SingletonManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            SingletonOutput output = new SingletonOutput(calledVar);

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addVariantToGeneList(SingletonOutput output) {
        List<SingletonOutput> geneOutputList
                = geneVariantListMap.get(output.getCalledVariant().getGeneName());

        if (geneOutputList == null) {
            geneOutputList = new ArrayList<>();
            geneOutputList.add(output);
            geneVariantListMap.put(output.getCalledVariant().getGeneName(), geneOutputList);
        } else {
            geneOutputList.add(output);
        }
    }

    private void outputSingleVarAndCompVar() {
        if (geneVariantListMap.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, List<SingletonOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                doOutput(entry.getValue());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<SingletonOutput> geneOutputList) {
        try {
            outputCarrierSet.clear();

            for (int i = 0; i < geneOutputList.size(); i++) {
                SingletonOutput output1 = geneOutputList.get(i);

                for (Singleton singleton : SingletonManager.getList()) {
                    output1.initSingletonData(singleton);

                    if (output1.isQualifiedGeno(output1.cGeno)) {
                        output1.initTierFlag4SingleVar();

                        for (int j = i + 1; j < geneOutputList.size(); j++) {
                            SingletonOutput output2 = geneOutputList.get(j);
                            output2.initSingletonData(singleton);

                            if (output2.isQualifiedGeno(output2.cGeno)) {
                                output2.initTierFlag4SingleVar();

                                outputCompHet(output1, output2);
                            }
                        }

                        outputSingleVar(output1);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSingleVar(SingletonOutput output) throws Exception {
        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());

        if (outputCarrierSet.contains(carrierIDSB.toString())) {
            return;
        }

        output.countSingleVar();

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getFamilyId());
        sj.add(output.child.getName());
        sj.add(output.child.getAncestry());
        sj.add(output.child.getBroadPhenotype());
        sj.add("'" + output.getCalledVariant().getGeneName() + "'");
        sj.add(output.getCalledVariant().getGeneLink());
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());
        sj.add(FormatManager.appendDoubleQuote(output.getSummary()));

        if (output.getTierFlag4SingleVar() != Data.BYTE_NA
                || output.isFlag()) {
            bwSingletonGeno.write(sj.toString());
            bwSingletonGeno.newLine();
        } else {
            bwSingletonGenoNoFlag.write(sj.toString());
            bwSingletonGenoNoFlag.newLine();
        }
    }

    private void outputCompHet(SingletonOutput output1, SingletonOutput output2) throws Exception {
        String compHetFlag = SingletonManager.getCompHetFlag(output1.cGeno, output2.cGeno);

        if (!compHetFlag.equals(COMP_HET_FLAG[2])) { // no flag
            doCompHetOutput(output1, output2);
        }
    }

    private void doCompHetOutput(SingletonOutput output1, SingletonOutput output2) throws Exception {
        float[] coFreq = SingletonManager.getCoOccurrenceFreq(output1, output2);

        StringBuilder compHetVarSB = new StringBuilder();
        compHetVarSB.append(output1.getCalledVariant().getVariantIdStr());
        compHetVarSB.append("&");
        compHetVarSB.append(output2.getCalledVariant().getVariantIdStr());

        String compHetVar1 = compHetVarSB.toString() + "#1";
        String compHetVar2 = compHetVarSB.toString() + "#2";

        // apply tier rules
        byte tierFlag4CompVar = Data.BYTE_NA;

        // tier 1
        if (coFreq[Index.CTRL] == 0
                // for both variants, genotype is not observed in Hemizygous or Homozygous from IGM default controls and gnomAD (WES & WGS) controls
                && output1.getCalledVariant().isNotObservedInHomAmongControl() && output2.getCalledVariant().isNotObservedInHomAmongControl()
                // for both variants, max 0.5% AF to IGM default controls and gnomAD (WES & WGS) controls
                && output1.getCalledVariant().isControlAFValid() && output2.getCalledVariant().isControlAFValid()) {
            tierFlag4CompVar = 1;
            Output.tier1CompoundVarCount++;
        } else if ( // tier 2
                // if one of the variant meets tier 2 inclusion criteria
                (output1.getCalledVariant().isMetTier2InclusionCriteria(output1.cCarrier)
                || output2.getCalledVariant().isMetTier2InclusionCriteria(output2.cCarrier))
                // for both variants, less than 10 homozygous observed from IGM default controls + gnomAD (WES & WGS) controls
                && output1.getCalledVariant().isNHomFromControlsValid(10) && output2.getCalledVariant().isNHomFromControlsValid(10)) {
            tierFlag4CompVar = 2;
            Output.tier2CompoundVarCount++;
        } else {
            // output as single var if compound var not tier 1 or 2
            compHetVar1 = Data.STRING_NA;
            compHetVar2 = Data.STRING_NA;
            coFreq[Index.CTRL] = Data.FLOAT_NA;
        }

        doCompHetOutput(tierFlag4CompVar, output1, coFreq, compHetVar1);
        doCompHetOutput(tierFlag4CompVar, output2, coFreq, compHetVar2);
    }

    private void doCompHetOutput(byte tierFlag4CompVar, SingletonOutput output, float[] coFreq, String compHetVar) throws Exception {
        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());

        // if output as single var then ignore duplicate output
        if (compHetVar.equals(Data.STRING_NA) && outputCarrierSet.contains(carrierIDSB.toString())) {
            return;
        }

        output.countSingleVar();
        outputCarrierSet.add(carrierIDSB.toString());

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getFamilyId());
        sj.add(output.child.getName());
        sj.add(output.child.getAncestry());
        sj.add(output.child.getBroadPhenotype());
        sj.add("'" + output.getCalledVariant().getGeneName() + "'");
        sj.add(output.getCalledVariant().getGeneLink());
        sj.add(compHetVar);
        sj.add(FormatManager.getFloat(coFreq[Index.CTRL]));
        sj.add(FormatManager.getByte(tierFlag4CompVar));
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());
        sj.add(FormatManager.appendDoubleQuote(output.getSummary()));

        if (tierFlag4CompVar != Data.BYTE_NA
                || output.getTierFlag4SingleVar() != Data.BYTE_NA
                || output.isFlag()) {
            bwSingletonGeno.write(sj.toString());
            bwSingletonGeno.newLine();
        } else {
            bwSingletonGenoNoFlag.write(sj.toString());
            bwSingletonGenoNoFlag.newLine();
        }
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "Start running list singleton function";
    }
}
