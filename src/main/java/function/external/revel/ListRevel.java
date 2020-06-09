package function.external.revel;

import function.AnalysisBase;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListRevel extends AnalysisBase {

    BufferedWriter bwRevel = null;
    final String revelFilePath = CommonCommand.outputPath + "revel.csv";

    @Override
    public void initOutput() {
        try {
            bwRevel = new BufferedWriter(new FileWriter(revelFilePath));
            bwRevel.write(RevelOutput.getHeader());
            bwRevel.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwRevel.flush();
            bwRevel.close();
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
            PreparedStatement preparedStatemen = RevelManager.getPreparedStatement4Region();
            preparedStatemen.setString(1, region.getChrStr());
            preparedStatemen.setInt(2, region.getStartPosition());
            preparedStatemen.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatemen.executeQuery();
            while (rs.next()) {
                RevelOutput output = new RevelOutput(rs);

                if (VariantManager.isVariantIdIncluded(output.revel.getVariantId())
                        && output.isValid()) {
                    bwRevel.write(output.toString());
                    bwRevel.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list revel function";
    }
}
