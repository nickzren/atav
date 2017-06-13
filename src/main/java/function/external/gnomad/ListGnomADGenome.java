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
public class ListGnomADGenome extends AnalysisBase {

    BufferedWriter bwGnomADGenome = null;
    final String gnomADGenomeFilePath = CommonCommand.outputPath + "gnomad.genome.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bwGnomADGenome = new BufferedWriter(new FileWriter(gnomADGenomeFilePath));
            bwGnomADGenome.write(GnomADGenomeOutput.getTitle());
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
        int totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : VariantManager.VARIANT_TYPE) {

                if (VariantManager.isVariantTypeValid(r, varType)) {

                    boolean isIndel = varType.equals("indel");

                    Region region = RegionManager.getRegion(r, varType);

                    String sqlCode = GnomADManager.getSql4MafGenome(region);

                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);

                    while (rset.next()) {
                        GnomADGenomeOutput output = new GnomADGenomeOutput(isIndel, rset);

                        if (VariantManager.isVariantIdIncluded(output.gnomADGenome.getVariantId())
                                && output.isValid()) {
                            bwGnomADGenome.write(output.gnomADGenome.getVariantId() + ",");
                            bwGnomADGenome.write(output.toString());
                            bwGnomADGenome.newLine();
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
        return "Start running list gnomad genome function";
    }
}
