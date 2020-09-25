package function.cohort.base;

import function.annotation.base.EffectManager;
import function.variant.base.RegionManager;
import function.variant.base.Variant;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map.Entry;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class CarrierBlockManager {

    public static final int CARRIER_BLOCK_SIZE = 1000;

    private static int currentBlockId = Data.INTEGER_NA;

    private static HashMap<Integer, HashMap<Integer, Carrier>> blockCarrierMap = new HashMap<>(); // variantId <SampleId, CarrierMap>

    private static final HashMap<String, PreparedStatement> preparedStatement4BlockMap = new HashMap<>();
    private static final HashMap<String, PreparedStatement> preparedStatement4VariantMap = new HashMap<>();

    public static void init() {
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "SELECT sample_id,variant_id,block_id,GT,DP,AD_REF,AD_ALT,GQ,VQSLOD,SOR,FS,MQ,QD,QUAL,ReadPosRankSum,MQRankSum,FILTER+0,PGT,PID_variant_id,HP_GT,HP_variant_id "
                    + "FROM called_variant_chr" + chr + "," + EffectManager.TMP_IMPACT_TABLE + ","
                    + SampleManager.TMP_SAMPLE_ID_TABLE
                    + " WHERE block_id=?"
                    + " AND highest_impact=input_impact"
                    + " AND sample_id=input_sample_id";

            preparedStatement4BlockMap.put(chr, DBManager.initPreparedStatement(sql));

            sql = "SELECT sample_id,variant_id,block_id,GT,DP,AD_REF,AD_ALT,GQ,VQSLOD,SOR,FS,MQ,QD,QUAL,ReadPosRankSum,MQRankSum,FILTER+0,PGT,PID_variant_id,HP_GT,HP_variant_id "
                    + "FROM called_variant_chr" + chr + "," + EffectManager.TMP_IMPACT_TABLE + ","
                    + SampleManager.TMP_SAMPLE_ID_TABLE
                    + " WHERE block_id=?"
                    + " AND highest_impact=input_impact"
                    + " AND sample_id=input_sample_id"
                    + " AND variant_id=?";

            preparedStatement4VariantMap.put(chr, DBManager.initPreparedStatement(sql));
        }
    }

    public static void init(Variant var) {
        int blockId = Math.floorDiv(var.getStartPosition(), CARRIER_BLOCK_SIZE);

        if (currentBlockId != blockId) {
            currentBlockId = blockId;

            blockCarrierMap.clear();

            initBlockCarrierMap(var);
        }
    }

    private static void initBlockCarrierMap(Variant var) {
        try {
            HashMap<Integer, Integer> validVariantCarrierCount = new HashMap<>();
            
            PreparedStatement preparedStatement = preparedStatement4BlockMap.get(var.getChrStr());
            preparedStatement.setInt(1, currentBlockId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Carrier carrier = new Carrier(rs);

                int variantId = rs.getInt("variant_id");

                HashMap<Integer, Carrier> varCarrierMap = blockCarrierMap.get(variantId);

                if (varCarrierMap == null) {
                    varCarrierMap = new HashMap<>();
                    blockCarrierMap.put(variantId, varCarrierMap);
                }

                carrier.checkValidOnXY(var);

                carrier.applyQualityFilter(var.isSnv());

                if (carrier.isValid()) {
                    validVariantCarrierCount.computeIfPresent(variantId, (k, v) -> v + 1);
                }
                
                // add carrier in order to distinguish non-carrier
                varCarrierMap.put(carrier.getSampleId(), carrier);
            }

            rs.close();
            
            // remove variant if no qualified carriers
            for (Entry<Integer, Integer> entry : validVariantCarrierCount.entrySet()) {
                if (entry.getValue() == 0) {
                    blockCarrierMap.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initCarrierMap(HashMap<Integer, Carrier> carrierMap, Variant var) {
        try {
            PreparedStatement preparedStatement = preparedStatement4VariantMap.get(var.getChrStr());
            int blockId = Math.floorDiv(var.getStartPosition(), CARRIER_BLOCK_SIZE);
            preparedStatement.setInt(1, blockId);
            preparedStatement.setInt(2, var.getVariantId());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Carrier carrier = new Carrier(rs);

                carrier.checkValidOnXY(var);

                carrier.applyQualityFilter(var.isSnv());

                carrierMap.put(carrier.getSampleId(), carrier);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static HashMap<Integer, Carrier> getVarCarrierMap(int variantId) {
        return blockCarrierMap.get(variantId);
    }
}
