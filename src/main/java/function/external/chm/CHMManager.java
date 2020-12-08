package function.external.chm;

import function.variant.base.RegionManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nick
 */
public class CHMManager {

    private static String repeatRegionBedFilePath = Data.ATAV_HOME + "data/CHM-eval/CHR_um75-hs37d5.bed.gz";
    private static HashMap<String, int[][]> repeatRegionByChrMap = new HashMap<>();

    public static String getHeader() {
        return "Repeat Region";
    }

    public static void init() throws Exception {
        if (CHMCommand.isFlag || CHMCommand.isExclude) {
            for (String chr : RegionManager.getChrList()) {
                Path path = Paths.get(repeatRegionBedFilePath.replace("CHR", chr));

                int count = getGzipFileRowCount(path.toFile());
                repeatRegionByChrMap.put(chr, new int[count][2]);

                GZIPInputStream in = new GZIPInputStream(new FileInputStream(path.toFile()));
                Reader decoder = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(decoder);

                String lineStr = "";
                int index = 0;
                br = new BufferedReader(decoder);
                while ((lineStr = br.readLine()) != null) {
                    String[] tmp = lineStr.split("\t");

                    int start = Integer.valueOf(tmp[1]);
                    int end = Integer.valueOf(tmp[2]);

                    repeatRegionByChrMap.get(chr)[index][0] = start;
                    repeatRegionByChrMap.get(chr)[index++][1] = end;
                }

                br.close();
                decoder.close();
                in.close();
            }
        }
    }

    private static int getGzipFileRowCount(File file) throws FileNotFoundException, IOException {
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(file));
        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);

        int count = (int) br.lines().count();

        br.close();
        decoder.close();
        in.close();

        return count;
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
