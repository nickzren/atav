package function.external.denovo;

import function.AnalysisBase;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ListDenovoDB extends AnalysisBase {

    BufferedWriter bwDenovoDB = null;
    final String denovoDBFilePath = CommonCommand.outputPath + "denovodb.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwDenovoDB = new BufferedWriter(new FileWriter(denovoDBFilePath));
            bwDenovoDB.write(DenovoDBOutput.getTitle());
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
        int totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : VariantManager.VARIANT_TYPE) {

                if (VariantManager.isVariantTypeValid(r, varType)) {

                    Region region = RegionManager.getRegion(r, varType);

                    String sqlCode = DenovoDBManager.getSql(region);

                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

                    while (rset.next()) {
                        DenovoDBOutput output = new DenovoDBOutput(rset);

                        if (VariantManager.isVariantIdIncluded(output.denovoDB.toString())) {
                            bwDenovoDB.write(output.denovoDB.toString() + ",");
                            bwDenovoDB.write(output.denovoDB.getOutput());
                            bwDenovoDB.newLine();
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
        return "Start running list denovodb function";
    }
}
