package function.genotype.base;

import function.annotation.base.EffectManager;
import function.variant.base.Variant;
import global.Data;
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

    public static final int CARRIER_BLOCK_SIZE = 10000;

    private static int currentBlockId = Data.NA;

    private static HashMap<Integer, HashMap<Integer, Carrier>> blockCarrierMap = new HashMap<>(); // variantId <SampleId, CarrierMap> 

    public static void init(Variant var) {
        int blockId = Math.floorDiv(var.getStartPosition(), CARRIER_BLOCK_SIZE);

        if (currentBlockId != blockId) {
            currentBlockId = blockId;

            blockCarrierMap.clear();

            initBlockCarrierMap(var);
        }
    }

    private static void initBlockCarrierMap(Variant var) {
        String sql = "SELECT * FROM called_variant_chr" + var.getChrStr() + " ,"
                + SampleManager.ALL_SAMPLE_ID_TABLE
                + " WHERE block_id = " + currentBlockId
                + " AND highest_impact+0 <= " + EffectManager.getLowestImpact()
                + " AND sample_id = id ";

        try {
            HashMap<Integer, Integer> validVariantCarrierCount = new HashMap<>();

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                Carrier carrier = new Carrier(rs);

                carrier.checkValidOnXY(var);

                carrier.applyQualityFilter();

                int variantId = rs.getInt("variant_id");

                HashMap<Integer, Carrier> varCarrierMap = blockCarrierMap.get(variantId);

                if (varCarrierMap == null) {
                    varCarrierMap = new HashMap<>();
                    blockCarrierMap.put(variantId, varCarrierMap);

                    validVariantCarrierCount.put(variantId, 0);
                }

                if (carrier.isValid()) {
                    validVariantCarrierCount.computeIfPresent(variantId, (k, v) -> v + 1);
                }

                varCarrierMap.put(carrier.getSampleId(), carrier);
            }

            rs.close();

            // removed no qualified carriers variant
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
        String sql = "SELECT * FROM called_variant_chr" + var.getChrStr() + " ,"
                + SampleManager.ALL_SAMPLE_ID_TABLE
                + " WHERE block_id = " + currentBlockId
                + " AND highest_impact+0 <= " + EffectManager.getLowestImpact()
                + " AND sample_id = id"
                + " AND variant_id =" + var.getVariantId();

        try {
            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                Carrier carrier = new Carrier(rs);

                carrier.checkValidOnXY(var);

                carrier.applyQualityFilter();

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
