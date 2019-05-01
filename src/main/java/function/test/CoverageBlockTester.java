package function.test;

import function.cohort.base.DPBinBlockManager;
import static function.cohort.base.DPBinBlockManager.getCoverageByBin;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author nick
 */
public class CoverageBlockTester {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        DPBinBlockManager.init();

        long start = System.currentTimeMillis();
        test("/Users/zr2180/Desktop/exome_binned_v1", "commabase");
        test("/Users/zr2180/Desktop/genome_binned_v1", "commabase");
        outputRuntime(start);

        start = System.currentTimeMillis();
        test("/Users/zr2180/Desktop/exome_binned_v2", "nocomma");
        test("/Users/zr2180/Desktop/genome_binned_v2", "nocomma");
        outputRuntime(start);

        start = System.currentTimeMillis();
        test("/Users/zr2180/Desktop/exome_binned_v3", "16base");
        test("/Users/zr2180/Desktop/genome_binned_v3", "16base");
        outputRuntime(start);

        start = System.currentTimeMillis();
        test("/Users/zr2180/Desktop/exome_binned_v4", "36base");
        test("/Users/zr2180/Desktop/genome_binned_v4", "36base");
        outputRuntime(start);
    }

    private static void test(String path, String option) throws FileNotFoundException, IOException {
        File dir = new File(path);

        int count = 0;

        File files[] = dir.listFiles();

        for (File file : files) {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                count++;

                String[] tmp = lineStr.split("\t");

                if (option.equals("commabase")) {
                    parseCoverageCommaBase(tmp[2]);
                } else if (option.equals("nocomma")) {
                    parseCoverageNoComma(tmp[2]);
                } else if (option.equals("16base")) {
                    parseCoverage16Base(tmp[2]);
                } else if (option.equals("36base")) {
                    parseCoverage36Base(tmp[2]);
                }
            }

            br.close();
            fr.close();
        }

        System.out.println(count);
    }

    private static int[][] parseCoverageCommaBase(String allCov) {
        String[] allCovArray = allCov.split(",");
        int[][] allCovBin = new int[allCovArray.length][2];
        int covBinPos = 0;

        for (int i = 0; i < allCovArray.length; i++) {
            int covBinLength = allCovArray[i].length();
            covBinPos += Integer.valueOf(allCovArray[i].substring(0, covBinLength - 1));
            allCovBin[i][0] = covBinPos;
            allCovBin[i][1] = getCoverageByBin(allCovArray[i].charAt(covBinLength - 1));
        }

        return allCovBin;
    }

    private static AllCovBin parseCoverageNoComma(String allCov) {
        List<Integer> pos = new ArrayList<>();
        List<Short> bin = new ArrayList<>();
        String binLengthStr = "";
        int covBinPos = 0;

        for (int i = 0; i < allCov.length(); i++) {
            char c = allCov.charAt(i);
            if (Character.isDigit(c)) {
                binLengthStr += c;
            } else {
                covBinPos += Integer.valueOf(binLengthStr);
                pos.add(covBinPos);
                bin.add(getCoverageByBin(c));
                binLengthStr = "";
            }
        }

        return new AllCovBin(pos, bin);
    }

    private static AllCovBin parseCoverage16Base(String allCov) {
        List<Integer> pos = new ArrayList<>();
        List<Short> bin = new ArrayList<>();
        String binLengthStr = "";
        int covBinPos = 0;

        for (int i = 0; i < allCov.length(); i++) {
            char c = allCov.charAt(i);
            if (c != 'a' && c != 'b' && c != 'c' && c != 'd' && c != 'e') {
                binLengthStr += c;
            } else {
                covBinPos += Integer.parseInt(binLengthStr, 16);
                pos.add(covBinPos);
                bin.add(getCoverageByBin(c));
                binLengthStr = "";
            }
        }

        return new AllCovBin(pos, bin);
    }

    private static AllCovBin parseCoverage36Base(String allCov) {
        List<Integer> pos = new ArrayList<>();
        List<Short> bin = new ArrayList<>();
        String binLengthStr = "";
        int covBinPos = 0;

        for (int i = 0; i < allCov.length(); i++) {
            char c = allCov.charAt(i);
            if (c != 'a' && c != 'b' && c != 'c' && c != 'd' && c != 'e') {
                binLengthStr += c;
            } else {
                covBinPos += Integer.parseInt(binLengthStr, 36);
                pos.add(covBinPos);
                bin.add(getCoverageByBin(c));
                binLengthStr = "";
            }
        }

        return new AllCovBin(pos, bin);
    }

    static class AllCovBin {

        List<Integer> pos;
        List<Short> bin;

        public AllCovBin(List<Integer> pos, List<Short> bin) {
            this.pos = pos;
            this.bin = bin;
        }
    }

    private static void outputRuntime(long start) {
        long total = System.currentTimeMillis() - start;

        System.out.println(TimeUnit.MILLISECONDS.toSeconds(total) + " seconds");
        System.out.println(TimeUnit.MILLISECONDS.toMillis(total) + " millis");
    }
}

//           try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
//                tempMap = stream
//                        .map(elem -> elem.split("\t"))
//                        .collect(Collectors.toMap(e -> e[0] + "-" + e[1] + "-" + chr, e -> parseCoverage(e[2])));
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
