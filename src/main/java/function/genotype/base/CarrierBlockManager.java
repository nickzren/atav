package function.genotype.base;

import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.variant.base.Region;
import function.variant.base.Variant;
import global.Data;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author qwang
 */
public class CarrierBlockManager {

    //Outer Integer for Variant ID, Inner Integer for SampleID
    private static HashMap<Integer, HashMap<Integer, Carrier>> CarrierMaps = new HashMap<Integer, HashMap<Integer, Carrier>>();
    private static int currentBlockEndPos = Data.NA; //might have different blockID
    private static boolean EnableBlcok = true;

    public static HashMap<Integer, Carrier> getCarrierMap(String varType, int varPos, String chr, int varID) {
        int posIndex = varPos % Data.COVERAGE_BLOCK_SIZE;
        if (posIndex == 0) {
            posIndex = Data.COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }
        int blockEndPos = varPos - posIndex + Data.COVERAGE_BLOCK_SIZE;

        if (currentBlockEndPos == Data.NA || blockEndPos != currentBlockEndPos) {
            buildCarrierMap(varType, varPos, chr, blockEndPos);
        }
        if (CarrierMaps.containsKey(varID)) {
            return CarrierMaps.get(varID);
        } else {
            return new HashMap<Integer, Carrier>();
        }  
    }

    // need to change here latter - nick
    public static HashMap<Integer, Carrier> getCarrierMap(CalledVariant var) {
        if (!EnableBlcok) {
//            return SampleManager.initCarrierMap(var);
        }

        int varPosIndex = getVarPosIndex(var);
        int blockEndPos = getBlockEndPos(var, varPosIndex);

        if (currentBlockEndPos == Data.NA || blockEndPos != currentBlockEndPos) {
            buildCarrierMap(var, blockEndPos);
        } else {
            if (!CarrierMaps.containsKey(var.getVariantId())) {
                return new HashMap<Integer, Carrier>();
            }
        }
        return CarrierMaps.get(var.getVariantId());
    }

    private static void buildCarrierMap(Variant var, int blockID) {
        buildCarrierMap(var.getType(), var.getRegion().getStartPosition(), var.getRegion().getChrStr(), blockID);
    }

    private static void buildCarrierMap(String varType, int varPos, String chr, int blockID) {
        currentBlockEndPos = blockID;
        CarrierMaps.clear();

        String sqlCarrier = "SELECT * "
                //+ "FROM called_" + varType + "_chr21 va,"
                + "FROM new_called_" + varType + "_chr21_4096 va,"
                + Data.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va.block_id = " + blockID
                + " AND va.sample_id = t.id "
                + "ORDER BY va." + varType + "_id";
        try {
            ResultSet rs = DBManager.executeQuery(sqlCarrier);
            int currentVariantID = Data.NA;
            HashMap<Integer, Carrier> currentVariantMap = null;
            while (rs.next()) {
                Carrier carrier = new Carrier();
                carrier.init(rs);
                carrier.checkCoverageFilter(CommandValue.minCaseCoverageCall,
                        CommandValue.minCtrlCoverageCall);
                carrier.checkQualityFilter();
                carrier.checkValidOnXY(new Region(chr, varPos, varPos));

                int variantID = rs.getInt(varType + "_id");
                if (variantID != currentVariantID || currentVariantID == Data.NA) {
                    CarrierMaps.put(variantID, new HashMap<Integer, Carrier>());
                    currentVariantMap = CarrierMaps.get(variantID);
                }
                currentVariantMap.put(carrier.getSampleId(), carrier);
            }
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    protected static int getVarPosIndex(Variant var) { //duplicate these two functions for now
        int posIndex = var.getRegion().getStartPosition() % Data.COVERAGE_BLOCK_SIZE;

        if (posIndex == 0) {
            posIndex = Data.COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }

        return posIndex;
    }

    protected static int getBlockEndPos(Variant var, int varPosIndex) {
        return var.getRegion().getStartPosition() - varPosIndex + Data.COVERAGE_BLOCK_SIZE;
    }
}
