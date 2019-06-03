package function.external.primateai;

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
public class ListPrimateAI extends AnalysisBase {
    BufferedWriter bwPrimateAI = null;
    final String primateAIFilePath = CommonCommand.outputPath + "primate_ai.csv";
    
    @Override
    public void initOutput() {
        try {
            bwPrimateAI = new BufferedWriter(new FileWriter(primateAIFilePath));
            bwPrimateAI.write(PrimateAIOutput.getTitle());
            bwPrimateAI.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwPrimateAI.flush();
            bwPrimateAI.close();
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

            String sqlCode = PrimateAIManager.getSqlByRegion(region);

            ResultSet rset = DBManager.executeConcurReadOnlyQuery(sqlCode);

            while (rset.next()) {
                PrimateAIOutput output = new PrimateAIOutput(rset);

                if (VariantManager.isVariantIdIncluded(output.primateAI.getVariantId())
                        && output.isValid()) {
                    bwPrimateAI.write(output.toString());
                    bwPrimateAI.newLine();
                }
            }

            rset.close();
        }
    }

    @Override
    public String toString() {
        return "Start running list PrimateAI function";
    }
}
