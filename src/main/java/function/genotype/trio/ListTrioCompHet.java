package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import global.Index;
import function.annotation.base.GeneManager;
import static function.genotype.trio.TrioManager.COMP_HET_FLAG;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListTrioCompHet extends AnalysisBase4CalledVar {

    ArrayList<TrioOutput> outputList = new ArrayList<>();
    ArrayList<ArrayList<TrioOutput>> geneListVector = new ArrayList<>();
    HashSet<String> currentGeneList = new HashSet<>();
    BufferedWriter bwCompHet = null;
    final String compHetFilePath = CommonCommand.outputPath + "comphet.csv";

    @Override
    public void initOutput() {
        try {
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
            bwCompHet.flush();
            bwCompHet.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (TrioCommand.isRunTier) {
            ThirdPartyToolManager.runTrioCompHetTier(compHetFilePath);
        }
    }

    @Override
    public void beforeProcessDatabaseData() {
        TrioManager.initList();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            TrioOutput output = new TrioOutput(calledVar);

            output.countSampleGeno();

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                output.deleteParentGeno(trio);

                output.calculate();

                if (output.isValid()) {

                    int geno = output.getCalledVariant().getGenotype(trio.getChild().getIndex());

                    if (output.isQualifiedGeno(geno)) {
                        outputList.add((TrioOutput) output.clone());
                    }
                }

                output.addParentGeno(trio);
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
            for (ArrayList<TrioOutput> list : geneListVector) {
                LogManager.writeAndPrint("Analyzing qualified variants in gene ("
                        + list.get(0).getCalledVariant().getGeneName() + ")");

                doOutput(list);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initGeneVariantList() {
        ArrayList<TrioOutput> geneOutputList = null;

        for (TrioOutput output : outputList) {
            if (!currentGeneList.contains(output.getCalledVariant().getGeneName())) {
                currentGeneList.add(output.getCalledVariant().getGeneName());

                geneOutputList = new ArrayList<>();
                geneOutputList.add(output);
                geneListVector.add(geneOutputList);
            } else {
                geneOutputList.add(output);
            }
        }
    }

    private void doOutput(ArrayList<TrioOutput> geneVariantList) {
        try {
            for (int i = 0; i < geneVariantList.size() - 1; i++) {
                TrioOutput output1 = geneVariantList.get(i);
                for (int j = i + 1; j < geneVariantList.size(); j++) {
                    TrioOutput output2 = geneVariantList.get(j);

                    if (output1.child.getId() == output2.child.getId()
                            && output1.getCalledVariant().getVariantIdNegative4Indel()
                            != output2.getCalledVariant().getVariantIdNegative4Indel()) {
                        String compHetFlag = getTrioCompHetFlag(output1, output2);

                        if (compHetFlag.equals(COMP_HET_FLAG[0]) || compHetFlag.equals(COMP_HET_FLAG[1])) {
                            doOutput(bwCompHet, compHetFlag, output1, output2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private String getTrioCompHetFlag(TrioOutput output1, TrioOutput output2) {
        return TrioManager.getCompHetStatus(
                output1.cGeno, output1.cSamtoolsRawCoverage,
                output1.mGeno, output1.mSamtoolsRawCoverage,
                output1.fGeno, output1.fSamtoolsRawCoverage,
                output1.isMinorRef(),
                output2.cGeno, output2.cSamtoolsRawCoverage,
                output2.mGeno, output2.mSamtoolsRawCoverage,
                output2.fGeno, output2.fSamtoolsRawCoverage,
                output2.isMinorRef());
    }

    private void doOutput(BufferedWriter bw, String flag, TrioOutput output1, TrioOutput output2) throws Exception {
        double[] coFreq = TrioManager.getCoOccurrenceFreq(output1, output2);

        StringBuilder sb = new StringBuilder();
        sb.append(output1.child.getFamilyId()).append(",");
        sb.append(output1.child.getName()).append(",");
        sb.append(output1.motherName).append(",");
        sb.append(output1.fatherName).append(",");
        sb.append("'").append(output1.getCalledVariant().getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.getCalledVariant().getGeneName()))).append(",");
        sb.append(flag).append(",");
        sb.append(FormatManager.getDouble(coFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(coFreq[Index.CTRL])).append(",");

        sb.append(output1.toString());
        sb.append(output2.toString());

        bw.write(sb.toString());
        bw.newLine();
    }

    private void clearList() {
        outputList.clear();
        geneListVector.clear();
        currentGeneList.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio compound heterozygosity function...";
    }
}
