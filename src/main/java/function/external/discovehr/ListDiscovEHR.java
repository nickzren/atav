package function.external.discovehr;

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
public class ListDiscovEHR extends AnalysisBase {

    BufferedWriter bwDisCovEHR = null;
    final String disCovEHRFilePath = CommonCommand.outputPath + "discovehr.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwDisCovEHR = new BufferedWriter(new FileWriter(disCovEHRFilePath));
            bwDisCovEHR.write(DiscovEHROutput.getTitle());
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
            
            String sqlCode = DiscovEHRManager.getSql4AF(region);

            ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

            while (rset.next()) {
                DiscovEHROutput output = new DiscovEHROutput(rset);

                if (VariantManager.isVariantIdIncluded(output.discovEHR.getVariantId())) {
                    bwDisCovEHR.write(output.discovEHR.getVariantId() + ",");
                    bwDisCovEHR.write(output.toString());
                    bwDisCovEHR.newLine();
                }

                countVariant();
            }

            rset.close();
        }
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "Start running list DiscovEHR function";
    }
}
