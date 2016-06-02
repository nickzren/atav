package function.external.knownvar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
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

    public static final String clinVarTable = "knownvar.clinvar_2016_04_26";
    public static final String clinVarPathoratioTable = "knownvar.clinvar_pathoratio_2016_04_26";
    public static final String hgmdTable = "knownvar.hgmd_2016_1";
    public static final String omimTable = "knownvar.omim_2016_04_26";
    public static final String acmgTable = "knownvar.ACMG_2016_04_26";
    public static final String clinGenTable = "knownvar.ClinGen_2016_04_26";
    public static final String recessiveCarrierTable = "knownvar.RecessiveCarrier_2015_12_09";

    private static final Multimap<String, ClinVar> clinVarMultiMap = ArrayListMultimap.create();
    private static final HashMap<String, ClinVarPathoratio> clinVarPathoratioMap = new HashMap<>();
    private static final Multimap<String, HGMD> hgmdMultiMap = ArrayListMultimap.create();
    private static final HashMap<String, String> omimMap = new HashMap<>();
    private static final HashMap<String, String> acmgMap = new HashMap<>();
    private static final HashMap<String, ClinGen> clinGenMap = new HashMap<>();
    private static final HashSet<String> recessiveCarrierSet = new HashSet<>();

    public static String getTitle() {
        if (KnownVarCommand.isIncludeKnownVar) {
            return "HGMDm2site,"
                    + "HGMDm1site,"
                    + "HGMD site,"
                    + "HGMD Disease,"
                    + "HGMD PMID,"
                    + "HGMD Class,"
                    + "HGMDp1site,"
                    + "HGMDp2site,"
                    + "HGMD indel 9bpflanks,"
                    + "ClinVar,"
                    + "ClinVar Disease,"
                    + "ClinVar Clinical Significance,"
                    + "ClinVar PMID,"
                    + "ClinVar Other Ids,"
                    + "ClinVar pathogenic indels,"
                    + "ClinVar all indels,"
                    + "ClinVar Pathogenic Indel Count,"
                    + "Clinvar Pathogenic CNV Count,"
                    + "ClinVar Pathogenic SNV Splice Count,"
                    + "ClinVar Pathogenic SNV Nonsense Count,"
                    + "ClinVar Pathogenic SNV Missense Count,"
                    + "ClinGen,"
                    + "ClinGen HaploinsufficiencyDesc,"
                    + "ClinGen TriplosensitivityDesc,"
                    + "OMIM Disease,"
                    + "RecessiveCarrier," 
                    + "ACMG,";
        } else {
            return "";
        }
    }

    public static void init() throws SQLException {
        if (KnownVarCommand.isIncludeKnownVar) {
            initClinVarMap();

            initClinVarPathoratioMap();

            initHGMDMap();

            initOMIMMap();

            initACMGMap();

            initClinGenMap();

            initRecessiveCarrierMap();

            if (KnownVarCommand.isKnownVarOnly) {
                VariantManager.reset2KnownVarSet();
            }
        }
    }

    private static void initClinVarMap() {
        try {
            String sql = "SELECT * From " + clinVarTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");
                String clinicalSignificance = FormatManager.getString(rs.getString("ClinicalSignificance"));
                String otherIds = FormatManager.getString(rs.getString("OtherIds").replaceAll(",", " | "));
                String diseaseName = FormatManager.getString(rs.getString("DiseaseName").replaceAll(",", ""));
                String pubmedID = FormatManager.getString(rs.getString("PubmedID"));

                ClinVar clinVar = new ClinVar(chr, pos, ref, alt,
                        clinicalSignificance, otherIds, diseaseName, pubmedID);

                clinVarMultiMap.put(clinVar.getSiteId(), clinVar);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initClinVarPathoratioMap() {
        try {
            String sql = "SELECT * From " + clinVarPathoratioTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
                int indelCount = rs.getInt("indelCount");
                int copyCount = rs.getInt("copyCount");
                int snvSpliceCount = rs.getInt("snvSpliceCount");
                int snvNonsenseCount = rs.getInt("snvNonsenseCount");
                int snvMissenseCount = rs.getInt("snvMissenseCount");

                ClinVarPathoratio clinVarPathoratio = new ClinVarPathoratio(
                        indelCount,
                        copyCount,
                        snvSpliceCount,
                        snvNonsenseCount,
                        snvMissenseCount);

                clinVarPathoratioMap.put(geneName, clinVarPathoratio);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initHGMDMap() {
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

                HGMD hgmd = new HGMD(chr, pos, ref, alt,
                        variantClass, pmid, diseaseName);

                hgmdMultiMap.put(hgmd.getSiteId(), hgmd);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initOMIMMap() {
        try {
            String sql = "SELECT * From " + omimTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
                String diseaseName = rs.getString("diseaseName");
                omimMap.put(geneName, diseaseName);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initACMGMap() {
        try {
            String sql = "SELECT * From " + acmgTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
                String acmg = rs.getString("ACMG");
                acmgMap.put(geneName, acmg);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initClinGenMap() {
        try {
            String sql = "SELECT * From " + clinGenTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();
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

    private static void initRecessiveCarrierMap() {
        try {
            String sql = "SELECT * From " + recessiveCarrierTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String geneName = rs.getString("geneName").toUpperCase();

                recessiveCarrierSet.add(geneName);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static ClinVarOutput getClinVarOutput(Variant var) {
        Collection<ClinVar> collection = clinVarMultiMap.get(var.getSiteId());

        ClinVarOutput output = new ClinVarOutput(var.region.chrStr, var.region.startPosition,
                var.refAllele, var.allele, collection);

        return output;
    }

    public static Multimap<String, ClinVar> getClinVarMultiMap() {
        return clinVarMultiMap;
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            clinGen = new ClinGen("NA", "NA");
        }

        return clinGen;
    }

    public static Multimap<String, HGMD> getHGMDMultiMap() {
        return hgmdMultiMap;
    }

    public static HGMDOutput getHGMDOutput(Variant var) {
        Collection<HGMD> collection = hgmdMultiMap.get(var.getSiteId());

        HGMDOutput output = new HGMDOutput(var.region.chrStr, var.region.startPosition,
                var.refAllele, var.allele, collection);

        return output;
    }

    public static String getOMIM(String geneName) {
        return FormatManager.getString(omimMap.get(geneName));
    }

    public static String getACMG(String geneName) {
        return FormatManager.getString(acmgMap.get(geneName));
    }

    public static ClinVarPathoratio getClinPathoratio(String geneName) {
        ClinVarPathoratio clinVarPathoratio = clinVarPathoratioMap.get(geneName);

        if (clinVarPathoratio == null) {
            clinVarPathoratio = new ClinVarPathoratio(Data.NA, Data.NA, Data.NA, Data.NA, Data.NA);
        }

        return clinVarPathoratio;
    }

    public static int getRecessiveCarrier(String geneName) {
        if (recessiveCarrierSet.contains(geneName)) {
            return 1;
        }

        return 0;
    }

    public static int getClinVarPathogenicIndelFlankingCount(String chr, int pos) {
        try {
            int distance = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + clinVarTable + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos BETWEEN " + (pos - distance) + " AND " + (pos + distance) + " "
                    + "AND ClinicalSignificance like '%pathogenic%' "
                    + "AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";

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

    public static int getClinVarAllIndelFlankingCount(String chr, int pos) {
        try {
            int distance = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + clinVarTable + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos BETWEEN " + (pos - distance) + " AND " + (pos + distance) + " "
                    + "AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";

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

    public static int getHGMDIndelFlankingCount(String chr, int pos) {
        try {
            int width = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + hgmdTable + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width) + " "
                    + "AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";

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

    public static String getHGMDBySite(String chr, int pos, int distance) {
        try {
            String sql = "SELECT ref,alt,DiseaseName,variantClass,pmid "
                    + "From " + hgmdTable + " "
                    + "WHERE chr='" + chr + "' "
                    + "AND pos = " + (pos + distance);

            ResultSet rs = DBManager.executeQuery(sql);

            StringBuilder sb = new StringBuilder();
            int count = 0;

            while (rs.next()) {
                if (rs.getString("ref").length() > 1
                        || rs.getString("alt").length() > 1) { // skip indels
                    continue;
                }

                if (count > 0) {
                    sb.append("|");
                }

                sb.append(rs.getString("DiseaseName")).append(";");
                sb.append(rs.getString("variantClass")).append(";");
                sb.append(rs.getString("pmid"));

                count++;
            }

            rs.close();

            if (sb.length() > 0) {
                return count + ";" + sb.toString();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return "NA";
    }
}
