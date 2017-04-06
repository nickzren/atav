package function.genotype.base;

import static function.genotype.base.SampleManager.ALL_SAMPLE_ID_TABLE;
import function.variant.base.Variant;
import global.Data;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import utils.DBManager;
import utils.ErrorManager;

/**
 * @author nick, qwang
 */
public class DPBinBlockManager {

    public static final int DP_BIN_BLOCK_SIZE = 1000;

    private static ArrayList<SampleDPBin> currentBlockList = new ArrayList<>();
    private static int currentBlockId = Data.INTEGER_NA;

    private static HashMap<Character, Short> dpBin = new HashMap<>();

    public static void init() {
        dpBin.put('a', Data.SHORT_NA);
        dpBin.put('b', (short) 3);
        dpBin.put('c', (short) 10);
        dpBin.put('d', (short) 20);
        dpBin.put('e', (short) 30);
        dpBin.put('f', (short) 50);
        dpBin.put('g', (short) 200);
    }

    public static void add(SampleDPBin sampleDPBin) {
        currentBlockList.add(sampleDPBin);
    }

    public static void initCarrierAndNonCarrierByDPBin(Variant var,
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, NonCarrier> noncarrierMap) {
        int posIndex = var.getStartPosition() % DP_BIN_BLOCK_SIZE;

        int blockId = Math.floorDiv(var.getStartPosition(), DP_BIN_BLOCK_SIZE);

        if (blockId == currentBlockId) {
            for (SampleDPBin sampleDPBin : currentBlockList) {
                Carrier carrier = carrierMap.get(sampleDPBin.getSampleId());

                if (carrier != null) {
                    if (carrier.isValid()) {
                        carrier.setDPBin(sampleDPBin.getDPBin(posIndex));

                        carrier.applyCoverageFilter(GenotypeLevelFilterCommand.minCaseCoverageCall,
                                GenotypeLevelFilterCommand.minCtrlCoverageCall);
                    }
                } else {
                    NonCarrier noncarrier = new NonCarrier(sampleDPBin.getSampleId(),
                            sampleDPBin.getDPBin(posIndex));

                    noncarrier.applyCoverageFilter(GenotypeLevelFilterCommand.minCaseCoverageNoCall,
                            GenotypeLevelFilterCommand.minCtrlCoverageNoCall);

                    if (noncarrier.isValid()) {
                        noncarrierMap.put(noncarrier.getSampleId(), noncarrier);
                    }
                }
            }
        } else {
            currentBlockId = blockId;
            currentBlockList.clear();

            initBlockDPBin(carrierMap, noncarrierMap, var, posIndex, blockId);
        }
    }

    public static void initBlockDPBin(
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, NonCarrier> noncarrierMap,
            Variant var,
            int posIndex,
            int blockId) {
        try {
            String sql = "SELECT sample_id, DP_string FROM DP_bins_chr" + var.getChrStr() + "," + ALL_SAMPLE_ID_TABLE
                    + " WHERE block_id = " + blockId + " AND sample_id = id";

            ResultSet rs = DBManager.executeQuery(sql);
            while (rs.next()) {
                NonCarrier noncarrier = new NonCarrier(rs.getInt("sample_id"), rs.getString("DP_string"), posIndex);

                Carrier carrier = carrierMap.get(noncarrier.getSampleId());

                if (carrier != null) {
                    if (carrier.isValid()) {
                        carrier.setDPBin(noncarrier.getDPBin());

                        carrier.applyCoverageFilter(GenotypeLevelFilterCommand.minCaseCoverageCall,
                                GenotypeLevelFilterCommand.minCtrlCoverageCall);
                    }
                } else {
                    noncarrier.applyCoverageFilter(GenotypeLevelFilterCommand.minCaseCoverageNoCall,
                            GenotypeLevelFilterCommand.minCtrlCoverageNoCall);

                    if (noncarrier.isValid()) {
                        noncarrierMap.put(noncarrier.getSampleId(), noncarrier);
                    }
                }
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static short getCoverageByBin(Character bin) {
        return dpBin.get(bin);
    }

    public static HashMap<Character, Short> getCoverageBin() {
        return dpBin;
    }
}
