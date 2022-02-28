package function.cohort.trio;

import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.Sample;
import static function.cohort.trio.TrioManager.COMP_HET_FLAG;
import function.variant.base.Output;
import global.Index;
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

/**
 *
 * @author nick
 */
public class ListTrio extends AnalysisBase4CalledVar {

    BufferedWriter bwTrioGenotype = null;
    final String trioGenotypeFilePath = CommonCommand.outputPath + "trio_genotypes.csv";

    BufferedWriter bwTrioGenotypeNoFlag = null;
    final String trioGenotypeFilePathNoFlag = CommonCommand.outputPath + "trio_genotypes_noflag.csv";

    HashMap<String, List<TrioOutput>> geneVariantListMap = new HashMap<>();
    // avoid output duplicate carrier (comp var & single var)
    HashSet<String> outputCarrierSet = new HashSet<>();

    @Override
    public void initOutput() {
        try {
            bwTrioGenotype = new BufferedWriter(new FileWriter(trioGenotypeFilePath));
            bwTrioGenotype.write(TrioManager.getHeader());
            bwTrioGenotype.newLine();

            bwTrioGenotypeNoFlag = new BufferedWriter(new FileWriter(trioGenotypeFilePathNoFlag));
            bwTrioGenotypeNoFlag.write(TrioManager.getHeader());
            bwTrioGenotypeNoFlag.newLine();
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
            bwTrioGenotype.flush();
            bwTrioGenotype.close();

            bwTrioGenotypeNoFlag.flush();
            bwTrioGenotypeNoFlag.close();
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
            for (int i = 0; i < geneOutputList.size(); i++) {
                TrioOutput output1 = geneOutputList.get(i);

                for (Trio trio : TrioManager.getList()) {
                    output1.initTrioData(trio);

                    if (output1.isQualifiedGeno(output1.cGeno)
                            || output1.isQualifiedGeno(output1.fGeno)
                            || output1.isQualifiedGeno(output1.mGeno)) {
                        for (int j = i + 1; j < geneOutputList.size(); j++) {
                            TrioOutput output2 = geneOutputList.get(j);
                            output2.initTrioData(trio);

                            if (output1.isQualifiedGeno(output1.cGeno)
                                    && output2.isQualifiedGeno(output2.cGeno)) {
                                String compHetFlag = getTrioCompHetFlag(output1, output2);

                                if (compHetFlag.equals(COMP_HET_FLAG[0])) { // only COMPOUND HETEROZYGOTE
                                    doCompHetOutput(output1, output2, "child");
                                }
                            }

                            // father's v1 and v2 as HET, mother's v1 and v2 is REF
                            // child's v1 and v2 either (het and ref) or (ref and het)
                            if (output1.fGeno == Index.HET && output2.fGeno == Index.HET
                                    && output1.mGeno == Index.REF && output2.mGeno == Index.REF
                                    && ((output1.cGeno == Index.HET && output2.cGeno == Index.REF)
                                    || (output2.cGeno == Index.HET && output1.cGeno == Index.REF))) {
                                doCompHetOutput(output1, output2, "father");
                            }
                            
                            // mother's v1 and v2 as HET, father's v1 and v2 is REF
                            // child's v1 and v2 either (het and ref) or (ref and het)
                            if (output1.mGeno == Index.HET && output2.mGeno == Index.HET
                                    && output1.fGeno == Index.REF && output2.fGeno == Index.REF
                                    && ((output1.cGeno == Index.HET && output2.cGeno == Index.REF)
                                    || (output2.cGeno == Index.HET && output1.cGeno == Index.REF))) {
                                doCompHetOutput(output1, output2, "mother");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private String getTrioCompHetFlag(TrioOutput output1, TrioOutput output2) {
        String flag = TrioManager.getCompHetFlag(
                output1.cGeno, output1.mGeno, output1.fGeno,
                output2.cGeno, output2.mGeno, output2.fGeno);

        return flag;
    }

    private void doCompHetOutput(TrioOutput output1, TrioOutput output2, String member) throws Exception {
        StringBuilder compHetVarSB = new StringBuilder();
        compHetVarSB.append(output1.getCalledVariant().getVariantIdStr());
        compHetVarSB.append("&");
        compHetVarSB.append(output2.getCalledVariant().getVariantIdStr());

        String compHetVar1 = compHetVarSB.toString() + "#1";
        String compHetVar2 = compHetVarSB.toString() + "#2";

        doCompHetOutput(output1, compHetVar1, member);
        doCompHetOutput(output2, compHetVar2, member);
    }

    private void doCompHetOutput(TrioOutput output, String compHetVar, String member) throws Exception {
        Sample sample = output.child;
        if (member.equals("father")) {
            sample = output.father;
        } else if (member.equals("mother")) {
            sample = output.mother;
        }

        StringJoiner sj = new StringJoiner(",");
        sj.add(sample.getFamilyId());
        sj.add(sample.getName());
        sj.add(member);
        sj.add(sample.getAncestry());
        sj.add(sample.getBroadPhenotype());
        sj.add(compHetVar);
        sj.add(output.toString());

        bwTrioGenotype.write(sj.toString());
        bwTrioGenotype.newLine();
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio function...";
    }
}
