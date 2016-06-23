package function.genotype.family;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import function.annotation.base.GeneManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class FamilyAnalysis extends AnalysisBase4CalledVar {

    FormatManager util = new FormatManager();
    BufferedWriter bwShared = null;
    BufferedWriter bwNotShared = null;
    BufferedWriter bwSummaryShared = null;
    BufferedWriter bwSummaryAll = null;
    final String sharedFilePath = CommonCommand.outputPath + "shared.csv";
    final String notSharedFilePath = CommonCommand.outputPath + "notshared.csv";
    final String summarySharedFilePath = CommonCommand.outputPath + "summary.only.shared.csv";
    final String summaryAllFilePath = CommonCommand.outputPath + "summary.all.shared.csv";
    private final String[] FLAG = {"Shared", "Different zygosity", "Possibly shared",
        "Not shared", "Nnknown", "Partially shared"};

    @Override
    public void initOutput() {
        try {
            bwShared = new BufferedWriter(new FileWriter(sharedFilePath));
            bwShared.write(FamilyOutput.getTitle());
            bwShared.newLine();

            bwNotShared = new BufferedWriter(new FileWriter(notSharedFilePath));
            bwNotShared.write(FamilyOutput.getTitle());
            bwNotShared.newLine();

            bwSummaryShared = new BufferedWriter(new FileWriter(summarySharedFilePath));
            bwSummaryShared.write(FamilySummary.getTitle());
            bwSummaryShared.newLine();

            bwSummaryAll = new BufferedWriter(new FileWriter(summaryAllFilePath));
            bwSummaryAll.write(FamilySummary.getTitle());
            bwSummaryAll.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwShared.flush();
            bwShared.close();
            bwNotShared.flush();
            bwNotShared.close();
            bwSummaryShared.flush();
            bwSummaryShared.close();
            bwSummaryAll.flush();
            bwSummaryAll.close();
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
        outputSummary();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            FamilyOutput output = new FamilyOutput(calledVar);

            output.countSampleGeno();

            output.calculate();

            if (output.isValid()) {

                for (String familyId : FamilyManager.getUserFamilyIdSet()) {
                    output.familyId = familyId;

                    output.resetFamilyData();

                    output.calculateFamilyNum(familyId);

                    output.calculateFamilyFreq();

                    output.initFlag();

                    doOutput(output);

                    FamilyManager.updateFamilySummary(output);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputSummary() {
        FamilyManager.initSummaryList();

        outputSummary(FamilyManager.getSummarySharedList(), bwSummaryShared);

        outputSummary(FamilyManager.getSummaryAllList(), bwSummaryAll);
    }

    private void outputSummary(ArrayList<FamilySummary> list, BufferedWriter bwSummary) {
        try {
            for (FamilySummary summary : list) {
                StringBuilder sb = new StringBuilder();
                sb.append("'").append(summary.getGeneName()).append("'").append(",");
                sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(summary.getGeneName()))).append(",");
                sb.append(summary.getTotalSharedFamilyNum()).append(",");
                sb.append(summary.getTotalSharedHetFamilyNum()).append(",");
                sb.append(summary.getTotalSharedHomFamilyNum()).append(",");
                sb.append(summary.getTotalSharedCphtFamilyNum()).append(",");
                sb.append(summary.getTotalSnv()).append(",");
                sb.append(summary.getTotalIndel()).append(",");
                sb.append(summary.getFamilyIdSet()).append(",");
                sb.append(summary.getVariantIdSet()).append("\n");

                bwSummary.write(sb.toString());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(FamilyOutput output) throws Exception {
        if (output.flag.equals(FLAG[4])) { // Unknown
            return;
        } else if (output.flag.equals(FLAG[3])) { // Not shared
            bwNotShared.write(output.toString());
            bwNotShared.newLine();
        } else {
            bwShared.write(output.toString());
            bwShared.newLine();
        }
    }

    @Override
    public String toString() {
        return "It is running a family analysis function...";
    }
}
