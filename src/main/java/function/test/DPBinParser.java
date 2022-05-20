package function.test;

import global.Data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class DPBinParser {

    private static String input = "/Users/nick/Desktop/gvcf/IGM-CUIMC-LMEC109B.hard-filtered.gvcf.gz";
    private static String output = "/Users/nick/Desktop/gvcf/IGM-CUIMC-LMEC109B.DP_bins.txt";

    private static BufferedReader br;
    private static BufferedWriter bw;

    public static void main(String[] args) {
        run();
    }

    public static void initReadWrite() throws Exception {
        File f = new File(input);
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
        Reader decoder = new InputStreamReader(in);
        br = new BufferedReader(decoder);

        bw = new BufferedWriter(new FileWriter(output));
    }

    public static void run() {
        try {
            initReadWrite();

            String lineStr = "";
            Stack<String> dpBinStack = new Stack<>(); // per block dp bin stack
            String currentChr = Data.STRING_NA;
            int currentPosition = Data.INTEGER_NA;
            int blockId = Data.INTEGER_NA;
            int startPos = Data.INTEGER_NA;
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                // #CHROM 0
                // POS    1
                // ID     2 
                // REF    3 
                // ALT    4 
                // QUAL   5 
                // FILTER 6 
                // INFO   7 
                // FORMAT 8 
                // FORMAT value
                String[] tmp = lineStr.split("\t");

                String chr = tmp[0];
                int pos = Integer.parseInt(tmp[1]);
                String alt = tmp[4];
                String info = tmp[7];
                String format = tmp[8];
                String formatValue = tmp[9];

//                if(chr.equals("chr1")) {
//                    continue;
//                }
//                
//                if (chr.equals("chr2") && pos == 10475) {
//                    System.out.println("");
//                }

                if (!currentChr.equals(chr)) {
                    currentPosition = Data.INTEGER_NA;
                    blockId = Data.INTEGER_NA;
                    startPos = Data.INTEGER_NA;
                }

                if (blockId != Data.INTEGER_NA
                        && blockId < Math.floorDiv(pos, 1000)) {
                    int blockInteval = startPos + 1000 - currentPosition;
                    addDPBinToStack(dpBinStack, blockInteval + "a");

                    outputDPBin(lineStr, blockId, dpBinStack);
                    currentPosition = Data.INTEGER_NA;
                }

                blockId = Math.floorDiv(pos, 1000);
                startPos = blockId * 1000;

                if (currentPosition == Data.INTEGER_NA) {
                    if (pos > startPos) {
                        int interval = pos - startPos;
                        addDPBinToStack(dpBinStack, interval + "a");
                    }
                } else if (currentPosition < pos) {
                    int interval = pos - currentPosition;
                    addDPBinToStack(dpBinStack, interval + "a");
                }

                currentChr = chr;
                currentPosition = pos;

                if (alt.equals("<NON_REF>")) {
                    // NON_REF sites only

                    if (!format.split(":")[4].equals("MIN_DP")) {
                        System.out.println("MIN_DP order changed from FORMAT field");
                        System.out.println(lineStr);
                        System.exit(1);
                    }

                    int minDP = Integer.valueOf(formatValue.split(":")[4]);
                    String bin = getBinByDP(minDP);

                    int end = Integer.valueOf(info.replace("END=", ""));
                    int interval = end - pos + 1;

                    while (currentPosition + interval >= startPos + 1000) {
                        int blockInteval = startPos + 1000 - currentPosition;
                        addDPBinToStack(dpBinStack, blockInteval + bin);

                        outputDPBin(lineStr, blockId, dpBinStack);

                        interval = interval - blockInteval;
                        startPos = startPos + 1000;
                        currentPosition = startPos;
                        blockId += 1;
                    }

                    if (interval != 0) {
                        addDPBinToStack(dpBinStack, interval + bin);
                        currentPosition = end + 1;
                    }
                } else {
                    // variants site
                    if (!info.startsWith("DP=")) {
                        System.out.println("DP order changed from INFO field");
                        System.out.println(lineStr);
                        System.exit(1);
                    }

                    String dpStr = info.split(";")[0]; // DP=XX
                    int dp = Integer.valueOf(dpStr.split("=")[1]);

                    String bin = getBinByDP(dp);

                    int interval = 1;
                    while (currentPosition + interval >= startPos + 1000) {
                        int blockInteval = startPos + 1000 - currentPosition;
                        addDPBinToStack(dpBinStack, blockInteval + bin);

                        outputDPBin(lineStr, blockId, dpBinStack);

                        interval = interval - blockInteval;
                        startPos = startPos + 1000;
                        currentPosition = startPos;
                        blockId += 1;
                    }

                    if (interval != 0) {
                        addDPBinToStack(dpBinStack, interval + bin);
                        currentPosition++;
                    }
                }

//                if (blockId == 137) {
//                    break;
//                }
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void outputDPBin(String lineStr, int blockId, Stack<String> dpBinStack) throws IOException {
        StringBuilder dpBinStrSB = new StringBuilder();

        int interval = 0;
        while (!dpBinStack.isEmpty()) {
            String bin = dpBinStack.remove(0);
            interval += Integer.valueOf(bin.substring(0, bin.length() - 1));
            dpBinStrSB.append(bin);
        }

        String dpBinStr = dpBinStrSB.toString();

        if (!dpBinStr.equals("1000a")) {
            bw.write(String.valueOf(blockId));
            bw.write("\t");
            bw.write(dpBinStrSB.toString());
            bw.newLine();
        }

        if (interval != 1000) {
            System.out.println(lineStr);
            bw.flush();
            bw.close();
            System.exit(1);
        }
    }

    public static void addDPBinToStack(Stack<String> dpBinStack, String intervalDPBin) {
        if (dpBinStack.isEmpty()) {
            dpBinStack.add(intervalDPBin);
        } else {
            String bin = dpBinStack.pop();

            if (bin.charAt(bin.length() - 1) == intervalDPBin.charAt(intervalDPBin.length() - 1)) {
                int interval = Integer.valueOf(bin.substring(0, bin.length() - 1))
                        + Integer.valueOf(intervalDPBin.substring(0, intervalDPBin.length() - 1));
                intervalDPBin = interval + intervalDPBin.substring(intervalDPBin.length() - 1);
            } else {
                dpBinStack.add(bin);
            }

            dpBinStack.add(intervalDPBin);
        }
    }

    public static String getBinByDP(int dp) {
        if (dp < 10) {
            return "a";
        } else {
            return "b";
        }
    }
}
