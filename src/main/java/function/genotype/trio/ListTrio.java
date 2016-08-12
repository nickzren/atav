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
public class ListTrio extends AnalysisBase4CalledVar {

    BufferedWriter bwDenovo = null;
    final String denovoFilePath = CommonCommand.outputPath + "denovoandhom.csv";

    BufferedWriter bwCompHet = null;
    final String compHetFilePath = CommonCommand.outputPath + "comphet.csv";

    ArrayList<TrioOutput> outputList = new ArrayList<>();
    ArrayList<ArrayList<TrioOutput>> geneListVector = new ArrayList<>();
    HashSet<String> currentGeneList = new HashSet<>();

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

            output.countSampleGeno();

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                output.deleteParentGeno(trio);

                output.calculate();

                if (output.isValid()) {

                    int geno = output.getCalledVariant().getGenotype(trio.getChild().getIndex());

                    if (output.isQualifiedGeno(geno)) {
                        output.initDenovoFlag(trio.getChild());

                        doDenovoOutput(output);

                        outputList.add((TrioOutput) output.clone());
                    }
                }

                output.addParentGeno(trio);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doDenovoOutput(TrioOutput output) throws Exception {
        if (!output.denovoFlag.equals("no flag") && !output.denovoFlag.equals("unknown")) {
            StringBuilder sb = new StringBuilder();
            sb.append(output.child.getFamilyId()).append(",");
            sb.append(output.child.getName()).append(",");
            sb.append(output.motherName).append(",");
            sb.append(output.fatherName).append(",");
            sb.append(output.denovoFlag).append(",");
            sb.append("'").append(output.getCalledVariant().getGeneName()).append("'").append(",");
            sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output.getCalledVariant().getGeneName()))).append(",");
            sb.append(output.toString());
            bwDenovo.write(sb.toString());
            bwDenovo.newLine();
        }
    }

    private void listCompHets() {
        if (outputList.size() > 0) {
            Collections.sort(outputList);
        } else {
            return;
        }

        initGeneVariantList();

        for (ArrayList<TrioOutput> list : geneListVector) {
            LogManager.writeAndPrint("Analyzing qualified variants in gene ("
                    + list.get(0).getCalledVariant().getGeneName() + ")");

            processVariantsByGene(list);
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

    private void processVariantsByGene(ArrayList<TrioOutput> geneVariantList) {
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
        String flag = TrioManager.getCompHetFlag(
                output1.cGeno, output1.cSamtoolsRawCoverage,
                output1.mGeno, output1.mSamtoolsRawCoverage,
                output1.fGeno, output1.fSamtoolsRawCoverage,
                output1.isMinorRef(),
                output2.cGeno, output2.cSamtoolsRawCoverage,
                output2.mGeno, output2.mSamtoolsRawCoverage,
                output2.fGeno, output2.fSamtoolsRawCoverage,
                output2.isMinorRef());
        
        flag = TrioManager.getCompHetFlagByDenovo(flag, 
                output1.cGeno, output1.mGeno, output1.fGeno, output1.isMinorRef(), output1.denovoFlag,
                output2.cGeno, output2.mGeno, output2.fGeno, output2.isMinorRef(), output2.denovoFlag);
        
        return flag;
    }

    private void doCompHetOutput(BufferedWriter bw, String flag, TrioOutput output1, TrioOutput output2) throws Exception {
        double[] coFreq = TrioManager.getCoOccurrenceFreq(output1, output2);

        StringBuilder sb = new StringBuilder();
        sb.append(output1.child.getFamilyId()).append(",");
        sb.append(output1.child.getName()).append(",");
        sb.append(output1.motherName).append(",");
        sb.append(output1.fatherName).append(",");
        sb.append(flag).append(",");
        sb.append("'").append(output1.getCalledVariant().getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.getCalledVariant().getGeneName()))).append(",");
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
        return "It is running a list trio function...";
    }
}
