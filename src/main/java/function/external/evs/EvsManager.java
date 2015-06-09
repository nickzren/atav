package function.external.evs;

import function.variant.base.Variant;
import global.Data;
import global.SqlQuery;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author nick
 */
public class EvsManager {

    private static String filterStatus;

    public static String getTitle() {
        if (CommandValue.isOldEvsUsed) {
            return "Evs Eur Covered Samples,"
                    + "Evs Eur Average Coverage,"
                    + "Evs Afr Covered Samples,"
                    + "Evs Afr Average Coverage,"
                    + "Evs All Covered Samples,"
                    + "Evs All Average Coverage,"
                    + "Evs Eur Maf," // Eur America
                    + "Evs Eur Genotype Count,"
                    + "Evs Afr Maf," // Afr America
                    + "Evs Afr Genotype Count,"
                    + "Evs All Maf,"
                    + "Evs All Genotype Count,"
                    + "Evs Filter Status,";
        }

        return "";
    }

    private static final int[] EVS_NUM_PUBLIC = {4300, 2203, 6503}; // ea, aa, all

    public static String getFilterStatus() {
        return filterStatus;
    }
    
    public static int getTotalEvsNum(String evsSample) {
        if (evsSample.equals("ea")) {
            return EVS_NUM_PUBLIC[0];
        } else if (evsSample.equals("aa")) {
            return EVS_NUM_PUBLIC[1];
        } else {
            return EVS_NUM_PUBLIC[2];
        }
    }

    public static double[] getMhgf(boolean isSnv, String chr, String pos, String ref, String alt) throws Exception {
        String[] pop = {"ea", "aa", "all"};
        double[] result = {Data.NA, Data.NA, Data.NA};
        ResultSet rs = DBManager.executeQuery(getMAFSql(isSnv, chr, pos, ref));
        if (!rs.next()) {
            return result;
        }
        //get ref_allele and alt_alleles
        String ref_allele = rs.getString("ref_allele");
        String alt_alleles = rs.getString("alt_alleles");
        String[] alleles = alt_alleles.split("/");

        //first figure out if there is a match ref_allele/allele pair
        int alleleindex = getAlleleIndex(ref_allele, alleles, Integer.valueOf(pos), alt, ref);
        if (alleleindex == -1) { // no match 
            return result;
        }

        for (int i = 0; i < pop.length; i++) {
            String genocountstr = rs.getString(pop[i] + "_genotype_count");
            String[] items = genocountstr.split("/");

            int totalcount = 0;
            int homcount = 0;
            String HOMMALE;
            if (!isSnv) {
                HOMMALE = "A" + new Integer(alleleindex + 1).toString();
            } else {
                HOMMALE = alleles[alleleindex];
            }
            String HOM = HOMMALE + HOMMALE;
            for (int j = 0; j < items.length; j++) {
                String[] countpair = items[j].split("=");
                if (countpair.length != 2) {
                    ErrorManager.print("Parsing " + genocountstr + "in DBUtils.getMafString_snv failed");
                }
                int c = Integer.parseInt(countpair[1]);
                totalcount += c;
                if (countpair[0].equalsIgnoreCase(HOM) || countpair[0].equalsIgnoreCase(HOMMALE)) {
                    homcount += c;
                }
            }
            if (totalcount == 0) {//should never happen
                totalcount += 1;
            }
            result[i] = (double) homcount / (double) totalcount;
            if (result[i] > 0.5) {
                result[i] = 1.0 - result[i];
            }
        }
        return result;
    }

    public static String getMafInfo(boolean isSnv, String chr, String pos, String ref, String alt) throws Exception {
        StringBuilder str = new StringBuilder();
        ResultSet rs = DBManager.executeQuery(getMAFSql(isSnv, chr, pos, ref));
        String temp = getMafString(rs, isSnv, chr, Integer.valueOf(pos), ref, alt);

        if (temp.isEmpty()) {
            str.append("NAMAF,NA,NAMAF,NA,NAMAF,NA");
        } else {
            str.append(temp);
        }

        rs.close();

        return str.toString();
    }

    public static String getCoverageInfo(String chr, String pos) throws SQLException {
        StringBuilder str = new StringBuilder();
        ResultSet rs = DBManager.executeQuery(getCoverageSql(chr, pos));
        if (rs.next()) {
            str.append(getCoverageString(rs, "ea"));
            str.append(",").append(getCoverageString(rs, "aa"));
            str.append(",").append(getCoverageString(rs, "all"));
        } else {
            str.append("0,0,0,0,0,0");
        }
        rs.close();

        return str.toString();
    }

    private static String getMafString(ResultSet rs, boolean isSnv, String chr, 
            int pos, String ref, String alt) throws Exception {
        filterStatus = "NA";

        if (!rs.next()) {
            return "";
        }

        String[] pop = {"ea", "aa", "all"};
        double[] maf_values = new double[pop.length];

        String alt_alleles = rs.getString("alt_alleles");
        filterStatus = rs.getString("FilterStatus");
        //get mafs
        if (!isSnv) {
            //get ref_allele and alt_alleles
            String ref_allele = rs.getString("ref_allele");
            String[] alleles = alt_alleles.split("/");

            //first figure out if there is a match ref_allele/allele pair
            int alleleindex = getAlleleIndex(ref_allele, alleles, pos, alt, ref);
            if (alleleindex == -1) { // no match 
                return "";
            }

            for (int i = 0; i < pop.length; i++) {
                String allelecountstr = rs.getString(pop[i] + "_allele_count");
                String[] items = allelecountstr.split("/");
                int totalcount = 0;
                int allecount = 0;
                for (int j = 0; j < items.length; j++) {
                    String[] countpair = items[j].split("=");
                    if (countpair.length != 2) {
                        ErrorManager.print("Parsing " + allelecountstr + "in DBUtils.getMafString_snv failed");
                    }
                    int c = Integer.parseInt(countpair[1]);
                    totalcount += c;
                    if (j == alleleindex) {
                        allecount += c;
                    }
                }
                if (totalcount == 0) {//should never happen
                    totalcount += 1;
                }
                maf_values[i] = (double) allecount / (double) totalcount;
                if (maf_values[i] > 0.5) {
                    maf_values[i] = 1.0 - maf_values[i];
                }

            }

        } else { //snvs            
            if (alt_alleles.equals(alt)) {
                //in this case,we only need to read out the maf_perc value
                //get mafs, just a bit faster
                String mafstr = rs.getString("MAF_perc");
                String[] mafs = mafstr.split("/");
                for (int i = 0; i < mafs.length; i++) {
                    maf_values[i] = Double.parseDouble(mafs[i]) * 0.01;
                }
            } else if (alt_alleles.contains(alt)) {

                for (int i = 0; i < pop.length; i++) {
                    String allelecountstr = rs.getString(pop[i] + "_allele_count");
                    String[] items = allelecountstr.split("/");
                    int totalcount = 0;
                    int allecount = 0;
                    for (int j = 0; j < items.length; j++) {
                        String[] countpair = items[j].split("=");
                        if (countpair.length != 2) {
                            ErrorManager.print("Parsing " + allelecountstr + "in DBUtils.getMafString_snv failed");
                        }
                        int c = Integer.parseInt(countpair[1]);
                        totalcount += c;
                        if (countpair[0].equalsIgnoreCase(alt)) {
                            allecount += c;
                        }
                    }
                    if (totalcount == 0) {//should never happen
                        totalcount += 1;
                    }
                    maf_values[i] = (double) allecount / (double) totalcount;
                    if (maf_values[i] > 0.5) {
                        maf_values[i] = 1.0 - maf_values[i];
                    }

                }
            } else {
                return "";
            }
        }

        //get genotype
        String[] genotype = new String[pop.length];
        for (int i = 0; i < pop.length; i++) {
            genotype[i] = rs.getString(pop[i] + "_genotype_count");
        }

        NumberFormat pformat1 = new DecimalFormat("0.######");

        StringBuilder str = new StringBuilder();
        str.append(pformat1.format(maf_values[0])).append(",").append(genotype[0]);
        str.append(",");
        str.append(pformat1.format(maf_values[1])).append(",").append(genotype[1]);
        str.append(",");
        str.append(pformat1.format(maf_values[2])).append(",").append(genotype[2]);

        return str.toString();
    }

    private static String getMAFSql(boolean isSnv, String chr, String pos, String ref) {
        String str = "";
        if (isSnv) {
            str = SqlQuery.EVS_MAF_SNV;
        } else {
            str = SqlQuery.EVS_MAF_INDEL;
        }
        str = str.replaceAll("_CHR_", chr);
        str = str.replaceAll("_POS_", pos);
        str = str.replaceAll("_ALLELE_", ref);

        return str;
    }

    private static String getCoverageSql(String chr, String pos) {
        String str = SqlQuery.EVS_COVERAGE;;
        str = str.replaceAll("_CHR_", chr);
        return str.replaceAll("_POS_", pos);
    }

    private static String getAlleleKey(int position, String allele, String ref_allele) {
        if (ref_allele.length() == allele.length()) { //for snvs
            return ref_allele + "_" + allele + "_" + position;
        }
        String indelType = "";
        String deltaAllele = "";

        if (ref_allele.length() > allele.length()) {
            indelType = "DEL";
            deltaAllele = ref_allele.substring(allele.length());
        } else {
            indelType = "INS";
            deltaAllele = allele.substring(ref_allele.length());
        }
        return indelType + "_" + deltaAllele + "_" + (position + deltaAllele.length() - 1);
    }
    //figure out if there is a match ref_allele/allele pair

    private static int getAlleleIndex(String ref_allele, String[] alleles, int pos, String alt, String ref) {
        String annodb_ID = getAlleleKey(pos, alt, ref);
        int index = -1;
        for (int i = 0; i < alleles.length; i++) {
            String evs_ID = getAlleleKey(pos, alleles[i], ref_allele);
            if (evs_ID.equalsIgnoreCase(annodb_ID)) {
                index = i;
            }
        }
        return index; //no matching allele
    }

    private static String getCoverageString(ResultSet rs, String ethnicity) throws SQLException {
        StringBuilder str = new StringBuilder();
        int SampleCovered = rs.getInt(ethnicity + "SampleCovered");
        int AvgCoverage = rs.getInt(ethnicity + "AvgCoverage");
        str.append(SampleCovered).append(",").append(AvgCoverage);
        return str.toString();
    }
}
