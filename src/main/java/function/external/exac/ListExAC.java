package function.external.exac;

import function.AnalysisBase;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class ListExAC extends AnalysisBase {

    BufferedWriter bwExac = null;
    final String exacFilePath = CommonCommand.outputPath + "exac.csv";

    @Override
    public void initOutput() {
        try {
            bwExac = new BufferedWriter(new FileWriter(exacFilePath));
            bwExac.write(ExACOutput.getHeader());
            bwExac.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwExac.flush();
            bwExac.close();
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

            String sqlCode = ExACManager.getSqlByRegion(region);

            ResultSet rset = DBManager.executeConcurReadOnlyQuery(sqlCode);

            while (rset.next()) {
                ExACOutput output = new ExACOutput(rset);

                if (VariantManager.isVariantIdIncluded(output.exac.getVariantId())
                        && output.isValid()) {
                    bwExac.write(output.exac.getVariantId() + ",");
                    bwExac.write(output.toString());
                    bwExac.newLine();
                }
            }

            rset.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list exac function";
    }
}
