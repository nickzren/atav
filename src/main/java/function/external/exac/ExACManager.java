package function.external.exac;

import function.external.base.DataManager;
import function.variant.base.Region;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ExACManager {

    public static final String[] EXAC_POP = {"global", "afr", "amr", "eas", "sas", "fin", "nfe", "oth"};

    private static final String coverageTable = "exac.coverage_03";
    private static String variantTable = "exac.variant_r03_2015_09_16";
    private static String mnvTable = "exac.mnv_r03_2015_09_16";

    private static final String GENE_VARIANT_COUNT_PATH = "data/exac/ExAC.r0.3.damagingCounts.csv";
    private static final HashMap<String, String> geneVariantCountMap = new HashMap<>();
    private static String geneVariantCountHeader;
    private static StringJoiner NA = new StringJoiner(",");

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        for (String str : EXAC_POP) {
            sj.add("ExAC " + str + " af");
            sj.add("ExAC " + str + " gts");
        }

        sj.add("ExAC vqslod");
        sj.add("ExAC Mean Coverage");
        sj.add("ExAC Sample Covered 10x");

        return sj.toString();
    }

    public static String getGeneVariantCountHeader() {
        return geneVariantCountHeader;
    }

    public static void init() {
        if (ExACCommand.isIncludeCount) {
            initGeneVariantCountMap();
        }
    }

    public static String getVersion() {
        return "ExAC: " + DataManager.getVersion(variantTable) + "\n";
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

        return sql;
    }

    public static String getSqlByRegion(Region region) {
        String result = "chr,pos,ref_allele,alt_allele,";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        String sql = "SELECT " + result
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + region.getChrStr() + "' "
                + "AND pos BETWEEN " + region.getStartPosition() + " AND " + region.getEndPosition();

        return sql;
    }

    public static String getSqlByVariant(String chr,
            int pos, String ref, String alt, boolean isMNV) {
        String result = "";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";
        
        String table = variantTable;
        if(isMNV) {
            table = mnvTable;
        }

        return "SELECT " + result
                + "FROM " + table + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref_allele = '" + ref + "' "
                + "AND alt_allele = '" + alt + "'";
    }

    private static void initGeneVariantCountMap() {
        try {
            File f = new File(Data.ATAV_HOME + GENE_VARIANT_COUNT_PATH);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    geneVariantCountHeader = values;

                    for (int i = 0; i < values.split(",").length; i++) {
                        NA.add(Data.STRING_NA);
                    }
                } else {
                    geneVariantCountMap.put(geneName, values);
                }
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getLine(String geneName) {
        String line = geneVariantCountMap.get(geneName);

        return line == null ? NA.toString() : line;
    }
}
