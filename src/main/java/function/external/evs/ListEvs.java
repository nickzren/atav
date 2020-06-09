package function.external.evs;

import function.AnalysisBase;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ListEvs extends AnalysisBase {

    BufferedWriter bwEvs = null;
    final String evsFilePath = CommonCommand.outputPath + "evs.csv";

    @Override
    public void initOutput() {
        try {
            bwEvs = new BufferedWriter(new FileWriter(evsFilePath));
            bwEvs.write(EvsOutput.getHeader());
            bwEvs.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwEvs.flush();
            bwEvs.close();
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
            PreparedStatement preparedStatement = EvsManager.getPreparedStatement4Region();
            preparedStatement.setString(1, region.getChrStr());
            preparedStatement.setInt(2, region.getStartPosition());
            preparedStatement.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                EvsOutput output = new EvsOutput(rs);

                if (VariantManager.isVariantIdIncluded(output.evs.getVariantId())
                        && output.isValid()) {
                    bwEvs.write(output.evs.getVariantId() + ",");
                    bwEvs.write(output.toString());
                    bwEvs.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list evs function";
    }
}
