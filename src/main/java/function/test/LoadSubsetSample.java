package function.test;

import java.io.File;
import java.sql.SQLException;
import utils.DBManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class LoadSubsetSample {

    // minor config tweak for this task
    // server annodb01
    // db: annodb_pgm
    
    public static void run() throws SQLException {
        File dir = new File(OutputSubsetSample.OUTPUT_PATH);

        for (File file : dir.listFiles()) {
            String fileName = file.getName();

            if (fileName.equals("called_snv.txt")) { // leave it to the last step
                continue;
            }

            if (fileName.endsWith(".txt")) {
                String sql = "LOAD DATA INFILE "
                        + "'" + file.getAbsolutePath() + "' "
                        + "IGNORE INTO TABLE " + fileName.substring(0, fileName.indexOf(".txt"));

                LogManager.writeAndPrint(sql);
                DBManager.executeQuery(sql);

                String[] cmd = {"mv "
                    + file.getAbsolutePath() + " "
                    + file.getAbsolutePath() + ".loaded"};
                ThirdPartyToolManager.systemCall(cmd);
            }
        }
    }
}
