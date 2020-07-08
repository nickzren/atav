package function.external.denovo;

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
public class ListDenovoDB extends AnalysisBase {

    BufferedWriter bwDenovoDB = null;
    final String denovoDBFilePath = CommonCommand.outputPath + "denovodb.csv";

    @Override
    public void initOutput() {
        try {
            bwDenovoDB = new BufferedWriter(new FileWriter(denovoDBFilePath));
            bwDenovoDB.write(DenovoDBOutput.getHeader());
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
            PreparedStatement preparedStatement = DenovoDBManager.getPreparedStatement4Region();
            preparedStatement.setString(1, region.getChrStr());
            preparedStatement.setInt(2, region.getStartPosition());
            preparedStatement.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                DenovoDBOutput output = new DenovoDBOutput(rs);

                if (VariantManager.isVariantIdIncluded(output.denovoDB.toString())) {
                    bwDenovoDB.write(output.denovoDB.getVariantId() + ",");
                    bwDenovoDB.write(output.denovoDB.toString());
                    bwDenovoDB.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list denovodb function";
    }
}
