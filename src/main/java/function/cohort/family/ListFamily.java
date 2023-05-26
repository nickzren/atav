package function.cohort.family;

import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.SampleManager;
import function.variant.base.Output;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
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
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            FamilyOutput output = new FamilyOutput(calledVar);
            
//            for (Sample sample : SampleManager.getList()){  
//                   
//            }
            
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list variant genotype function";
    }
}
