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
public class ListExac extends AnalysisBase {

    BufferedWriter bwExac = null;
    final String exacFilePath = CommonCommand.outputPath + "exac.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwExac = new BufferedWriter(new FileWriter(exacFilePath));
            bwExac.write(ExacOutput.getTitle());
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
        int totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : VariantManager.VARIANT_TYPE) {

                if (VariantManager.isVariantTypeValid(r, varType)) {

                    boolean isIndel = varType.equals("indel");

                    Region region = RegionManager.getRegion(r, varType);

                    String sqlCode = ExacManager.getSql4Maf(isIndel, region);

                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

                    while (rset.next()) {
                        ExacOutput output = new ExacOutput(isIndel, rset);

                        if (VariantManager.isVariantIdIncluded(output.exac.getVariantId())
                                && output.isValid()) {
                            bwExac.write(output.exac.getVariantId() + ",");
                            bwExac.write(output.toString());
                            bwExac.newLine();
                        }

                        countVariant();
                    }

                    rset.close();
                }
            }
        }
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "Start running list exac function";
    }
}
