package function.test;

import function.variant.base.RegionManager;
import global.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ConvertDPBins2 {

    private static String outputRaw = "/nfs/goldstein/datasets/tmp/dp_bin/DP_bins_chrREPLACE_CHR.txt";
    private static String outputConvert = "/nfs/goldstein/datasets/tmp/dp_bin/DP_bins_chrREPLACE_CHR_converted_nick.txt";

    private static HashSet<Character> binSet = new HashSet<>();

    private static BufferedWriter bwRaw;
    private static BufferedWriter bwConvert;

    private static final int BASE = 36;

    private static Pattern b_to_c_only_Pattern = Pattern.compile("^(?=.*[bc])(?!.*[d-g]).*");
    private static Pattern c_to_g_only_Pattern = Pattern.compile("^(?=.*[c-g])(?!.*[b]).*");
    private static Pattern d_to_g_Pattern = Pattern.compile("^(?=.*[d-g]).*");

    public static void init() throws Exception {
        binSet.add('b');
        binSet.add('c');
        binSet.add('d');
        binSet.add('e');
        binSet.add('f');
        binSet.add('g');

        if (RegionManager.chrInput.equals(Data.NO_FILTER_STR)) {
            ErrorManager.print("--chromosome is required input option.", ErrorManager.COMMAND_PARSING);
        }

        bwRaw = new BufferedWriter(new FileWriter(outputRaw.replace("REPLACE_CHR", RegionManager.chrInput)));
        bwConvert = new BufferedWriter(new FileWriter(outputConvert.replace("REPLACE_CHR", RegionManager.chrInput)));
    }

    public static void run() {
        try {
            init();

            String sql = "select * from DP_bins_chr" + RegionManager.chrInput;

            ResultSet rset = DBManager.executeConcurReadOnlyQuery(sql);

            while (rset.next()) {
                StringJoiner sjRaw = new StringJoiner("\t");
                StringJoiner sjConvert = new StringJoiner("\t");

                String sampleId = rset.getString("sample_id");
                String block_id = rset.getString("block_id");
                String DP_string = rset.getString("DP_string");

                if (DP_string == null) {
                    continue;
                }

                sjRaw.add(sampleId);
                sjRaw.add(block_id);
                sjRaw.add(DP_string);
                bwRaw.write(sjRaw.toString());
                bwRaw.newLine();

                sjConvert.add(sampleId);
                sjConvert.add(block_id);

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

                sjConvert.add(sbLine.toString());

                if (totalInterval != 1000) {
                    LogManager.writeAndPrint(DP_string);
                    LogManager.writeAndPrint(sjConvert.toString());
                }

                bwConvert.write(sjConvert.toString());
                bwConvert.newLine();
            }

            bwRaw.flush();
            bwRaw.close();
            bwConvert.flush();
            bwConvert.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    // b-c only or c-g only
    private static boolean checkPatterns(String DP_string, StringJoiner sj) throws IOException {
        if (b_to_c_only_Pattern.matcher(DP_string).matches()) {
            sj.add(DP_string);
            bwConvert.write(sj.toString());
            bwConvert.newLine();
            return true;
        } else if (c_to_g_only_Pattern.matcher(DP_string).matches()) {
            sj.add("RSc");
            bwConvert.write(sj.toString());
            bwConvert.newLine();
            return true;
        }

        return false;
    }

    private static String getBaseStr(int value) {
        return Integer.toString(value, BASE).toUpperCase();
    }
}
