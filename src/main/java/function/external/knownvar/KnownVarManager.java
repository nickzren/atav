package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final HashMap<String, String> omimMap = new HashMap<String, String>();
    private static final HashMap<String, String> acmgMap = new HashMap<String, String>();
    private static final HashMap<String, String> adultOnsetMap = new HashMap<String, String>();
    private static final HashMap<String, ClinGen> clinGenMap = new HashMap<String, ClinGen>();
    private static final HashMap<String, String> pgxMap = new HashMap<String, String>();
    private static final HashSet<String> recessiveCarrierSet = new HashSet<String>();

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

            initOMIMList();

            initACMGList();

            initAdultOnsetList();

            initClinGenList();

            initPGxList();

            initRecessiveCarrierList();
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
                String clinicalSignificance = rs.getString("ClinicalSignificance");
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

    private static void initOMIMList() {
        try {
            String sql = "SELECT * From " + omimTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");
                String diseaseName = rs.getString("diseaseName");
                omimMap.put(geneName, diseaseName);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initACMGList() {
        try {
            String sql = "SELECT * From " + acmgTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");
                String acmg = rs.getString("ACMG");
                acmgMap.put(geneName, acmg);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initAdultOnsetList() {
        try {
            String sql = "SELECT * From " + adultOnsetTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");
                String adultOnset = rs.getString("AdultOnset");
                adultOnsetMap.put(geneName, adultOnset);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initClinGenList() {
        try {
            String sql = "SELECT * From " + clinGenTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");
                String haploinsufficiencyDesc = rs.getString("HaploinsufficiencyDesc");
                String triplosensitivityDesc = rs.getString("TriplosensitivityDesc");

                ClinGen clinGen = new ClinGen(haploinsufficiencyDesc, triplosensitivityDesc);

                clinGenMap.put(geneName, clinGen);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initPGxList() {
        try {
            String sql = "SELECT * From " + pgxTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");
                String pgx = rs.getString("PGx");

                pgxMap.put(geneName, pgx);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initRecessiveCarrierList() {
        try {
            String sql = "SELECT * From " + recessiveCarrierTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName");

                recessiveCarrierSet.add(geneName);
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

    public static HGMD getHGMD(Variant var) {
        HGMD hgmd = hgmdMap.get(var.variantIdStr);

        if (hgmd == null) {
            hgmd = new HGMD(var.region.chrStr, var.region.startPosition,
                    var.refAllele, var.allele, "NA", "NA", "NA");
        }

        hgmd.initFlankingCount();

        return hgmd;
    }

    public static String getOMIM(String geneName) {
        return FormatManager.getString(omimMap.get(geneName));
    }

    public static String getACMG(String geneName) {
        return FormatManager.getString(acmgMap.get(geneName));
    }

    public static String getAdultOnset(String geneName) {
        return FormatManager.getString(adultOnsetMap.get(geneName));
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            clinGen = new ClinGen("NA", "NA");
        }

        return clinGen;
    }

    public static String getPGx(String geneName) {
        return FormatManager.getString(pgxMap.get(geneName));
    }

    public static int getRecessiveCarrier(String geneName) {
        if (recessiveCarrierSet.contains(geneName)) {
            return 1;
        }

        return 0;
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
