package function.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ConvertDPBins {

    private static String input = "/nfs/goldstein/datasets/tmp/DP_bins_chr21.txt";
    private static String output = "/nfs/goldstein/datasets/tmp/DP_bins_10_only_base36_chr21.txt";
//    private static String input = "/Users/nick/Desktop/DP_bins_chr21.txt";
//    private static String output = "/Users/nick/Desktop/DP_bins_10_only_base36_chr21.txt";

    private static HashSet<Character> binSet = new HashSet<>();

    private static BufferedReader br;
    private static BufferedWriter bw;

    private static final int BASE = 36;

    public static void init() throws Exception {
        binSet.add('b');
        binSet.add('c');
        binSet.add('d');
        binSet.add('e');
        binSet.add('f');
        binSet.add('g');

        br = new BufferedReader(new FileReader(new File(input)));
        bw = new BufferedWriter(new FileWriter(output));
    }

    public static void run() {
        try {
            init();

            String lineStr = "";

            while ((lineStr = br.readLine()) != null) {
                String[] tmp = lineStr.split("\t");

                StringJoiner sj = new StringJoiner("\t");
                sj.add(tmp[0]);
                sj.add(tmp[1]);

                String DP_string = tmp[2];

                int previousInterval = 0;
                char previousBin = Character.MIN_VALUE;

                StringBuilder sbInterval = new StringBuilder();
                StringBuilder sbLine = new StringBuilder();
                int totalInterval = 0;

                for (int pos = 0; pos < DP_string.length(); pos++) {
                    char bin = DP_string.charAt(pos);
                    if (!binSet.contains(bin)) {
                        sbInterval.append(bin);
                    } else {
                        int inteval = Integer.parseInt(sbInterval.toString(), 36);
                        sbInterval.setLength(0); // clear StringBuilder

                        if (bin == 'b') {
                            if (previousBin == 'b') {
                                previousInterval += inteval;

                                // when it reach to the end pos and bin = previous bin
                                if (pos == DP_string.length() - 1) {
                                    sbLine.append(getBaseStr(previousInterval));
                                    sbLine.append(previousBin);
                                    totalInterval += previousInterval;
                                }

                                continue;
                            } else if (previousInterval != Character.MIN_VALUE) {
                                sbLine.append(getBaseStr(previousInterval));
                                sbLine.append(previousBin);
                                totalInterval += previousInterval;

                                // when it reach to the end pos and bin != previous bin
                                if (pos == DP_string.length() - 1) {
                                    sbLine.append(getBaseStr(inteval));
                                    sbLine.append(bin);
                                    totalInterval += inteval;
                                }
                            }
                        } else // bin >= c
                        {
                            if (previousBin == 'b') {
                                sbLine.append(getBaseStr(previousInterval));
                                sbLine.append(previousBin);
                                totalInterval += previousInterval;

                                // when it reach to the end pos and bin != previous bin
                                if (pos == DP_string.length() - 1) {
                                    sbLine.append(getBaseStr(inteval));
                                    sbLine.append(bin);
                                    totalInterval += inteval;
                                }
                            } else {
                                previousInterval += inteval;
                                previousBin = 'c';

                                // when it reach to the end pos and bin = previous bin
                                if (pos == DP_string.length() - 1) {
                                    sbLine.append(getBaseStr(previousInterval));
                                    sbLine.append(previousBin);
                                    totalInterval += previousInterval;
                                }

                                continue;
                            }
                        }

                        previousInterval = inteval;
                        previousBin = bin;
                    }
                }

                sj.add(sbLine.toString());
                
                if (totalInterval != 1000) {
                    LogManager.writeAndPrint(lineStr);
                    LogManager.writeAndPrint(sj.toString());
                }

                bw.write(sj.toString());
                bw.newLine();
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    private static String getBaseStr(int value) {
        return Integer.toString(value, BASE).toUpperCase();
    }
}
