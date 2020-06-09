package function.external.discovehr;

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
public class ListDiscovEHR extends AnalysisBase {

    BufferedWriter bwDisCovEHR = null;
    final String disCovEHRFilePath = CommonCommand.outputPath + "discovehr.csv";

    @Override
    public void initOutput() {
        try {
            bwDisCovEHR = new BufferedWriter(new FileWriter(disCovEHRFilePath));
            bwDisCovEHR.write(DiscovEHROutput.getHeader());
            bwDisCovEHR.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwDisCovEHR.flush();
            bwDisCovEHR.close();
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
            PreparedStatement preparedStatement = DiscovEHRManager.getPreparedStatement4Region();
            preparedStatement.setString(1, region.getChrStr());
            preparedStatement.setInt(2, region.getStartPosition());
            preparedStatement.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                DiscovEHROutput output = new DiscovEHROutput(rs);

                if (VariantManager.isVariantIdIncluded(output.discovEHR.getVariantId())) {
                    bwDisCovEHR.write(output.discovEHR.getVariantId() + ",");
                    bwDisCovEHR.write(output.toString());
                    bwDisCovEHR.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list DiscovEHR function";
    }
}
