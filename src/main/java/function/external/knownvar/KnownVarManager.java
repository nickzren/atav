package function.external.knownvar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import function.external.base.DataManager;
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

    public static final String hgmdTable = "knownvar.hgmd_2017_1";
    public static final String clinVarTable = "knownvar.clinvar_2017_05_02";
    public static final String clinVarPathoratioTable = "knownvar.clinvar_pathoratio_2017_05_02";
    public static final String clinGenTable = "knownvar.clingen_2017_05_03";
    public static final String omimTable = "knownvar.omim_2017_05_02";
    public static final String recessiveCarrierTable = "knownvar.RecessiveCarrier_2015_12_09";
    public static final String acmgTable = "knownvar.ACMG_2016_11_19";
    public static final String dbDSMTable = "knownvar.dbDSM_2016_09_28";

    private static final Multimap<String, HGMD> hgmdMultiMap = ArrayListMultimap.create();
    private static final Multimap<String, ClinVar> clinVarMultiMap = ArrayListMultimap.create();
    private static final HashMap<String, ClinVarPathoratio> clinVarPathoratioMap = new HashMap<>();
    private static final HashMap<String, ClinGen> clinGenMap = new HashMap<>();
    private static final HashMap<String, String> omimMap = new HashMap<>();
    private static final HashSet<String> recessiveCarrierSet = new HashSet<>();
    private static final HashMap<String, String> acmgMap = new HashMap<>();
    private static final Multimap<String, DBDSM> dbDSMMultiMap = ArrayListMultimap.create();

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
                    + "ClinVar RS Number,"
                    + "ClinVar Disease,"
                    + "ClinVar Clinical Significance,"
                    + "ClinVar PMID,"
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
                    + "ACMG,"
                    + "dbDSM Disease,"
                    + "dbDSM Classification,"
                    + "dbDSM PubmedID,";
        } else {
            return "";
        }
    }

    public static String getVersion() {
        if (KnownVarCommand.isIncludeKnownVar) {
            return "HGMD: " + DataManager.getVersion(hgmdTable) + "\n"
                    + "ClinVar: " + DataManager.getVersion(clinVarTable) + "\n"
                    + "ClinVarPathoratio: " + DataManager.getVersion(clinVarPathoratioTable) + "\n"
                    + "ClinGen: " + DataManager.getVersion(clinGenTable) + "\n"
                    + "OMIM: " + DataManager.getVersion(omimTable) + "\n"
                    + "RecessiveCarrier: " + DataManager.getVersion(recessiveCarrierTable) + "\n"
                    + "ACMG: " + DataManager.getVersion(acmgTable) + "\n"
                    + "dbDSM: " + DataManager.getVersion(dbDSMTable) + "\n";
        } else {
            return "";
        }
    }

    public static void init() throws SQLException {
        if (KnownVarCommand.isIncludeKnownVar) {
            initHGMDMap();

            initClinVarMap();

            initClinVarPathoratioMap();

            initClinGenMap();

            initOMIMMap();

            initRecessiveCarrierMap();

            initACMGMap();

            initDBDSMMap();

            if (KnownVarCommand.isKnownVarOnly) {
                VariantManager.reset2KnownVarSet();
            }
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

    private static void initClinVarMap() {
        try {
            String sql = "SELECT * From " + clinVarTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");
                String rsNumber = FormatManager.getString(rs.getString("rsID"));
                String clinicalSignificance = FormatManager.getString(rs.getString("ClinSig"));
                String diseaseName = FormatManager.getString(rs.getString("DiseaseName"));
                String pubmedID = FormatManager.getString(rs.getString("PubmedID"));

                ClinVar clinVar = new ClinVar(chr, pos, ref, alt,
                        rsNumber, clinicalSignificance, diseaseName, pubmedID);

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

    private static void initDBDSMMap() {
        try {
            String sql = "SELECT * From " + dbDSMTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");
                String disease = FormatManager.getString(rs.getString("Disease"));
                String classification = FormatManager.getString(rs.getString("Classification"));
                String pubmedID = FormatManager.getString(rs.getString("PubmedID"));

                DBDSM dbDSM = new DBDSM(chr, pos, ref, alt,
                        disease, classification, pubmedID);

                dbDSMMultiMap.put(dbDSM.getSiteId(), dbDSM);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static Multimap<String, HGMD> getHGMDMultiMap() {
        return hgmdMultiMap;
    }

    public static HGMDOutput getHGMDOutput(Variant var) {
        Collection<HGMD> collection = hgmdMultiMap.get(var.getSiteId());

        HGMDOutput output = new HGMDOutput(var, collection);

        return output;
    }

    public static int getHGMDIndelFlankingCount(Variant var) {
        try {
            int width = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + hgmdTable + " "
                    + "WHERE chr='" + var.getChrStr() + "' "
                    + "AND pos BETWEEN " + (var.getStartPosition() - width) + " AND " + (var.getStartPosition() + width) + " "
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

    public static String getHGMDBySite(Variant var, int distance) {
        try {
            if (var.isSnv()) { // only query for SNVs
                String sql = "SELECT ref,alt,DiseaseName,variantClass,pmid "
                        + "From " + hgmdTable + " "
                        + "WHERE chr='" + var.getChrStr() + "' "
                        + "AND pos = " + (var.getStartPosition() + distance);

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
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return "NA";
    }

    public static Multimap<String, ClinVar> getClinVarMultiMap() {
        return clinVarMultiMap;
    }

    public static ClinVarOutput getClinVarOutput(Variant var) {
        Collection<ClinVar> collection = clinVarMultiMap.get(var.getSiteId());

        ClinVarOutput output = new ClinVarOutput(var, collection);

        return output;
    }

    public static int getClinVarPathogenicIndelFlankingCount(Variant var) {
        try {
            int width = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + clinVarTable + " "
                    + "WHERE chr='" + var.getChrStr() + "' "
                    + "AND pos BETWEEN " + (var.getStartPosition() - width) + " AND " + (var.getStartPosition() + width) + " "
                    + "AND ClinSig like '%pathogenic%' "
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

    public static int getClinVarAllIndelFlankingCount(Variant var) {
        try {
            int width = 9;

            String sql = "SELECT count(*) as count "
                    + "From " + clinVarTable + " "
                    + "WHERE chr='" + var.getChrStr() + "' "
                    + "AND pos BETWEEN " + (var.getStartPosition() - width) + " AND " + (var.getStartPosition() + width) + " "
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

    public static ClinVarPathoratio getClinPathoratio(String geneName) {
        ClinVarPathoratio clinVarPathoratio = clinVarPathoratioMap.get(geneName);

        if (clinVarPathoratio == null) {
            clinVarPathoratio = new ClinVarPathoratio(Data.NA, Data.NA, Data.NA, Data.NA, Data.NA);
        }

        return clinVarPathoratio;
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            clinGen = new ClinGen("NA", "NA");
        }

        return clinGen;
    }

    public static String getOMIM(String geneName) {
        return FormatManager.getString(omimMap.get(geneName));
    }

    public static int getRecessiveCarrier(String geneName) {
        if (recessiveCarrierSet.contains(geneName)) {
            return 1;
        }

        return 0;
    }

    public static String getACMG(String geneName) {
        return FormatManager.getString(acmgMap.get(geneName));
    }

    public static Multimap<String, DBDSM> getDBDSMMultiMap() {
        return dbDSMMultiMap;
    }

    public static DBDSMOutput getDBDSMOutput(Variant var) {
        Collection<DBDSM> collection = dbDSMMultiMap.get(var.getSiteId());

        DBDSMOutput output = new DBDSMOutput(var, collection);

        return output;
    }
}
