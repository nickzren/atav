package function.genotype.parent;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import static function.genotype.parent.FamilyManager.COMP_HET_FLAG;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ListParentCompHet extends AnalysisBase4CalledVar {

    BufferedWriter bwCompHet = null;
    final String compHetFilePath = CommonCommand.outputPath + "parent_comphet.csv";

    HashMap<String, List<ParentOutput>> geneVariantListMap = new HashMap<>();

    @Override
    public void initOutput() {
        try {
            bwCompHet = new BufferedWriter(new FileWriter(compHetFilePath));
            bwCompHet.write(ParentOutput.getTitle());
            bwCompHet.newLine();
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
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            ParentOutput output = new ParentOutput(calledVar);

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addVariantToGeneList(ParentOutput output) {
        List<ParentOutput> geneOutputList
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
            for (Map.Entry<String, List<ParentOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                doOutput(entry.getValue());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<ParentOutput> geneOutputList) {
        try {
            for (int i = 0; i < geneOutputList.size(); i++) {
                ParentOutput output1 = geneOutputList.get(i);
                
                for (int j = i + 1; j < geneOutputList.size(); j++) {
                    ParentOutput output2 = geneOutputList.get(j);
                    
                    for (Family family : FamilyManager.getList()) {
                        output1.initFamilyData(family);
                        output2.initFamilyData(family);

                        // find comp het for mother
                        String compHetFlag = FamilyManager.getParentCompHetFlag(
                                output1.cGeno, output1.mGeno, output1.fGeno,
                                output2.cGeno, output2.mGeno, output2.fGeno);

                        if (!compHetFlag.equals(COMP_HET_FLAG[2])) {
                            doCompHetOutput(bwCompHet, output1.motherName, compHetFlag, output1, output2);
                        }

                        // find comp het for father
                        compHetFlag = FamilyManager.getParentCompHetFlag(
                                output1.cGeno, output1.fGeno, output1.mGeno,
                                output2.cGeno, output2.fGeno, output2.mGeno);

                        if (!compHetFlag.equals(COMP_HET_FLAG[2])) {
                            doCompHetOutput(bwCompHet, output1.fatherName, compHetFlag, output1, output2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doCompHetOutput(BufferedWriter bw, String parent, String flag, ParentOutput output1, ParentOutput output2) throws Exception {
        StringJoiner sj = new StringJoiner(",");
        sj.add(output1.child.getFamilyId());
        sj.add(parent);
        sj.add(flag);
        sj.merge(output1.getStringJoiner());
        sj.merge(output2.getStringJoiner());

        bw.write(sj.toString());
        bw.newLine();
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "It is running a list parent comp het function...";
    }
}
