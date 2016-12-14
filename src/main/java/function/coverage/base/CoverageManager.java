package function.coverage.base;

import function.genotype.base.DPBinBlockManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import function.variant.base.Region;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class CoverageManager {

    public static HashMap<Integer, Integer> getSampleCoveredLengthMap(Region region) {
        HashMap<Integer, Integer> sampleCoveredLengthMap = new HashMap<>();

        String sql = getDPBinsSQLStr(region);
        CoverageManager.initSampleCoveredLengthMap(sql, region, sampleCoveredLengthMap);

        return sampleCoveredLengthMap;
    }

    public static SiteCoverage getSiteCoverage(Region region) {
        SiteCoverage siteCoverage = new SiteCoverage(region.getLength());

        String sql = getDPBinsSQLStr(region);
        CoverageManager.initSiteCoverage(sql, region, siteCoverage);

        return siteCoverage;
    }

    public static String getDPBinsSQLStr(Region region) {
        String str = "SELECT * FROM DP_bins_chr" + region.getChrStr() + ","
                + SampleManager.ALL_SAMPLE_ID_TABLE
                + " WHERE block_id in (" + getBlockIdStr(region) + ")"
                + " AND sample_id = id ";

        return str;
    }

    private static String getBlockIdStr(Region region) {
        int startPosBlockId = Math.floorDiv(region.getStartPosition(), DPBinBlockManager.DP_BIN_BLOCK_SIZE);
        int endPosBlockId = Math.floorDiv(region.getEndPosition(), DPBinBlockManager.DP_BIN_BLOCK_SIZE);
        if (startPosBlockId == endPosBlockId) {
            return Integer.toString(startPosBlockId);
        } else {
            StringJoiner sj = new StringJoiner(",");
            for (int blockId = startPosBlockId; blockId <= endPosBlockId; blockId++) {
                sj.add(Integer.toString(blockId));
            }
            return sj.toString();
        }
    }

    private static HashMap<Integer, Integer> initSampleCoveredLengthMap(String strQuery,
            Region region, HashMap<Integer, Integer> sampleCoveredLengthMap) {
        try {
            ResultSet rs = DBManager.executeQuery(strQuery);
            while (rs.next()) {
                int blockId = rs.getInt("block_id");
                String dpStr = rs.getString("DP_string");
                ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(blockId, dpStr);

                for (CoverageInterval ci : cilist) {
                    int overlap = region.intersectLength(ci.getStartPos(), ci.getEndPos());
                    if (overlap > 0) {
                        int sample_id = rs.getInt("sample_id");
                        Integer coveredLength = sampleCoveredLengthMap.get(sample_id);
                        if (coveredLength != null) {
                            sampleCoveredLengthMap.put(sample_id, coveredLength + overlap);
                        } else {
                            sampleCoveredLengthMap.put(sample_id, overlap);
                        }
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return sampleCoveredLengthMap;
    }

    private static void initSiteCoverage(String strQuery, Region region,
            SiteCoverage siteCoverage) {
        try {
            ResultSet rs = DBManager.executeQuery(strQuery);
            while (rs.next()) {
                String dpStr = rs.getString("DP_string");
                int blockId = rs.getInt("block_id");
                int sampleId = rs.getInt("sample_id");
                ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(blockId, dpStr);
                for (CoverageInterval ci : cilist) {
                    Region cr = region.intersect(ci.getStartPos(), ci.getEndPos());
                    if (cr != null) {
                        for (int i = cr.getStartPosition(); i <= cr.getEndPosition(); i++) {
                            siteCoverage.addValue(SampleManager.getMap().get(sampleId).isCase(),
                                    i - region.getStartPosition());
                        }
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static ArrayList<CoverageInterval> getCoverageIntervalListByMinCoverage(
            int blockId, String dpBinStr) {

        StringBuilder sb = new StringBuilder();

        ArrayList<CoverageInterval> list = new ArrayList<>();

        int endIndex = 0;

        for (int pos = 0; pos < dpBinStr.length(); pos++) {
            char c = dpBinStr.charAt(pos);

            if (!DPBinBlockManager.getCoverageBin().containsKey(c)) {
                sb.append(c);
            } else {
                int startIndex = endIndex + 1;
                endIndex += Integer.parseInt(sb.toString(), 36);

                short dpBin = DPBinBlockManager.getCoverageByBin(c);

                if (dpBin >= GenotypeLevelFilterCommand.minCoverage) {
                    list.add(new CoverageInterval(blockId, startIndex, endIndex));
                }

                sb.setLength(0);
            }
        }

        return list;
    }
}
