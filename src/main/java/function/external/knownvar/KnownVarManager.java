package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.sql.ResultSet;
import java.util.HashMap;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class KnownVarManager {

    public static final String clinvarTable = "knownvar.clinvar_2015_10_12";
    public static final String hgmdTable = "knownvar.hgmd_2015_4";
    public static final String omimTable = "knownvar.omim_2015_10_12";
    public static final String acmgTable = "knownvar.ACMG_2015_12_09";
    public static final String adultOnsetTable = "knownvar.AdultOnset_2015_12_09";
    public static final String clinGenTable = "knownvar.ClinGen_2015_12_09";
    public static final String pgxTable = "knownvar.PGx_2015_12_09";
    public static final String recessiveCarrierTable = "knownvar.RecessiveCarrier_2015_12_09";

    private static final HashMap<String, Clinvar> clinvarMap = new HashMap<String, Clinvar>();
    private static final HashMap<String, HGMD> hgmdMap = new HashMap<String, HGMD>();

    public static String getTitle() {
        if (KnownVarCommand.isIncludeKnownVar) {
            return "Clinvar Clinical Significance,"
                    + "Clinvar Other Ids,"
                    + "Clinvar Disease Name,"
                    + "Clinvar Flanking Count,"
                    + "HGMD Variant Class,"
                    + "HGMD Pmid,"
                    + "HGMD Disease Name,"
                    + "HGMD Flanking Count,"
                    + "OMIM Disease Name,"
                    + "ACMG,"
                    + "AdultOnset,"
                    + "ClinGen HaploinsufficiencyDesc,"
                    + "ClinGen TriplosensitivityDesc,"
                    + "PGx,"
                    + "RecessiveCarrier,";
        } else {
            return "";
        }
    }

    public static void init() {
        if (KnownVarCommand.isIncludeKnownVar) {
            initClinvarList();

            initHGMDList();
        }
    }

    private static void initClinvarList() {
        try {
            String sql = "SELECT * From " + clinvarTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");
                String clinicalSignificance = rs.getString("ClinicalSignificance".replaceAll(";", " | "));
                String otherIds = rs.getString("OtherIds").replaceAll(",", " | ");
                String diseaseName = rs.getString("DiseaseName").replaceAll(",", "");

                Clinvar clinvar = new Clinvar(chr, pos, ref, alt,
                        clinicalSignificance, otherIds, diseaseName);

                clinvarMap.put(clinvar.getVariantId(), clinvar);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static Clinvar getClinvar(Variant var) {
        Clinvar clinvar = clinvarMap.get(var.variantIdStr);

        if (clinvar == null) {
            clinvar = new Clinvar(var.region.chrStr, var.region.startPosition,
                    var.refAllele, var.allele, "NA", "NA", "NA");
        }

        clinvar.initFlankingCount();

        return clinvar;
    }

    private static void initHGMDList() {
        try {
            String sql = "SELECT * From " + hgmdTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");
                String variantClass = FormatManager.getString(rs.getString("variantClass"));
                String pmid = FormatManager.getString(rs.getString("pmid"));
                String diseaseName = FormatManager.getString(rs.getString("DiseaseName"));

                String id = chr + "-" + pos + "-" + ref + "-" + alt;

                if (hgmdMap.containsKey(id)) {
                    hgmdMap.get(id).append(variantClass, pmid, diseaseName);
                } else {
                    HGMD hgmd = new HGMD(chr, pos, ref, alt,
                            variantClass, pmid, diseaseName);

                    hgmdMap.put(id, hgmd);
                }
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static HGMD getHGMD(Variant var) {
        HGMD hgmd = hgmdMap.get(var.variantIdStr);

        if (hgmd == null) {
            hgmd = new HGMD(var.region.chrStr, var.region.startPosition,
                    var.refAllele, var.allele, "NA", "NA", "NA");
        }

        hgmd.initFlankingCount();

        return hgmd;
    }

    public static String getSql4OMIM(String geneName) {
        return "SELECT diseaseName "
                + "From " + omimTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static String getSql4ACMG(String geneName) {
        return "SELECT ACMG "
                + "From " + acmgTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static String getSql4AdultOnset(String geneName) {
        return "SELECT AdultOnset "
                + "From " + adultOnsetTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static String getSql4ClinGen(String geneName) {
        return "SELECT HaploinsufficiencyDesc,TriplosensitivityDesc "
                + "From " + clinGenTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static String getSql4PGx(String geneName) {
        return "SELECT PGx "
                + "From " + pgxTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static String getSql4RecessiveCarrier(String geneName) {
        return "SELECT * "
                + "From " + recessiveCarrierTable + " "
                + "WHERE geneName='" + geneName + "' ";
    }

    public static int getFlankingCount(boolean isSnv, String chr, int pos, String table) {
        try {
            int width = KnownVarCommand.snvWidth;

            if (!isSnv) {
                width = KnownVarCommand.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From " + table + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);
            
            System.out.println(sql);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.NA;
    }
}
