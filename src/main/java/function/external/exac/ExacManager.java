package function.external.exac;

import function.external.base.DataManager;
import function.variant.base.Region;
import global.Data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ExacManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};

    private static final String coverageTable = "exac.coverage_03";
    private static String snvTable = "exac.snv_maf_r03_2015_09_16";
    private static String indelTable = "exac.indel_maf_r03_2015_09_16";

    private static final String GENE_VARIANT_COUNT_PATH = "data/exac/ExAC.r0.3.damagingCounts.csv";
    private static final HashMap<String, String> geneVariantCountMap = new HashMap<>();
    private static String geneVariantCountTitle;
    private static String NA = "";

    public static void init() {
        if (ExacCommand.isIncludeExacGeneVariantCount) {
            initGeneVariantCountMap();
        }
    }

    public static String getTitle() {
        String title = "";

        if (ExacCommand.isIncludeExac) {
            for (String str : EXAC_POP) {
                title += "ExAC " + str + " maf,"
                        + "ExAC " + str + " gts,";
            }

            title += "ExAC vqslod,"
                    + "ExAC Mean Coverage,"
                    + "ExAC Sample Covered 10x,";
        }

        return title;
    }

    public static String getGeneVariantCountTitle() {
        if (ExacCommand.isIncludeExacGeneVariantCount) {
            return geneVariantCountTitle + ",";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (ExacCommand.isIncludeExac) {
            return "ExAC: " + DataManager.getVersion(snvTable) + "\n";
        } else {
            return "";
        }
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSql4Maf(boolean isIndel, Region region) {
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result;

        String table = snvTable;

        if (isIndel) {
            table = indelTable;
        }

        sql += "FROM " + table + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSql4Maf(boolean isSnv, String chr,
            int pos, String ref, String alt) {
        String result = "";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result;

        if (isSnv) {
            sql += "FROM " + snvTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND alt_allele = '" + alt + "'";
        } else {
            sql += "FROM " + indelTable + " "
                    + "WHERE chr = '" + chr + "' "
                    + "AND pos = " + pos + " "
                    + "AND ref_allele = '" + ref + "' "
                    + "AND alt_allele = '" + alt + "'";
        }

        return sql;
    }

    private static void initGeneVariantCountMap() {
        try {
            File f = new File(Data.ATAV_HOME + GENE_VARIANT_COUNT_PATH);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    geneVariantCountTitle = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA += "NA,";
                    }
                } else {
                    geneVariantCountMap.put(geneName, values + ",");
                }
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = geneVariantCountMap.get(geneName);

        return line == null ? NA : line;
    }
}
