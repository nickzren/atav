package function.external.evs;

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
public class ListEvs extends AnalysisBase {

    BufferedWriter bwEvs = null;
    final String evsFilePath = CommonCommand.outputPath + "evs.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwEvs = new BufferedWriter(new FileWriter(evsFilePath));
            bwEvs.write(EvsOutput.getTitle());
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
        int totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : VariantManager.VARIANT_TYPE) {
                if (VariantManager.isVariantTypeValid(r, varType)) {
                    boolean isIndel = varType.equals("indel");

                    Region region = RegionManager.getRegion(r, varType);

                    String sqlCode = EvsManager.getSql4Maf(isIndel, region);

                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

                    while (rset.next()) {
                        EvsOutput output = new EvsOutput(isIndel, rset);

                        if (VariantManager.isIncluded(output.evs.getVariantId())
                                && output.isValid()) {
                            bwEvs.write(output.evs.getVariantId() + ",");
                            bwEvs.write(output.toString());
                            bwEvs.newLine();
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
        return "Start running list evs function";
    }
}
