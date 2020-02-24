package function.test;

import function.AnalysisBase;
import global.Data;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * code here for any testing or small ETL task purpose
 *
 * @author nick
 */
public class Test extends AnalysisBase {

    @Override
    public void initOutput() {
    }

    @Override
    public void closeOutput() {
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
//            String input = Data.ATAV_HOME + "data/CHM-eval/um75-hs37d5.bed";
//            SplitFileByChr.run(input);
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return "Start running test function";
    }
}
