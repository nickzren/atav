package function.external.gnomad;

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
import utils.DBManager;

/**
 *
 * @author nick
 */
public class ListGnomADExome extends AnalysisBase {

    BufferedWriter bwGnomADExome = null;
    final String gnomADExomeFilePath = CommonCommand.outputPath + "gnomad.exome.csv";

    @Override
    public void initOutput() {
        try {
            bwGnomADExome = new BufferedWriter(new FileWriter(gnomADExomeFilePath));
            bwGnomADExome.write(GnomADExomeOutput.getHeader());
            bwGnomADExome.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGnomADExome.flush();
            bwGnomADExome.close();
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
            PreparedStatement preparedStatement = GnomADManager.getPreparedStatement4RegionExome();
            preparedStatement.setString(1, region.getChrStr());
            preparedStatement.setInt(2, region.getStartPosition());
            preparedStatement.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                GnomADExomeOutput output = new GnomADExomeOutput(rs);

                if (VariantManager.isVariantIdIncluded(output.gnomADExome.getVariantId())
                        && output.isValid()) {
                    bwGnomADExome.write(output.gnomADExome.getVariantId() + ",");
                    bwGnomADExome.write(output.toString());
                    bwGnomADExome.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list gnomad exome function";
    }
}
