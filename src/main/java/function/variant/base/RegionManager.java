package function.variant.base;

import utils.ErrorManager;
import utils.CommonCommand;
import utils.LogManager;
import global.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author nick
 */
public class RegionManager {

    private static ArrayList<Region> regionList = new ArrayList<>();
    private static ArrayList<String> chrList = new ArrayList<>();
    private static boolean isUsed = false;

    public static final String[] ALL_CHR = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"};
    
    public static void init() {
        if (CommonCommand.isNonDBAnalysis) {
            return;
        }

        if (CommonCommand.regionInput.isEmpty()) {
            initChrRegionList(ALL_CHR);
        } else {
            isUsed = true;

            File f = new File(CommonCommand.regionInput);

            if (CommonCommand.regionInput.equals("all")) {
                initChrRegionList(ALL_CHR);
            } else if (f.isFile()) {
                initMultiChrRegionList(f);
            } else {
                CommonCommand.regionInput = CommonCommand.regionInput.toLowerCase();
                initChrRegionList(CommonCommand.regionInput.split(","));
            }
        }

        sortRegionList();
    }

    public static void sortRegionList() {
        Collections.sort(regionList);
    }

    public static void clear() {
        regionList.clear();
        chrList.clear();
    }

    public static boolean isUsed() {
        return isUsed;
    }

    public static int getRegionSize() {
        return regionList.size();
    }

    public static void initRegionList(String[] list) {
        for (String regionStr : list) {
            Region region = getRegion(regionStr);

            if (!isRegionListContained(region)) {
                regionList.add(region);
                addChrList(region.getChrStr());
            }
        }
    }

    public static void initChrRegionList(String[] list) {
        for (String chr : list) {
            initOneChrRegionList(chr);
        }
    }

    private static void initOneChrRegionList(String chr) {
        add2RegionList(chr);
    }

    private static void add2RegionList(String str) {
        Region region = getRegion(str);

        if (!isRegionListContained(region)) {
            regionList.add(region);
            addChrList(region.getChrStr());
        }
    }

    private static boolean isRegionListContained(Region r) {
        for (Region region : regionList) {
            if (region.toString().equals(r.toString())) {
                return true;
            }
        }

        return false;
    }

    private static void initMultiChrRegionList(File f) {
        String lineStr = "";
        int lineNum = 0;

        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            ArrayList<String> chrList = new ArrayList<>();

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                lineStr = lineStr.replaceAll("( )+", "").toLowerCase();

                if (!lineStr.contains(":")) {
                    String[] values = lineStr.split("\t");
                    if (values.length > 1) {
                        lineStr = values[0] + ":" + values[1] + "-" + values[2];
                    }
                }

                if (!chrList.contains(lineStr)) {
                    chrList.add(lineStr);
                    add2RegionList(lineStr);
                }
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in region file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static Region getRegion(String str) {
        str = str.toUpperCase().replace("CHR", "");

        String[] tmp = str.split(":");

        String chr = getChr(tmp[0]);

        int start = Data.INTEGER_NA, end = Data.INTEGER_NA;

        try {
            if (tmp.length == 2) {
                tmp = tmp[1].split("-");

                if (tmp.length == 2) {
                    start = Integer.valueOf(tmp[0]);
                    end = Integer.valueOf(tmp[1]);
                } else {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            ErrorManager.print("Invalid region: " + str, ErrorManager.INPUT_PARSING);
        }

        return new Region(chr, start, end);
    }

    private static String getChr(String chr) {
        checkChrValid(chr);

        return chr;
    }

    public static void checkChrValid(String chr) {
        boolean isValid = false;
        for (String str : ALL_CHR) {
            if (chr.equalsIgnoreCase(str)) {
                isValid = true;
            }
        }

        if (!isValid) {
            ErrorManager.print("Invalid chr: " + chr, ErrorManager.INPUT_PARSING);
        }
    }

    public static Region getRegion(int index) {
        System.out.println("Analysing variants in region " + regionList.get(index) + "\n");

        return regionList.get(index);
    }

    public static void addRegionByVariantPos(String varPos) {
        String[] values = varPos.split("-");
        String chr = values[0];
        int pos = Integer.valueOf(values[1]);
        regionList.add(new Region(chr, pos, pos));
        addChrList(chr);
    }

    private static void addChrList(String chr) {
        if (!chrList.contains(chr)) {
            chrList.add(chr);
        }
    }

    public static ArrayList<String> getChrList() {
        return chrList;
    }
}
