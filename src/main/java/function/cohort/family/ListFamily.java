package function.cohort.family;

import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.Sample;
import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.ThirdPartyToolManager;

/**
 *
 * @author jaimee
 */
public class ListFamily extends AnalysisBase4CalledVar {

    BufferedWriter bwGenotypes = null;
    final String genotypesFilePath = CommonCommand.outputPath + "family_genotypes.csv";

    @Override
    public void initOutput() {
        try {
            bwGenotypes = new BufferedWriter(new FileWriter(genotypesFilePath));
            bwGenotypes.write(FamilyOutput.getHeader());
            bwGenotypes.newLine();
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
            bwGenotypes.flush();
            bwGenotypes.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (CommonCommand.gzip) {
            ThirdPartyToolManager.gzipFile(genotypesFilePath);
        }
    }

    @Override
    public void beforeProcessDatabaseData() {
        if (!FamilyManager.isInit()) {
            FamilyManager.initFamily();
        }
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            for (Family family : FamilyManager.getFamilyList()) {
                for (Sample sample : family.getCaseList()) {
                    FamilyOutput output = new FamilyOutput(calledVar);
                    output.calculateInheritanceModel(family);
                    if (!output.getInheritanceModel().equals(Data.STRING_NA)) {
                        output.initCarrierData(sample);
                        output.initACMG();
                        bwGenotypes.write(output.getString(sample));
                        bwGenotypes.newLine();
                    }
                }
            }

        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list family function";
    }
}
