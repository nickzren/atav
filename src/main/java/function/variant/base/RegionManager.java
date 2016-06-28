package function.variant.base;

import function.annotation.base.GeneManager;
import utils.DBManager;
import utils.ErrorManager;
import utils.CommonCommand;
import utils.LogManager;
import global.Data;
import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class RegionManager {

    private static ArrayList<Region> regionList = new ArrayList<>();
    private static HashMap<Integer, String> idChrMap = new HashMap<>();
    private static HashMap<String, Integer> chrIdMap = new HashMap<>();
    private static boolean isUsed = false;

    public static final String[] ALL_CHR = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"};

    public static void init() {
        if (CommonCommand.isNonDBAnalysis) {
            return;
        }

        initIdChrMap();

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
            in.close();
            fstream.close();
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

        int start = Data.NA, end = Data.NA;

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
            ErrorManager.print("Invalid region: " + str);
        }

        return new Region(chr, start, end);
    }

    private static String getChr(String chr) {
        if (chr.equalsIgnoreCase("XY")) {
            chr = "X";
        }

        checkChrValid(chr);

        return chr;
    }

    private static void checkChrValid(String chr) {
        boolean isValid = false;
        for (String str : ALL_CHR) {
            if (chr.equalsIgnoreCase(str)) {
                isValid = true;
            }
        }

        if (!isValid) {
            ErrorManager.print("Invalid chr: " + chr);
        }
    }

    public static Region getRegion(int index, String varType) {
        LogManager.writeAndPrintNoNewLine("It is analysing " + varType.toUpperCase().toUpperCase()
                + "s in region " + regionList.get(index) + ".");

        return regionList.get(index);
    }

    public static String addRegionToSQL(Region region, String sqlCode, boolean isIndel) {
        int regionId = RegionManager.getIdByChr(region.chrStr);

        sqlCode += " WHERE v.seq_region_id = " + regionId + " ";

        if (region.getStartPosition() > 0) {
            sqlCode += "AND v.seq_region_pos >= " + region.getStartPosition() + " ";
        }

        if (region.getEndPosition() > 0) {
            sqlCode += "AND v.seq_region_pos <= " + region.getEndPosition() + " ";
        }

        if (GeneManager.isUsed()) {
            sqlCode += "AND g.gene_name in " + GeneManager.getAllGeneByChr(region.chrStr) + " ";
        }

        return sqlCode;
    }

    public static void addRegionByVariantId(String variantId) {
        String[] values = variantId.split("-");
        String chr = values[0];
        int pos = Integer.valueOf(values[1]);

        if (values.length == 3) // variant position format chr-pos-type
        {
            VariantManager.addType(values[2]);
        } else // variant id format chr-pos-ref-alt
         if (values[2].length() == 1
                    && values[3].length() == 1) {
                VariantManager.addType("snv");
            } else {
                VariantManager.addType("indel");
            }

        regionList.add(new Region(chr, pos, pos));
    }

    private static void initIdChrMap() {
        try {
            for (String chr : ALL_CHR) {
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
