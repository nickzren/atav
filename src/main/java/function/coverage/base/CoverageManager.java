package function.coverage.base;

import function.genotype.base.CoverageBlockManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import function.variant.base.Region;
import global.Index;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class CoverageManager {

    public static HashMap<Integer, Integer> getSampleCoveredLengthMap(Region region) {
        String strQuery = getCoverageString(Index.GENOME, region);
        HashMap<Integer, Integer> result = CoverageManager.getSampleCoveredLengthMap(strQuery, region);

        strQuery = getCoverageString(Index.EXOME, region);
        result.putAll(CoverageManager.getSampleCoveredLengthMap(strQuery, region));

        return result;
    }

    public static int[][] getSampleSiteCoverage(Region region) {
        String strQuery = getCoverageString(Index.GENOME, region);
        int[][] result = CoverageManager.calculateCoverageForSites(strQuery, region);

        strQuery = getCoverageString(Index.EXOME, region);
        int[][] result1 = CoverageManager.calculateCoverageForSites(strQuery, region);

        for (int pos = 0; pos < region.getLength(); pos++) {
            result[Index.CASE][pos] += result1[Index.CASE][pos];
            result[Index.CTRL][pos] += result1[Index.CTRL][pos];
        }

        return result;
    }

    public static String getCoverageString(int sampleTypeIndex, Region region) {
        if (region.chrStr.length() > 2) {
            return "";
        } else {
            String str = "SELECT sample_id, position, min_coverage FROM "
                    + "_SAMPLE_TYPE__read_coverage_1024_chr_CHROM_ c ,"
                    + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                    + "WHERE position in (_POSITIONS_) "
                    + "AND c.sample_id = t.id ";

            str = str.replaceAll("_SAMPLE_TYPE_", SampleManager.SAMPLE_TYPE[sampleTypeIndex]);
            str = str.replaceAll("_CHROM_", region.chrStr);
            str = str.replaceAll("_POSITIONS_", getPositionString(region));
            return str;
        }
    }

    private static int getPosition(int pos) { //optimize it later
        int posIndex = pos % CoverageBlockManager.COVERAGE_BLOCK_SIZE; // coverage data block size is 1024
        if (posIndex == 0) {
            posIndex = CoverageBlockManager.COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }
        return pos - posIndex + CoverageBlockManager.COVERAGE_BLOCK_SIZE;
    }

    private static String getPositionString(Region region) {
        int firstIndex = getPosition(region.startPosition);
        int lastIndex = getPosition(region.endPosition);
        if (firstIndex == lastIndex) {
            return Integer.toString(firstIndex);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int index = firstIndex; index < lastIndex; index += CoverageBlockManager.COVERAGE_BLOCK_SIZE) {
                sb.append(Integer.toString(index)).append(",");
            }
            sb.append(Integer.toString(lastIndex));
            return sb.toString();
        }
    }

    private static HashMap<Integer, Integer> getSampleCoveredLengthMap(String strQuery, Region region) {
        HashMap<Integer, Integer> result = new HashMap<>();

        if (!strQuery.isEmpty()) {
            try {
                ResultSet rs = DBManager.executeQuery(strQuery);
                while (rs.next()) {
                    String strCoverage = rs.getString("min_coverage");
                    int position = rs.getInt("position");
                    ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(position, strCoverage);

                    for (CoverageInterval ci : cilist) {
                        int overlap = region.intersectLength(ci.getStartPos(), ci.getEndPos());
                        if (overlap > 0) {
                            int sample_id = rs.getInt("sample_id");
                            if (result.containsKey(sample_id)) {
                                result.put(sample_id, result.get(sample_id) + overlap);
                            } else {
                                result.put(sample_id, overlap);
                            }
                        }
                    }

                }
                rs.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
        return result;
    }

    private static int[][] calculateCoverageForSites(String strQuery, Region region) {
        int[][] result = new int[2][region.getLength()];

        if (!strQuery.isEmpty()) {
            try {
                ResultSet rs = DBManager.executeQuery(strQuery);
                while (rs.next()) {
                    String strCoverage = rs.getString("min_coverage");
                    int position = rs.getInt("position");
                    int sampleid = rs.getInt("sample_id");
                    ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(position, strCoverage);
                    for (CoverageInterval ci : cilist) {
                        Region cr = region.intersect(ci.getStartPos(), ci.getEndPos());
                        if (cr != null) {
                            for (int i = cr.getStartPosition(); i <= cr.getEndPosition(); i++) {
                                if (SampleManager.getMap().get(sampleid).isCase()) {
                                    result[Index.CASE][i - region.getStartPosition()]++;
                                } else {
                                    result[Index.CTRL][i - region.getStartPosition()]++;
                                }
                            }
                        }
                    }
                }
                rs.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }

        return result;
    }

    private static ArrayList<CoverageInterval> getCoverageIntervalListByMinCoverage(
            int sampleBlockPos, String sampleCovBinStr) {
        String[] allCovBinArray = sampleCovBinStr.split(",");

        ArrayList<CoverageInterval> list = new ArrayList<>();

        int endIndex = 0;

        for (String oneCovBinStr : allCovBinArray) {
            String oneCovBinLength = oneCovBinStr.substring(0, oneCovBinStr.length() - 1);
            int startIndex = endIndex + 1;
            endIndex += Integer.valueOf(oneCovBinLength);
            char covStr = oneCovBinStr.charAt(oneCovBinStr.length() - 1);
            int cov = CoverageBlockManager.getCoverageByBin(covStr);
            if (cov >= GenotypeLevelFilterCommand.minCoverage) {
                list.add(new CoverageInterval(sampleBlockPos, startIndex, endIndex));
            }
        }

        return list;
    }
}
