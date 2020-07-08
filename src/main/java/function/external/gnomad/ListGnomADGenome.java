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
public class ListGnomADGenome extends AnalysisBase {

    BufferedWriter bwGnomADGenome = null;
    final String gnomADGenomeFilePath = CommonCommand.outputPath + "gnomad.genome.csv";

    @Override
    public void initOutput() {
        try {
            bwGnomADGenome = new BufferedWriter(new FileWriter(gnomADGenomeFilePath));
            bwGnomADGenome.write(GnomADGenomeOutput.getHeader());
            bwGnomADGenome.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwGnomADGenome.flush();
            bwGnomADGenome.close();
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
            PreparedStatement preparedStatement = GnomADManager.getPreparedStatement4RegionGenome(region.getChrStr());
            preparedStatement.setString(1, region.getChrStr());
            preparedStatement.setInt(2, region.getStartPosition());
            preparedStatement.setInt(3, region.getEndPosition());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                GnomADGenomeOutput output = new GnomADGenomeOutput(rs);

                if (VariantManager.isVariantIdIncluded(output.gnomADGenome.getVariantId())
                        && output.isValid()) {
                    bwGnomADGenome.write(output.gnomADGenome.getVariantId() + ",");
                    bwGnomADGenome.write(output.toString());
                    bwGnomADGenome.newLine();
                }
            }

            rs.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list gnomad genome function";
    }
}
