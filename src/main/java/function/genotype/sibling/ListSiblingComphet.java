package function.genotype.sibling;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.annotation.base.GeneManager;
import function.genotype.trio.ListTrioCompHet;
import utils.CommonCommand;
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

    final String comphetSharedFilePath = CommonCommand.outputPath + "comphet.shared.csv";
    final String comphetNotSharedFilePath = CommonCommand.outputPath + "comphet.notshared.csv";
    BufferedWriter compHetSharedBw = null;
    BufferedWriter compHetNotSharedBw = null;
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
            compHetSharedBw = new BufferedWriter(new FileWriter(comphetSharedFilePath));
            compHetSharedBw.write(CompHetOutput.getTitle());
            compHetSharedBw.newLine();
            compHetNotSharedBw = new BufferedWriter(new FileWriter(comphetNotSharedFilePath));
            compHetNotSharedBw.write(CompHetOutput.getTitle());
            compHetNotSharedBw.newLine();
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
            compHetSharedBw.flush();
            compHetSharedBw.close();
            compHetNotSharedBw.flush();
            compHetNotSharedBw.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CompHetOutput output = new CompHetOutput(calledVar);

            output.countSampleGeno();

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

        CompHetOutput output1, output2;

        for (int i = 0; i < outputSize - 1; i++) {
            output1 = geneOutputList.get(i);

            for (int j = i + 1; j < outputSize; j++) {
                output2 = geneOutputList.get(j);

                for (Family family : FamilyManager.getList()) {

                    for (int c1 = 0; c1 < family.getChildList().size() - 1; c1++) {

                        for (int c2 = c1 + 1; c2 < family.getChildList().size(); c2++) {
                            Sample child1 = family.getChildList().get(c1);
                            Sample child2 = family.getChildList().get(c2);

                            // child1 trio comp het flag
                            String child1Flag = getTrioCompHetFlag(output1, output2, child1,
                                    family.getMother(), family.getFather());

                            // child2 trio comp het flag
                            String child2Flag = getTrioCompHetFlag(output1, output2, child2,
                                    family.getMother(), family.getFather());

                            // sibling comp het flag
                            String flag = getFlag(child1Flag, child2Flag);

                            doOutput(sb, output1, output2, child1, child1Flag, child2, child2Flag, flag);
                        }
                    }
                }
            }
        }
    }

    private String getFlag(String child1Flag, String child2Flag) {
        if (child1Flag.equals(ListTrioCompHet.FLAG[0]) && child2Flag.equals(ListTrioCompHet.FLAG[0])) {
            return FLAG[0]; // Shared
        } else if ((child1Flag.equals(ListTrioCompHet.FLAG[1]) && child2Flag.equals(ListTrioCompHet.FLAG[0]))
                || (child1Flag.equals(ListTrioCompHet.FLAG[1]) && child2Flag.equals(ListTrioCompHet.FLAG[1]))
                || (child1Flag.equals(ListTrioCompHet.FLAG[0]) && child2Flag.equals(ListTrioCompHet.FLAG[1]))) {
            return FLAG[1]; // Possibly shared
        } else if ((child1Flag.equals(ListTrioCompHet.FLAG[2]) && child2Flag.equals(ListTrioCompHet.FLAG[0]))
                || (child1Flag.equals(ListTrioCompHet.FLAG[2]) && child2Flag.equals(ListTrioCompHet.FLAG[1]))
                || (child1Flag.equals(ListTrioCompHet.FLAG[1]) && child2Flag.equals(ListTrioCompHet.FLAG[2]))
                || (child1Flag.equals(ListTrioCompHet.FLAG[0]) && child2Flag.equals(ListTrioCompHet.FLAG[2]))) {
            return FLAG[2]; // Not shared
        }

        return FLAG[3];
    }

    private String getTrioCompHetFlag(CompHetOutput output1, CompHetOutput output2,
            Sample child, Sample mother, Sample father) {
        int cGeno1 = output1.getCalledVariant().getGenotype(child.getIndex());

        int cCov1 = output1.getCalledVariant().getCoverage(child.getIndex());
        int mGeno1 = output1.getCalledVariant().getGenotype(mother.getIndex());
        int mCov1 = output1.getCalledVariant().getCoverage(mother.getIndex());
        int fGeno1 = output1.getCalledVariant().getGenotype(father.getIndex());
        int fCov1 = output1.getCalledVariant().getCoverage(father.getIndex());

        int cGeno2 = output2.getCalledVariant().getGenotype(child.getIndex());

        int cCov2 = output2.getCalledVariant().getCoverage(child.getIndex());
        int mGeno2 = output2.getCalledVariant().getGenotype(mother.getIndex());
        int mCov2 = output2.getCalledVariant().getCoverage(mother.getIndex());
        int fGeno2 = output2.getCalledVariant().getGenotype(father.getIndex());
        int fCov2 = output2.getCalledVariant().getCoverage(father.getIndex());

        return ListTrioCompHet.getCompHetStatus(
                cGeno1, cCov1,
                mGeno1, mCov1,
                fGeno1, fCov1,
                output1.isMinorRef(),
                cGeno2, cCov2,
                mGeno2, mCov2,
                fGeno2, fCov2,
                output2.isMinorRef());
    }

    private void doOutput(StringBuilder sb,
            CompHetOutput output1, CompHetOutput output2,
            Sample child1, String child1Flag,
            Sample child2, String child2Flag,
            String flag) {
        try {
            if (!flag.equals(FLAG[3])) {
                sb.append(child1.getFamilyId()).append(",");
                sb.append(child1.getPaternalId()).append(",");
                sb.append(child1.getMaternalId()).append(",");
                sb.append(flag).append(",");
                sb.append(child1.getName()).append(",");
                sb.append(child1Flag).append(",");
                sb.append(child2.getName()).append(",");
                sb.append(child2Flag).append(",");

                sb.append("'").append(output1.geneName).append("'").append(",");
                sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.geneName))).append(",");

                sb.append(output1.getString(child1, child2));
                sb.append(output2.getString(child2, child2));

                if (flag.equals(FLAG[2])) {
                    compHetNotSharedBw.write(sb.toString());
                    compHetNotSharedBw.newLine();
                } else {
                    compHetSharedBw.write(sb.toString());
                    compHetSharedBw.newLine();
                }

                sb.setLength(0); // clear data
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
        return "Start running sibling compound heterozygosity function";
    }
}
