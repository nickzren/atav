package function.external.gnomad;

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
public class ListGnomADExome extends AnalysisBase {

    BufferedWriter bwGnomADExome = null;
    final String gnomADExomeFilePath = CommonCommand.outputPath + "gnomad.exome.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwGnomADExome = new BufferedWriter(new FileWriter(gnomADExomeFilePath));
            bwGnomADExome.write(GnomADExomeOutput.getTitle());
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
        int totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : VariantManager.VARIANT_TYPE) {

                if (VariantManager.isVariantTypeValid(r, varType)) {

                    boolean isIndel = varType.equals("indel");

                    Region region = RegionManager.getRegion(r, varType);

                    String sqlCode = GnomADManager.getSql4Maf(region);

                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

                    while (rset.next()) {
                        GnomADExomeOutput output = new GnomADExomeOutput(isIndel, rset);

                        if (VariantManager.isVariantIdIncluded(output.gnomADExome.getVariantId())
                                && output.isValid()) {
                            bwGnomADExome.write(output.gnomADExome.getVariantId() + ",");
                            bwGnomADExome.write(output.toString());
                            bwGnomADExome.newLine();
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
        return "Start running list gnomad exome function";
    }
}
