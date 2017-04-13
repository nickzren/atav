package function.test;

import function.AnalysisBase;
import function.genotype.base.DPBinBlockManager;
import static function.test.ConvertCalledVariant.executeSQL;
import function.variant.base.RegionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.DBManager;
import utils.LogManager;

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
            CountVariant.run();
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return "Start running test function";
    }
}
