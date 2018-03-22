package function.external.denovo;

import function.AnalysisBase;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListDenovoDB extends AnalysisBase {

    BufferedWriter bwDenovoDB = null;
    final String denovoDBFilePath = CommonCommand.outputPath + "denovodb.csv";

    @Override
    public void initOutput() {
        try {
            bwDenovoDB = new BufferedWriter(new FileWriter(denovoDBFilePath));
            bwDenovoDB.write(DenovoDBOutput.getTitle());
            bwDenovoDB.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwDenovoDB.flush();
            bwDenovoDB.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
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
    public void processDatabaseData() throws Exception {
        for (int r = 0; r < RegionManager.getRegionSize(); r++) {

            Region region = RegionManager.getRegion(r);

            String sqlCode = DenovoDBManager.getSql(region);

            ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

            while (rset.next()) {
                DenovoDBOutput output = new DenovoDBOutput(rset);

                if (VariantManager.isVariantIdIncluded(output.denovoDB.toString())) {
                    bwDenovoDB.write(output.denovoDB.getVariantId() + ",");
                    bwDenovoDB.write(output.denovoDB.toString());
                    bwDenovoDB.newLine();
                }
            }

            rset.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list denovodb function";
    }
}
