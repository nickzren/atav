package function.external.exac;

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
    static String snvTable = "exac.snv_maf_r03_2015_09_16";
    static String indelTable = "exac.indel_maf_r03_2015_09_16";

    private static final String GENE_DAMAGING_COUNTS_PATH = "data/exac/ExAC.r0.3.damagingCounts.csv";
    private static final HashMap<String, String> geneDamagingCountsMap = new HashMap<>();
    private static String geneDamagingCountsNA = "";

    public static void init() {
        if (ExacCommand.isIncludeExac) {
            if (!ExacCommand.isListExac) { // hack here , list exac function cannot support gene level output
                initGeneDamagingCountsMap();
            }
        }
    }

    public static void resetTables() {
        if (ExacCommand.exacSubset.equalsIgnoreCase("nonpsyc")) {
            snvTable = "exac.snv_maf_r03_nonpsych";
            indelTable = "exac.indel_maf_r03_nonpsych";
        } else if (ExacCommand.exacSubset.equalsIgnoreCase("nonTCGA")) {
            snvTable = "exac.snv_maf_r03_nonTCGA";
            indelTable = "exac.indel_maf_r03_nonTCGA";
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

            if (!ExacCommand.isListExac) { // hack here , list exac function cannot support gene level output
                title += geneDamagingCountsMap.get("title");
            }
        }

        return title;
    }

    public static String getSql4Cvg(String chr, int pos) {
        String sql = "SELECT mean_cvg, covered_10x "
                + "FROM " + coverageTable + " "
                + "WHERE chr = '" + chr + "' "
                + "AND pos = " + pos;

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
}
