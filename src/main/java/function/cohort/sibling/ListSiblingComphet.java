package function.cohort.sibling;

import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import function.cohort.trio.TrioManager;
import static function.cohort.trio.TrioManager.COMP_HET_FLAG;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class ListSiblingComphet extends AnalysisBase4CalledVar {

    final String comphetSharedFilePath = CommonCommand.outputPath + "comphet.shared.csv";
    final String comphetNotSharedFilePath = CommonCommand.outputPath + "comphet.notshared.csv";
    BufferedWriter compHetSharedBw = null;
    BufferedWriter compHetNotSharedBw = null;

    HashMap<String, List<CompHetOutput>> geneVariantListMap = new HashMap<>();

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

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addVariantToGeneList(CompHetOutput output) {
        List<CompHetOutput> geneOutputList
                = geneVariantListMap.get(output.getCalledVariant().getGeneName());

        if (geneOutputList == null) {
            geneOutputList = new ArrayList<>();
            geneOutputList.add(output);
            geneVariantListMap.put(output.getCalledVariant().getGeneName(), geneOutputList);
        } else {
            geneOutputList.add(output);
        }
    }

    private void listCompHets() {
        if (geneVariantListMap.isEmpty()) {
            return;
        }

        try {
            for (Entry<String, List<CompHetOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                doOutput(entry.getValue());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<CompHetOutput> geneOutputList) {
        int outputSize = geneOutputList.size();

        CompHetOutput output1, output2;

        for (int i = 0; i < outputSize; i++) {
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

                            doOutput(output1, output2, child1, child1Flag, child2, child2Flag, flag);
                        }
                    }
                }
            }
        }
    }

    private String getFlag(String child1Flag, String child2Flag) {
        if (child1Flag.equals(COMP_HET_FLAG[0]) && child2Flag.equals(COMP_HET_FLAG[0])) {
            return FLAG[0]; // Shared
        } else if ((child1Flag.equals(COMP_HET_FLAG[1]) && child2Flag.equals(COMP_HET_FLAG[0]))
                || (child1Flag.equals(COMP_HET_FLAG[1]) && child2Flag.equals(COMP_HET_FLAG[1]))
                || (child1Flag.equals(COMP_HET_FLAG[0]) && child2Flag.equals(COMP_HET_FLAG[1]))) {
            return FLAG[1]; // Possibly shared
        } else if ((child1Flag.equals(COMP_HET_FLAG[2]) && child2Flag.equals(COMP_HET_FLAG[0]))
                || (child1Flag.equals(COMP_HET_FLAG[2]) && child2Flag.equals(COMP_HET_FLAG[1]))
                || (child1Flag.equals(COMP_HET_FLAG[1]) && child2Flag.equals(COMP_HET_FLAG[2]))
                || (child1Flag.equals(COMP_HET_FLAG[0]) && child2Flag.equals(COMP_HET_FLAG[2]))) {
            return FLAG[2]; // Not shared
        }

        return FLAG[3];
    }

    private String getTrioCompHetFlag(CompHetOutput output1, CompHetOutput output2,
            Sample child, Sample mother, Sample father) {
        byte cGeno1 = output1.getCalledVariant().getGT(child.getIndex());
        byte mGeno1 = output1.getCalledVariant().getGT(mother.getIndex());
        byte fGeno1 = output1.getCalledVariant().getGT(father.getIndex());

        byte cGeno2 = output2.getCalledVariant().getGT(child.getIndex());
        byte mGeno2 = output2.getCalledVariant().getGT(mother.getIndex());
        byte fGeno2 = output2.getCalledVariant().getGT(father.getIndex());

        return TrioManager.getCompHetFlag(
                cGeno1, mGeno1, fGeno1,
                cGeno2, mGeno2, fGeno2);
    }

    private void doOutput(
            CompHetOutput output1, CompHetOutput output2,
            Sample child1, String child1Flag,
            Sample child2, String child2Flag,
            String flag) {
        try {
            if (!flag.equals(FLAG[3])) {
                StringJoiner sj = new StringJoiner(",");
                
                sj.add(child1.getFamilyId());
                sj.add(child1.getPaternalId());
                sj.add(child1.getMaternalId());
                sj.add(flag);
                sj.add(child1.getName());
                sj.add(child1Flag);
                sj.add(child2.getName());
                sj.add(child2Flag);
                sj.merge(output1.getStringJoiner(child1, child2));
                sj.merge(output2.getStringJoiner(child2, child2));

                if (flag.equals(FLAG[2])) {
                    compHetNotSharedBw.write(sj.toString());
                    compHetNotSharedBw.newLine();
                } else {
                    compHetSharedBw.write(sj.toString());
                    compHetSharedBw.newLine();
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "Start running sibling compound heterozygosity function";
    }
}
