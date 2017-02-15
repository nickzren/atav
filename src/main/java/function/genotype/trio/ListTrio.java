package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import static function.genotype.trio.TrioManager.COMP_HET_FLAG;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        listCompHets();

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

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                byte geno = output.getCalledVariant().getGT(trio.getChild().getIndex());

                if (output.isQualifiedGeno(geno)) {
                    output.initDenovoFlag(trio.getChild());

                    doDenovoOutput(output);

                    addVariantToGeneList((TrioOutput) output.clone());
                }
            }
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

    private void doDenovoOutput(TrioOutput output) throws Exception {
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

    private void listCompHets() {
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
            for (int i = 0; i < geneOutputList.size() - 1; i++) {
                TrioOutput output1 = geneOutputList.get(i);
                for (int j = i + 1; j < geneOutputList.size(); j++) {
                    TrioOutput output2 = geneOutputList.get(j);

                    if (output1.child.getId() == output2.child.getId()
                            && output1.getCalledVariant().getVariantIdNegative4Indel()
                            != output2.getCalledVariant().getVariantIdNegative4Indel()) {
                        String compHetFlag = getTrioCompHetFlag(output1, output2);

                        if (!compHetFlag.equals(COMP_HET_FLAG[2])) { // no flag
                            doCompHetOutput(bwCompHet, compHetFlag, output1, output2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private String getTrioCompHetFlag(TrioOutput output1, TrioOutput output2) {
        String flag = TrioManager.getCompHetFlag(output1.cGeno, output1.cDPBin,
                output1.mGeno, output1.mDPBin,
                output1.fGeno, output1.fDPBin,
                output2.cGeno, output2.cDPBin,
                output2.mGeno, output2.mDPBin,
                output2.fGeno, output2.fDPBin);

        flag = TrioManager.getCompHetFlagByDenovo(flag,
                output1.cGeno, output1.cDPBin,
                output1.mGeno, output1.mDPBin,
                output1.fGeno, output1.fDPBin,
                output1.denovoFlag,
                output2.cGeno, output2.cDPBin,
                output2.mGeno, output2.mDPBin,
                output2.fGeno, output2.fDPBin,
                output2.denovoFlag);

        return flag;
    }

    private void doCompHetOutput(BufferedWriter bw, String flag, TrioOutput output1, TrioOutput output2) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(output1.child.getFamilyId()).append(",");
        sb.append(output1.motherName).append(",");
        sb.append(output1.fatherName).append(",");
        sb.append(flag).append(",");

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
