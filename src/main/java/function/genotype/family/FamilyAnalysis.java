package function.genotype.family;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import function.genotype.base.SampleManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

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
    BufferedWriter bwQualifiedVariants = null;
    final String sharedFilePath = CommandValue.outputPath + "shared.csv";
    final String notSharedFilePath = CommandValue.outputPath + "notshared.csv";
    final String summarySharedFilePath = CommandValue.outputPath + "summary.only.shared.csv";
    final String summaryAllFilePath = CommandValue.outputPath + "summary.all.shared.csv";
    final String variantCarrierPath = CommandValue.outputPath + "variant.carrier.csv";
    private final String[] FLAG = {"Shared", "Different zygosity", "Possibly shared",
        "Not shared", "Nnknown", "Partially shared"};

    @Override
    public void initOutput() {
        try {
            bwShared = new BufferedWriter(new FileWriter(sharedFilePath));
            bwShared.write(FamilyOutput.title);
            bwShared.newLine();

            bwNotShared = new BufferedWriter(new FileWriter(notSharedFilePath));
            bwNotShared.write(FamilyOutput.title);
            bwNotShared.newLine();

            bwSummaryShared = new BufferedWriter(new FileWriter(summarySharedFilePath));
            bwSummaryShared.write(FamilySummary.title);
            bwSummaryShared.newLine();

            bwSummaryAll = new BufferedWriter(new FileWriter(summaryAllFilePath));
            bwSummaryAll.write(FamilySummary.title);
            bwSummaryAll.newLine();

            bwQualifiedVariants = new BufferedWriter(new FileWriter(variantCarrierPath));
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
            bwQualifiedVariants.flush();
            bwQualifiedVariants.close();
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

            output.countSampleGenoCov();

            output.calculate();

            if (output.isValid()) {

                HashSet<String> familyIdSet = calledVar.getFamilyIdSet();

                for (String familyId : familyIdSet) {
                    output.familyId = familyId;

                    output.resetFamilyData();

                    output.calculateFamilyNum(familyId);

                    output.calculateFamilyFreq();

                    output.initFlag();

                    doOutput(output);

                    outputVariantCarrier(output);

                    FamilyManager.updateFamilySummary(output);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputVariantCarrier(FamilyOutput output) throws Exception {
        if (output.isAllShared()) {
            bwQualifiedVariants.write(output.getCalledVariant().getVariantIdStr() + ",");

            int geno;
            for (Sample sample : SampleManager.getList()) {
                geno = output.getCalledVariant().getGenotype(sample.getIndex());

                if (output.isQualifiedGeno(geno)) {
                    bwQualifiedVariants.write(sample.getName() + ",");
                }
            }

            bwQualifiedVariants.newLine();
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
                sb.append(IntolerantScoreManager.getValues(summary.getGeneName())).append(",");
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
