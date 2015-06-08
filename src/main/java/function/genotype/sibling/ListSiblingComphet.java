package function.genotype.sibling;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class ListSiblingComphet extends AnalysisBase4CalledVar {

    final String comphetFilePath = CommandValue.outputPath + "comphet.csv";
    BufferedWriter bwCompHet = null;
    ArrayList<CompHetOutput> outputList = new ArrayList<CompHetOutput>();
    ArrayList<ArrayList<CompHetOutput>> geneListVector = new ArrayList<ArrayList<CompHetOutput>>();
    HashSet<String> currentGeneList = new HashSet<String>();

    String[] FLAG = {
        "Shared", // 0
        "Possibly shared", // 1
        "Not shared", //2
        "No flag" // 3
    };

    @Override
    public void initOutput() {
        try {
            bwCompHet = new BufferedWriter(new FileWriter(comphetFilePath));
            bwCompHet.write(CompHetOutput.title);
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
        listCompHets();

        clearList();
    }

    @Override
    public void closeOutput() {
        try {
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
        SiblingManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CompHetOutput output = new CompHetOutput(calledVar);

            output.countSampleGenoCov();

            output.calculate();

            if (output.isValid()) {

                for (String geneName : calledVar.getGeneSet()) {
                    if (!geneName.equals("NA")) {
                        output.geneName = geneName;
                        outputList.add((CompHetOutput) output.clone());
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void listCompHets() {
        if (outputList.size() > 0) {
            Collections.sort(outputList);
        } else {
            return;
        }

        initGeneVariantList();

        try {
            for (ArrayList<CompHetOutput> list : geneListVector) {
                String geneName = list.get(0).geneName;

                LogManager.writeAndPrint("Analyzing qualified variants in gene ("
                        + geneName + ")");

                doOutput(list);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initGeneVariantList() {
        ArrayList<CompHetOutput> geneOutputList = null;

        for (CompHetOutput output : outputList) {
            if (!currentGeneList.contains(output.geneName)) {
                currentGeneList.add(output.geneName);

                geneOutputList = new ArrayList<CompHetOutput>();
                geneOutputList.add(output);
                geneListVector.add(geneOutputList);
            } else {
                geneOutputList.add(output);
            }
        }
    }

    private void doOutput(ArrayList<CompHetOutput> geneOutputList) {
        StringBuilder sb = new StringBuilder();

        int outputSize = geneOutputList.size();
        int childSize;

        CompHetOutput output1, output2;
        Sample child1, child2, father, mother;

        for (int i = 0; i < outputSize - 1; i++) {
            output1 = geneOutputList.get(i);

            for (int j = i + 1; j < outputSize; j++) {
                output2 = geneOutputList.get(j);

                for (Sibling sibling : SiblingManager.getList()) {
                    mother = sibling.getMother();
                    father = sibling.getFather();

                    if (!checkParents(output1, output2, mother, father)) {
                        continue;
                    }

                    childSize = sibling.getChildList().size();

                    for (int x = 0; x < childSize - 1; x++) {
                        child1 = sibling.getChildList().get(x);

                        for (int y = x + 1; y < childSize; y++) {
                            child2 = sibling.getChildList().get(y);

                            if (child1.equals(child2)) {
                                continue;
                            }

                            String flag = getFlag(output1, output2, child1, child2);

                            doOutput(sb, output1, output2, child1, child2, flag);
                        }
                    }
                }
            }
        }
    }

    private boolean checkParents(CompHetOutput output1, CompHetOutput output2,
            Sample mother, Sample father) {
        if (output1.getCalledVariant().getGenotype(mother.getIndex()) != Data.NA
                && output2.getCalledVariant().getGenotype(father.getIndex()) != Data.NA) {
            return true;
        } else if (output1.getCalledVariant().getGenotype(father.getIndex()) != Data.NA
                && output2.getCalledVariant().getGenotype(mother.getIndex()) != Data.NA) {
            return true;
        } else {
            return false;
        }
    }

    /*
     Shared: if both cases are het.
     Possibly shared: if one case is het and the other is missing
     Not shared: if only one case is het, and the other is not het and not missing
     */
    private String getFlag(CompHetOutput output1, CompHetOutput output2,
            Sample child1, Sample child2) {
        int geno1 = output1.getCalledVariant().getGenotype(child1.getIndex());
        int geno2 = output2.getCalledVariant().getGenotype(child2.getIndex());

        if (geno1 == Index.HET) {
            if (geno2 == Index.HET) {
                return FLAG[0];
            } else if (geno2 == Data.NA) {
                return FLAG[1];
            } else if (output2.isQualifiedGeno(geno2)) {
                return FLAG[2];

            }
        }

        return FLAG[3];
    }

    private void doOutput(StringBuilder sb, CompHetOutput output1,
            CompHetOutput output2, Sample child1, Sample child2, String flag) {
        try {
            if (!flag.equals(FLAG[3])) {
                sb.append(child1.getFamilyId()).append(",");
                sb.append(child1.getMaternalId()).append(",");
                sb.append(child1.getPaternalId()).append(",");
                sb.append("'").append(output1.geneName).append("'").append(",");
                sb.append(IntolerantScoreManager.getValues(output1.geneName)).append(",");
                sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.geneName))).append(",");
                sb.append(flag).append(",");

                sb.append(child1.getName()).append(",");
                sb.append(output1.getString(child1)).append(",");

                sb.append(child2.getName()).append(",");
                sb.append(output2.getString(child2)).append("\n");

                bwCompHet.write(sb.toString());

                sb.setLength(0);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void clearList() {
        outputList.clear();
        geneListVector.clear();
        currentGeneList.clear();
    }

    @Override
    public String toString() {
        return "It is running a sibling compound heterozygosity function...";
    }
}
