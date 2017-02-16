package function.external.exac;

import function.external.base.DataManager;
import function.variant.base.Region;
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
    public static final String[] EXAC_SUBSET = {"nonpsych", "nonTCGA"};

    static final String coverageTable = "exac.coverage_03";
    static String variantTable = "exac.variant_r03_2015_09_16";

    private static final String GENE_DAMAGING_COUNTS_PATH = "data/exac/ExAC.r0.3.damagingCounts.csv";
    private static final HashMap<String, String> geneDamagingCountsMap = new HashMap<>();
    private static String geneDamagingCountsNA = "";

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

    public static void init() {
        if (ExacCommand.isListExacCount) {
            initGeneDamagingCountsMap();
        }
    }

    public static String getVersion() {
        if (ExacCommand.isIncludeExac) {
            return "ExAC: " + DataManager.getVersion(variantTable) + "\n";
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

    public static String getSqlByRegion(Region region) {
        String result = "chr,pos,ref,alt,";

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
            int pos, String ref, String alt) {
        String result = "";

        for (String str : EXAC_POP) {
            result += str + "_af,"
                    + str + "_gts,";
        }

        result += "vqslod ";

        return "SELECT " + result
                + "FROM " + variantTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos + " "
                + "AND ref = '" + ref + "' "
                + "AND alt = '" + alt + "'";
    }

    private static void initGeneDamagingCountsMap() {
        try {
            File f = new File(GENE_DAMAGING_COUNTS_PATH);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                int firstCommaIndex = lineStr.indexOf(",");
                String geneName = lineStr.substring(0, firstCommaIndex);
                String values = lineStr.substring(firstCommaIndex + 1);

                if (geneName.equals("Gene")) {
                    geneDamagingCountsMap.put("title", values + ",");

                    for (int i = 0; i < values.split(",").length; i++) {
                        geneDamagingCountsNA += "NA,";
                    }
                } else {
                    geneDamagingCountsMap.put(geneName, values + ",");
                }
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getGeneDamagingCountsLine(String geneName) {
        String line = geneDamagingCountsMap.get(geneName);

        if (line == null) {
            return geneDamagingCountsNA;
        }

        return line;
    }

    public static String getCountByGene(String gene) {
        return geneDamagingCountsMap.get(gene);
    }
}
