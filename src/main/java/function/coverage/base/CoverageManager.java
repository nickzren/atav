package function.coverage.base;

import function.coverage.comparison.SiteCoverageComparison;
import function.genotype.base.DPBinBlockManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import function.variant.base.Region;
import global.Data;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

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
        String str = "SELECT * FROM DP_bins_chr" + region.getChrStr() + " use index(primary),"
                + SampleManager.TMP_SAMPLE_ID_TABLE
                + " WHERE block_id in (" + getBlockIdStr(region) + ")"
                + " AND sample_id = input_sample_id ";

        if (GenotypeLevelFilterCommand.minGQBin != Data.NO_FILTER) {
            str = "SELECT * FROM GQ_bins_chr" + region.getChrStr() + " use index(primary),"
                    + SampleManager.TMP_SAMPLE_ID_TABLE
                    + " WHERE block_id in (" + getBlockIdStr(region) + ")"
                    + " AND sample_id = input_sample_id ";
        }

        return str;
    }

    private static String getBlockIdStr(Region region) {
        int startPosBlockId;
        int endPosBlockId;

        if (GenotypeLevelFilterCommand.minGQBin != Data.NO_FILTER) {
            startPosBlockId = Math.floorDiv(region.getStartPosition(), DPBinBlockManager.GQ_BIN_BLOCK_SIZE);
            endPosBlockId = Math.floorDiv(region.getEndPosition(), DPBinBlockManager.GQ_BIN_BLOCK_SIZE);
        } else {
            startPosBlockId = Math.floorDiv(region.getStartPosition(), DPBinBlockManager.DP_BIN_BLOCK_SIZE);
            endPosBlockId = Math.floorDiv(region.getEndPosition(), DPBinBlockManager.DP_BIN_BLOCK_SIZE);
        }

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
                String dpStr;
                if (GenotypeLevelFilterCommand.minGQBin != Data.NO_FILTER) {
                    dpStr = rs.getString("GQ_string");
                } else {
                    dpStr = rs.getString("DP_string");
                }

                ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(
                        rs.getInt("sample_id"), region.getChrStr(), blockId, dpStr);

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
                String dpStr;
                if (GenotypeLevelFilterCommand.minGQBin != Data.NO_FILTER) {
                    dpStr = rs.getString("GQ_string");
                } else {
                    dpStr = rs.getString("DP_string");
                }

                int blockId = rs.getInt("block_id");
                int sampleId = rs.getInt("sample_id");
                ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(
                        sampleId, region.getChrStr(), blockId, dpStr);
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
            int sampleId, String chr, int blockId, String dpBinStr) throws IOException {

        StringBuilder sb = new StringBuilder();

        ArrayList<CoverageInterval> list = new ArrayList<>();

        int endIndex = 0;

        StringBuilder siteSB = new StringBuilder();

        for (int pos = 0; pos < dpBinStr.length(); pos++) {
            char c = dpBinStr.charAt(pos);

            boolean isContained = false;

            if (GenotypeLevelFilterCommand.minGQBin == Data.NO_FILTER) {
                isContained = !DPBinBlockManager.getCoverageBin().containsKey(c);
            } else {
                isContained = !DPBinBlockManager.getGQbin().containsKey(c);
            }

            if (isContained) {
                sb.append(c);
            } else {
                int startIndex = endIndex + 1;
                endIndex += Integer.parseInt(sb.toString(), 36);

                if (GenotypeLevelFilterCommand.minGQBin == Data.NO_FILTER) {
                    short dpBin = DPBinBlockManager.getCoverageByBin(c);

                    if (dpBin >= GenotypeLevelFilterCommand.minCoverage) {
                        list.add(new CoverageInterval(blockId, startIndex, endIndex));
                    } else if (CoverageCommand.isIncludePrunedSite) {
                        siteSB.append(sampleId).append(",");
                        siteSB.append(chr).append(",");
                        siteSB.append(blockId).append(",");
                        siteSB.append(blockId * 1000 + startIndex).append(",");
                        siteSB.append(endIndex).append(",");
                        siteSB.append(FormatManager.getShort(dpBin));
                        SiteCoverageComparison.bwSitePruned.write(siteSB.toString());
                        SiteCoverageComparison.bwSitePruned.newLine();
                        siteSB.setLength(0);
                    }
                } else {
                    short gqBin = DPBinBlockManager.getGQbinByBin(c);

                    if (gqBin >= GenotypeLevelFilterCommand.minGQBin) {
                        list.add(new CoverageInterval(blockId, startIndex, endIndex));
                    } else if (CoverageCommand.isIncludePrunedSite) {
                        siteSB.append(sampleId).append(",");
                        siteSB.append(chr).append(",");
                        siteSB.append(blockId).append(",");
                        siteSB.append(startIndex).append(",");
                        siteSB.append(endIndex).append(",");
                        siteSB.append(FormatManager.getShort(gqBin));
                        SiteCoverageComparison.bwSitePruned.write(siteSB.toString());
                        SiteCoverageComparison.bwSitePruned.newLine();
                        siteSB.setLength(0);
                    }
                }

                sb.setLength(0);
            }
        }

        return list;
    }
}
