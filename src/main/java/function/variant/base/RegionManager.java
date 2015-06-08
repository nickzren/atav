package function.variant.base;

import utils.FormatManager;
import utils.DBManager;
import utils.ErrorManager;
import utils.CommandValue;
import utils.LogManager;
import temp.TempRange;
import function.variant.base.Region;
import global.Data;
import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import function.genotype.base.SampleManager;

/**
 *
 * @author nick
 */
public class RegionManager {

    private static ArrayList<Region> regionList = new ArrayList<Region>();
    private static HashMap<Integer, String> idChrMap = new HashMap<Integer, String>();
    private static HashMap<String, Integer> chrIdMap = new HashMap<String, Integer>();
    private static boolean isUsed = false;

    // temp hack solution - phs000473 coverage restriction
    public static HashMap<String, ArrayList<TempRange>> phs000473RegionMap
            = new HashMap<String, ArrayList<TempRange>>();

    public static void init() {
        initIdChrMap();

        CommandValue.regionInput.replaceAll("( )+", "");

        if (CommandValue.regionInput.isEmpty()) {
            initChrRegionList(Data.ALL_CHR);
        } else {
            isUsed = true;

            File f = new File(CommandValue.regionInput);

            if (CommandValue.regionInput.equals("all")) {
                initChrRegionList(Data.ALL_CHR);
            } else if (f.isFile()) {
                initMultiChrRegionList(f);
            } else {
                CommandValue.regionInput = CommandValue.regionInput.toLowerCase();
                initChrRegionList(CommandValue.regionInput.split(","));
            }
        }

        sortRegionList();

        initPhs000473RegionMap();
    }

    private static void initPhs000473RegionMap() {
        if (SampleManager.phs000473SampleIdSet.isEmpty()) {
            return;
        }

        File f = new File(Data.phs000473_SAMPLE_REGION_PATH);

        for (String chr : Data.ALL_CHR) {
            phs000473RegionMap.put(chr, new ArrayList<TempRange>());
        }

        String lineStr = "";

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                String[] values = lineStr.split("\t");

                TempRange range = new TempRange();
                range.start = Integer.valueOf(values[1]);
                range.end = Integer.valueOf(values[2]);

                phs000473RegionMap.get(values[0]).add(range);
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void sortRegionList() {
        Collections.sort(regionList);
    }

    public static void clear() {
        regionList.clear();
    }

    public static boolean isUsed() {
        return isUsed;
    }

    public static int getRegionSize() {
        return regionList.size();
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
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            ArrayList<String> chrList = new ArrayList<String>();

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                lineStr = lineStr.replaceAll("( )+", "").toLowerCase();

                if (lineStr.indexOf(":") == -1) {
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
            in.close();
            fstream.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in region file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static Region getRegion(String str) {
        int start = 0, end = 0;
        String chr;
        int index_1 = str.indexOf(":");
        if (index_1 != -1) {
            chr = str.substring(0, index_1);
            String range = str.substring(index_1 + 1);
            int index_2 = range.indexOf("-");
            if (index_2 != -1) {
                start = Integer.parseInt(range.substring(0, index_2));
                if (FormatManager.isDigit(range.substring(index_2 + 1))) {
                    end = Integer.parseInt(range.substring(index_2 + 1));
                }
            } else {
                if (FormatManager.isDigit(range)) {
                    start = Integer.parseInt(range);
                }
            }
        } else {
            chr = str;
        }

        if (!chr.startsWith("chr")) {
            chr = "chr" + chr;
        }

        if (chr.equalsIgnoreCase("chrXY")) {
            chr = "chrX";
        }

        checkChrValid(chr);

        return new Region(chr, start, end);
    }

    private static void checkChrValid(String chr) {
        boolean isValid = false;
        for (String str : Data.ALL_CHR) {
            if (chr.equalsIgnoreCase("chr" + str)) {
                isValid = true;
            }
        }

        if (!isValid) {
            ErrorManager.print("Invalid region: " + chr);
        }
    }

    public static Region getRegion(int index, String varType) {
        LogManager.writeAndPrintNoNewLine("It is analysing " + varType.toUpperCase().toUpperCase()
                + "s in region " + regionList.get(index) + ".");

        return regionList.get(index);
    }

    public static String addRegionToSQL(Region region, String sqlCode, boolean isIndel) {
        sqlCode += " WHERE v.seq_region_id = " + region.getRegionId() + " ";

        if (region.getStartPosition() > 0) {
            sqlCode += "AND v.seq_region_pos >= " + region.getStartPosition() + " ";
        }

        if (region.getEndPosition() > 0) {
            sqlCode += "AND v.seq_region_pos <= " + region.getEndPosition() + " ";
        }

        return sqlCode;
    }

    public static void addRegionByVariantId(String variantId) {
        String chr = "";
        int start = Data.NA;

        if (variantId.contains("_")) {
            String[] values = variantId.split("_");
            chr = "chr" + values[0];
            start = Integer.valueOf(values[1]);

            if (FormatManager.isInteger(values[2])) {
                VariantManager.addType("indel");
            } else {
                VariantManager.addType("snv");
            }
        } else if (variantId.contains("-")) {
            String[] values = variantId.split("-");
            chr = "chr" + values[0];
            start = Integer.valueOf(values[1]);

            if (values[2].length() == 1
                    && values[3].length() == 1) {
                VariantManager.addType("snv");
            } else {
                VariantManager.addType("indel");
            }
        }

        regionList.add(new Region(chr, start, start));
    }

    private static void initIdChrMap() {
        try {
            for (String chr : Data.ALL_CHR) {
                String sql = "SELECT seq_region_id FROM seq_region where coord_system_id = 2 "
                        + "AND name = '" + chr + "'";
                ResultSet rs = DBManager.executeQuery(sql);

                if (rs.next()) {
                    int id = rs.getInt("seq_region_id");
                    idChrMap.put(id, chr);
                    chrIdMap.put(chr, id);
                }
            }

            chrIdMap.put("XY", chrIdMap.get("X"));
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getChrById(int id) {
        if (idChrMap.containsKey(id)) {
            return idChrMap.get(id);
        }

        return "NA";
    }

    public static int getIdByChr(String chr) {
        if (chrIdMap.containsKey(chr)) {
            return chrIdMap.get(chr);
        }

        return Data.NA;
    }
}