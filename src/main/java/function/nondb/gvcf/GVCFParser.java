package function.nondb.gvcf;

import function.AnalysisBase;
import global.Data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class GVCFParser extends AnalysisBase {

    private static final String[] chrArray = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M"};
    private static Set<String> chrSet = new HashSet<>(Arrays.asList(chrArray));

    public static HashMap<String, BufferedWriter> dpBinChrBWMap = new HashMap<>();

    private static final String DP_LESS_THAN_10_BIN = "a";
    private static final String DP_LARGER_OR_EQUAL_THAN_10_BIN = "b";
    private static final int DP_BIN_BLOCK_LENGTH = 1000; // fixed 1000bp

    private static BufferedReader gvcfBR;

    @Override
    public void initOutput() {
        try {
            File f = new File(GVCFCommand.gvcfFilePath);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            gvcfBR = new BufferedReader(decoder);

            for (String chr : chrArray) {
                String outputFileName = CommonCommand.realOutputPath + File.separator + "DP_bins_chr" + chr;
                dpBinChrBWMap.put(chr, new BufferedWriter(new FileWriter(new File(outputFileName))));
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void closeOutput() {
        try {
            for (String chr : chrArray) {
                dpBinChrBWMap.get(chr).flush();
                dpBinChrBWMap.get(chr).close();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            Stack<String> dpBinStack = new Stack<>(); // per block dp bin stack
            String currentChr = Data.STRING_NA;
            int currentPos = Data.INTEGER_NA;
            int currentBlockId = Data.INTEGER_NA;
            int currentStartPos = Data.INTEGER_NA; // start pos of the block

            String lineStr = "";
            while ((lineStr = gvcfBR.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                // gvcf columns:
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

                String chr = tmp[0].replace("chr", "");
                int pos = Integer.parseInt(tmp[1]);
                String alt = tmp[4]; // w or wo <NON_REF>
                String info = tmp[7];
                String format = tmp[8];
                String formatValue = tmp[9];

                if (!chrSet.contains(chr)) {
                    continue;
                }

                // when reaching to new chr - complete the last block from previous chr
                if (!currentChr.equals(Data.STRING_NA)
                        && !currentChr.equals(chr)) {
                    int blockInteval = currentStartPos + DP_BIN_BLOCK_LENGTH - currentPos;
                    addDPBinToStack(dpBinStack, blockInteval + DP_LESS_THAN_10_BIN);
                    outputDPBin(currentChr, currentBlockId, dpBinStack);
                    currentPos = Data.INTEGER_NA;

                    currentBlockId = Data.INTEGER_NA;
                    currentStartPos = Data.INTEGER_NA;
                }

                // when reaching to new block - complete the previous block
                if (currentBlockId != Data.INTEGER_NA
                        && currentBlockId < Math.floorDiv(pos, DP_BIN_BLOCK_LENGTH)) {
                    int blockInteval = currentStartPos + DP_BIN_BLOCK_LENGTH - currentPos;
                    addDPBinToStack(dpBinStack, blockInteval + DP_LESS_THAN_10_BIN);
                    outputDPBin(currentChr, currentBlockId, dpBinStack);
                    currentPos = Data.INTEGER_NA;
                }

                currentBlockId = Math.floorDiv(pos, DP_BIN_BLOCK_LENGTH);
                currentStartPos = currentBlockId * DP_BIN_BLOCK_LENGTH;

                // when reaching to start pos of new chr 
                if (currentPos == Data.INTEGER_NA) {
                    int interval = pos - currentStartPos;
                    addDPBinToStack(dpBinStack, interval + DP_LESS_THAN_10_BIN);
                } // when reaching to next pos the current block
                else if (currentPos < pos) {
                    int interval = pos - currentPos;
                    addDPBinToStack(dpBinStack, interval + DP_LESS_THAN_10_BIN);
                }

                currentChr = chr;
                currentPos = pos;

                // non variant sites
                if (alt.equals("<NON_REF>")) {
                    int minDP = getMinDP(format, formatValue);
                    String bin = getBinByDP(minDP);

                    int endPos = Integer.valueOf(info.replace("END=", ""));
                    int interval = endPos - pos + 1;

                    // when interval covered more than one blocks
                    while (currentPos + interval >= currentStartPos + DP_BIN_BLOCK_LENGTH) {
                        int blockInteval = currentStartPos + DP_BIN_BLOCK_LENGTH - currentPos;
                        addDPBinToStack(dpBinStack, blockInteval + bin);
                        outputDPBin(currentChr, currentBlockId, dpBinStack);

                        interval = interval - blockInteval;
                        currentStartPos = currentStartPos + DP_BIN_BLOCK_LENGTH;
                        currentPos = currentStartPos;
                        currentBlockId++;
                    }

                    // process the last part of the original interval
                    if (interval != 0) {
                        addDPBinToStack(dpBinStack, interval + bin);
                        currentPos = endPos + 1;
                    }
                } // variants site
                else {
                    int dp = getDP(info);
                    String bin = getBinByDP(dp);

                    int interval = 1; // variant site always 1bp
                    if (currentPos + interval == currentStartPos + DP_BIN_BLOCK_LENGTH) {
                        addDPBinToStack(dpBinStack, interval + bin);
                        outputDPBin(currentChr, currentBlockId, dpBinStack);

                        currentStartPos = currentStartPos + DP_BIN_BLOCK_LENGTH;
                        currentPos = currentStartPos;
                        currentBlockId++;
                    } else {
                        addDPBinToStack(dpBinStack, interval + bin);
                        currentPos++;
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    // get MIN_DP from NON_REF sites
    private static int getMinDP(String format, String formatValue) {
        if (!format.split(":")[4].equals("MIN_DP")) {
            ErrorManager.print("MIN_DP order changed from FORMAT field: " + format, ErrorManager.UNEXPECTED_FAIL);
        }

        return Integer.valueOf(formatValue.split(":")[4]);
    }

    // get DP from variant site
    private static int getDP(String info) {
        if (!info.startsWith("DP=")) {
            ErrorManager.print("DP order changed from INFO field" + info, ErrorManager.UNEXPECTED_FAIL);
        }

        return Integer.valueOf(info.split(";")[0].split("=")[1]);
    }

    private static void outputDPBin(String chr, int blockId, Stack<String> dpBinStack) throws IOException {
        StringBuilder dpBinStrSB = new StringBuilder();

        int interval = 0;
        while (!dpBinStack.isEmpty()) {
            String bin = dpBinStack.remove(0);
            dpBinStrSB.append(bin);
            interval += Integer.valueOf(bin.substring(0, bin.length() - 1));
        }

        if (interval != DP_BIN_BLOCK_LENGTH) {
            ErrorManager.print("interval != 1000, blockId: " + blockId, ErrorManager.UNEXPECTED_FAIL);
        }

        String dpBinStr = dpBinStrSB.toString();
        if (!dpBinStr.equals("1000a")) { // skip to output if all site's DP < 10
            BufferedWriter bw = dpBinChrBWMap.get(chr);
            bw.write(String.valueOf(blockId));
            bw.write("\t");
            bw.write(dpBinStrSB.toString());
            bw.newLine();
        }
    }

    // add interval DP Bin to 1000bp DP Bin stack and potentially combined with previous DP Bin 
    private static void addDPBinToStack(Stack<String> dpBinStack, String currentIntervalDPBin) {
        if (dpBinStack.isEmpty()) {
            dpBinStack.add(currentIntervalDPBin);
        } else {
            String previousIntervalDPBin = dpBinStack.pop();

            char previousBin = previousIntervalDPBin.charAt(previousIntervalDPBin.length() - 1);
            char currentBin = currentIntervalDPBin.charAt(currentIntervalDPBin.length() - 1);

            // check: if current bin is the same as previous bin 
            // yes: merged two dp bins into current interval dp bin
            // no: add previous dp bin back to stack
            if (previousBin == currentBin) {
                int previousInterval = Integer.valueOf(previousIntervalDPBin.substring(0, previousIntervalDPBin.length() - 1));
                int currentInteval = Integer.valueOf(currentIntervalDPBin.substring(0, currentIntervalDPBin.length() - 1));

                int interval = previousInterval + currentInteval;
                currentIntervalDPBin = interval + String.valueOf(currentBin);
            } else {
                dpBinStack.add(previousIntervalDPBin);
            }

            dpBinStack.add(currentIntervalDPBin);
        }
    }

    private static String getBinByDP(int dp) {
        if (dp < 10) {
            return DP_LESS_THAN_10_BIN;
        } else {
            return DP_LARGER_OR_EQUAL_THAN_10_BIN;
        }
    }

    @Override
    public String toString() {
        return "Start running gvcf parser function";
    }
}
