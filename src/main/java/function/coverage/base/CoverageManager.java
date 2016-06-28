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
        HashMap<Integer, Integer> sampleCoveredLengthMap = new HashMap<>();

        String strQuery = getCoverageString(Index.GENOME, region);
        CoverageManager.initSampleCoveredLengthMap(strQuery, region, sampleCoveredLengthMap);

        strQuery = getCoverageString(Index.EXOME, region);
        CoverageManager.initSampleCoveredLengthMap(strQuery, region, sampleCoveredLengthMap);

        return sampleCoveredLengthMap;
    }

    public static SiteCoverage getSiteCoverage(Region region) {
        SiteCoverage siteCoverage = new SiteCoverage(region.getLength());

        String strQuery = getCoverageString(Index.GENOME, region);
        CoverageManager.initSiteCoverage(strQuery, region, siteCoverage);

        strQuery = getCoverageString(Index.EXOME, region);
        CoverageManager.initSiteCoverage(strQuery, region, siteCoverage);

        return siteCoverage;
    }

    public static String getCoverageString(int sampleTypeIndex, Region region) {
        String str = "SELECT sample_id, position, min_coverage FROM "
                + "_SAMPLE_TYPE__read_coverage_1024_chr_CHROM_ c ,"
                + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE position in (_POSITIONS_) "
                + "AND c.sample_id = t.id ";

        str = str.replaceAll("_SAMPLE_TYPE_", SampleManager.SAMPLE_TYPE[sampleTypeIndex]);
        str = str.replaceAll("_CHROM_", region.getChrStr());
        str = str.replaceAll("_POSITIONS_", getPositionString(region));
        return str;
    }

    private static int getPosition(int pos) { //optimize it later
        int posIndex = pos % CoverageBlockManager.COVERAGE_BLOCK_SIZE; // coverage data block size is 1024
        if (posIndex == 0) {
            posIndex = CoverageBlockManager.COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }
        return pos - posIndex + CoverageBlockManager.COVERAGE_BLOCK_SIZE;
    }

    private static String getPositionString(Region region) {
        int firstIndex = getPosition(region.getStartPosition());
        int lastIndex = getPosition(region.getEndPosition());
        if (firstIndex == lastIndex) {
            return Integer.toString(firstIndex);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int index = firstIndex; index < lastIndex; index += CoverageBlockManager.COVERAGE_BLOCK_SIZE) {
                sb.append(index).append(",");
            }
            sb.append(lastIndex);
            return sb.toString();
        }
    }

    private static HashMap<Integer, Integer> initSampleCoveredLengthMap(String strQuery,
            Region region, HashMap<Integer, Integer> sampleCoveredLengthMap) {
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
                String strCoverage = rs.getString("min_coverage");
                int position = rs.getInt("position");
                int sampleid = rs.getInt("sample_id");
                ArrayList<CoverageInterval> cilist = getCoverageIntervalListByMinCoverage(position, strCoverage);
                for (CoverageInterval ci : cilist) {
                    Region cr = region.intersect(ci.getStartPos(), ci.getEndPos());
                    if (cr != null) {
                        for (int i = cr.getStartPosition(); i <= cr.getEndPosition(); i++) {
                            siteCoverage.addValue(
                                    SampleManager.getMap().get(sampleid).isCase(),
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
