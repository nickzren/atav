package function.external.chm;

import function.variant.base.RegionManager;
import global.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class CHMManager {

    private static String repeatRegionBedFilePath = Data.ATAV_HOME + "data/CHM-eval/CHR_um75-hs37d5.bed";
    private static HashMap<String, int[][]> repeatRegionByChrMap = new HashMap<>();

    public static String getHeader() {
        return "Repeat Region";
    }

    public static void init() throws Exception {
        if (CHMCommand.isFlagRepeatRegion || CHMCommand.isExcludeRepeatRegion) {
            for (String chr : RegionManager.getChrList()) {
                Path path = Paths.get(repeatRegionBedFilePath.replace("CHR", chr));
                int count = (int) Files.lines(path).count();

                repeatRegionByChrMap.put(chr, new int[count][2]);

                BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
                String lineStr = "";
                int index = 0;
                while ((lineStr = br.readLine()) != null) {
                    String[] tmp = lineStr.split("\t");

                    int start = Integer.valueOf(tmp[1]);
                    int end = Integer.valueOf(tmp[2]);

                    repeatRegionByChrMap.get(chr)[index][0] = start;
                    repeatRegionByChrMap.get(chr)[index++][1] = end;
                }
            }
        }
    }

    public static boolean isRepeatRegion(String chr, int pos) {
        int[][] repeatRegionArray = repeatRegionByChrMap.get(chr);
        int start = 0;
        int last = repeatRegionArray.length - 1;
        pos -= 1; // variant position is 1-based wherease the repeat region bed file is 0-based

        return binarySearch(repeatRegionArray, start, last, pos) != Data.INTEGER_NA;
    }

    /*
        binary search whether a given position fall into a repeat region
     */
    private static int binarySearch(int arr[][], int first, int last, int pos) {
        if (last >= first) {
            int mid = first + (last - first) / 2;

            int start = arr[mid][0];
            int end = arr[mid][1];
            
            if (pos < start) {
                return binarySearch(arr, first, mid - 1, pos); //search in left subarray  
            } else if (pos >= end) {
                return binarySearch(arr, mid + 1, last, pos); //search in right subarray  
            } else {
                return mid;
            }
        }

        return Data.INTEGER_NA;
    }
}
