package function.external.mtr;

import function.external.discovehr.*;
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
public class ListMTR extends AnalysisBase {

    BufferedWriter bwMTR = null;
    final String disMTRFilePath = CommonCommand.outputPath + "mtr.csv";

    @Override
    public void initOutput() {
        try {
            bwMTR = new BufferedWriter(new FileWriter(disMTRFilePath));
            bwMTR.write(MTROutput.getTitle());
            bwMTR.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwMTR.flush();
            bwMTR.close();
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
//        int totalNumOfRegionList = RegionManager.getRegionSize();
//
//        for (int r = 0; r < totalNumOfRegionList; r++) {
//
//            for (String varType : VariantManager.VARIANT_TYPE) {
//                if (VariantManager.isVariantTypeValid(r, varType)) {
//                    Region region = RegionManager.getRegion(r, varType);
//
//                    String sqlCode = MTRManager.getSql4MTR(region);
//
//                    ResultSet rset = DBManager.executeReadOnlyQuery(sqlCode);
//
//                    while (rset.next()) {
//                        MTROutput output = new MTROutput(rset);
//
//                        if (VariantManager.isVariantIdIncluded(output.mtr.getVariantPos())) { // needs to fix here
//                            bwMTR.write(output.mtr.getVariantPos() + ",");
//                            bwMTR.write(output.toString());
//                            bwMTR.newLine();
//                        }
//                    }
//
//                    rset.close();
//                }
//            }
//        }
    }

    @Override
    public String toString() {
        return "Start running list DiscovEHR function";
    }
}
