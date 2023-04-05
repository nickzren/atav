package function.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ConvertDPBins {

    private static String input = "/nfs/goldstein/datasets/tmp/dp_bin/DP_bins_chr13.txt";
    private static String output = "/nfs/goldstein/datasets/tmp/dp_bin/DP_bins_chr13_converted_nick.txt";

    private static HashSet<Character> binSet = new HashSet<>();

    private static BufferedReader br;
    private static BufferedWriter bw;

    private static final int BASE = 36;

    private static Pattern b_to_c_only_Pattern = Pattern.compile("^(?=.*[bc])(?!.*[d-g]).*");
    private static Pattern c_to_g_only_Pattern = Pattern.compile("^(?=.*[c-g])(?!.*[b]).*");
    private static Pattern d_to_g_Pattern = Pattern.compile("^(?=.*[d-g]).*");
    private static Pattern b_to_g_Pattern = Pattern.compile("^(?=.*[b-g]).*");

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

    public static void main(String[] args) {
        input = "/Users/nick/Desktop/test.txt";
        output = "/Users/nick/Desktop/output.txt";

        run();
    }

    public static void run() {
        String lineStr = "";

        try {
            init();

            while ((lineStr = br.readLine()) != null) {
//                String[] tmp = lineStr.split("\t");

                StringJoiner sj = new StringJoiner("\t");
//                sj.add(tmp[0]);
//                sj.add(tmp[1]);

//                String DP_string = tmp[2];
                String DP_string = lineStr;

                if (DP_string == null) {
                    continue;
                }

                int previousInterval = 0;
                char previousBin = Character.MIN_VALUE;

                StringBuilder sbInterval = new StringBuilder();
                StringBuilder sbLine = new StringBuilder();
                int totalInterval = 0;
                int endPos = DP_string.length() - 1;

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
                                if (pos == endPos) {
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
                                if (pos == endPos) {
                                    sbLine.append(getBaseStr(inteval));
                                    sbLine.append(bin);
                                    totalInterval += inteval;
                                }
                            }
                        } else // bin >= c
                        {
                            bin = 'c';
                            if (previousBin == 'b') {
                                sbLine.append(getBaseStr(previousInterval));
                                sbLine.append(previousBin);
                                totalInterval += previousInterval;

                                // when it reach to the end pos and bin != previous bin
                                if (pos == endPos) {
                                    sbLine.append(getBaseStr(inteval));
                                    sbLine.append(bin);
                                    totalInterval += inteval;
                                }
                            } else {
                                previousInterval += inteval;
                                previousBin = bin;

                                // when it reach to the end pos and bin = previous bin
                                if (pos == endPos) {
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

                if (!binSet.contains(DP_string.charAt(endPos))) {                    
                    sbLine.append(getBaseStr(previousInterval));
                    sbLine.append(previousBin);
                    totalInterval += previousInterval;
                }

                // for debug problem cases only
                if (CommonCommand.isDebug) {
                    if (d_to_g_Pattern.matcher(sbLine.toString()).matches()) {
                        LogManager.writeAndPrint(DP_string);
                    }
                }

                sj.add(sbLine.toString());

                if (totalInterval != 1000) {
                    LogManager.writeAndPrint(DP_string);
                    LogManager.writeAndPrint(sj.toString());
                    
                    if (!b_to_g_Pattern.matcher(sbLine.toString()).matches()) {
                        continue;
                    }
                }

                bw.write(sj.toString());
                bw.newLine();
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            LogManager.writeAndPrint(lineStr);
            ErrorManager.send(e);
        }
    }

    // b-c only or c-g only
    private static boolean checkPatterns(String DP_string, StringJoiner sj) throws IOException {
        if (b_to_c_only_Pattern.matcher(DP_string).matches()) {
            sj.add(DP_string);
            bw.write(sj.toString());
            bw.newLine();
            return true;
        } else if (c_to_g_only_Pattern.matcher(DP_string).matches()) {
            sj.add("RSc");
            bw.write(sj.toString());
            bw.newLine();
            return true;
        }

        return false;
    }

    private static String getBaseStr(int value) {
        return Integer.toString(value, BASE).toUpperCase();
    }
}
