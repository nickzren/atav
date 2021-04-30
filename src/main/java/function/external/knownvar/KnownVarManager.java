package function.external.knownvar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import function.external.base.DataManager;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class KnownVarManager {

    public static final String hgmdTable = "knownvar.hgmd_2021_1";
    public static final String clinVarTable = "knownvar.clinvar_2021_04_08";
    public static final String clinVarPathoratioTable = "knownvar.clinvar_pathoratio_2021_04_08";
    public static final String clinGenTable = "knownvar.clingen_2021_04_08";
    public static final String omimTable = "knownvar.omim_2021_04_08";
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

    private static PreparedStatement preparedStatement4HGMDSite;
    private static PreparedStatement preparedStatement4HGMDIndelFlankingCount;
    private static PreparedStatement preparedStatement4ClinVarPathogenicIndelFlankingCount;
    private static PreparedStatement preparedStatement4ClinVarAllIndelFlankingCount;

    private static void initPreparedStatement() {
        String sql = "SELECT ref,alt,DiseaseName,variantClass,pmid From " + hgmdTable + " WHERE chr=? AND pos=?";
        preparedStatement4HGMDSite = DBManager.initPreparedStatement(sql);

        sql = "SELECT count(*) as count From " + hgmdTable
                + " WHERE chr=? AND pos BETWEEN ? AND ? AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";
        preparedStatement4HGMDIndelFlankingCount = DBManager.initPreparedStatement(sql);

        sql = "SELECT count(*) as count "
                + "From " + clinVarTable + " "
                + "WHERE chr=? "
                + "AND pos BETWEEN ? AND ? "
                + "AND ClinSig like '%pathogenic%' "
                + "AND ClinSig not like '%conflicting%' "
                + "AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";
        preparedStatement4ClinVarPathogenicIndelFlankingCount = DBManager.initPreparedStatement(sql);

        sql = "SELECT count(*) as count From " + clinVarTable + " "
                + "WHERE chr=? AND pos BETWEEN ? AND ? AND (LENGTH(ref) > 1 or LENGTH(alt) > 1)";
        preparedStatement4ClinVarAllIndelFlankingCount = DBManager.initPreparedStatement(sql);

    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("HGMDm2site");
        sj.add("HGMDm1site");
        sj.add("HGMD site");
        sj.add("HGMD Disease");
        sj.add("HGMD PMID");
        sj.add("HGMD Class");
        sj.add("HGMDp1site");
        sj.add("HGMDp2site");
        sj.add("HGMD indel 9bpflanks");
        sj.add("ClinVar");
        sj.add("ClinVar HGVS");
        sj.add("ClinVar ClinSource");
        sj.add("ClinVar AlleleOrigin");
        sj.add("ClinVar ClinRevStat");
        sj.add("ClinVar ClinRevStar");
        sj.add("ClinVar ClinSig");
        sj.add("ClinVar ClinSigIncl");
        sj.add("ClinVar DiseaseDB");
        sj.add("ClinVar DiseaseName");
        sj.add("ClinVar PubmedID");
        sj.add("ClinVar rsID");
        sj.add("ClinVar pathogenic indels");
        sj.add("ClinVar all indels");
        sj.add("ClinVar Pathogenic Indel Count");
        sj.add("Clinvar Pathogenic CNV Count");
        sj.add("ClinVar Pathogenic SNV Splice Count");
        sj.add("ClinVar Pathogenic SNV Nonsense Count");
        sj.add("ClinVar Pathogenic SNV Missense Count");
        sj.add("ClinVar Pathogenic Last Pathogenic Location");
        sj.add("ClinGen");
        sj.add("ClinGen HaploinsufficiencyDesc");
        sj.add("ClinGen TriplosensitivityDesc");
        sj.add("OMIM Disease");
        sj.add("RecessiveCarrier");
        sj.add("ACMG");
        sj.add("dbDSM Disease");
        sj.add("dbDSM Classification");
        sj.add("dbDSM PubmedID");

        return sj.toString();
    }

    public static String getVersion() {
        return "HGMD: " + DataManager.getVersion(hgmdTable) + "\n"
                + "ClinVar: " + DataManager.getVersion(clinVarTable) + "\n"
                + "ClinVarPathoratio: " + DataManager.getVersion(clinVarPathoratioTable) + "\n"
                + "ClinGen: " + DataManager.getVersion(clinGenTable) + "\n"
                + "OMIM: " + DataManager.getVersion(omimTable) + "\n"
                + "RecessiveCarrier: " + DataManager.getVersion(recessiveCarrierTable) + "\n"
                + "ACMG: " + DataManager.getVersion(acmgTable) + "\n"
                + "dbDSM: " + DataManager.getVersion(dbDSMTable) + "\n";

    }

    public static void init() throws SQLException {
        if (KnownVarCommand.isInclude) {
            initPreparedStatement();

            initHGMDMap();

            initClinVarMap();

            initClinVarPathoratioMap();

            initClinGenMap();

            initOMIMMap();

            initRecessiveCarrierMap();

            initACMGMap();

            initDBDSMMap();

            if (KnownVarCommand.isKnownVarOnly
                    || KnownVarCommand.isKnownVarPathogenicOnly) {
                VariantManager.reset2KnownVarSet();
            }
        }
    }

    private static void initHGMDMap() {
        try {
            String sql = "SELECT * From " + hgmdTable;

            if (KnownVarCommand.isKnownVarPathogenicOnly) {
                sql = "SELECT * From " + hgmdTable + " "
                        + "WHERE variantClass = 'DM' "
                        + "AND is_in_clinvar = 0";
            }

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

            if (KnownVarCommand.isKnownVarPathogenicOnly) {
                sql = "SELECT * From " + clinVarTable
                        + " WHERE ClinSig like '%Pathogenic%' and ClinSig not like '%Conflicting_interpretations_of_pathogenicity%'";
            }

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");

                String HGVS = FormatManager.getString(rs.getString("HGVS"));
                String ClinSource = FormatManager.getString(rs.getString("ClinSource"));
                String AlleleOrigin = FormatManager.getInteger(FormatManager.getInt(rs, "AlleleOrigin"));
                String ClinRevStat = FormatManager.getString(rs.getString("ClinRevStat"));
                String ClinRevStar = FormatManager.getInteger(FormatManager.getInt(rs, "ClinRevStar"));
                String ClinSig = FormatManager.getString(rs.getString("ClinSig"));
                String ClinSigIncl = FormatManager.getString(rs.getString("ClinSigIncl"));
                String DiseaseDB = FormatManager.getString(rs.getString("DiseaseDB"));
                String DiseaseName = FormatManager.getString(rs.getString("DiseaseName"));
                String PubmedID = FormatManager.getString(rs.getString("PubmedID"));
                String rsID = FormatManager.getString(rs.getString("rsID"));

                ClinVar clinVar = new ClinVar(chr, pos, ref, alt,
                        HGVS,
                        ClinSource,
                        AlleleOrigin,
                        ClinRevStat,
                        ClinRevStar,
                        ClinSig,
                        ClinSigIncl,
                        DiseaseDB,
                        DiseaseName,
                        PubmedID,
                        rsID);

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
                String lastPathoLoc = rs.getString("lastPathoLoc");

                ClinVarPathoratio clinVarPathoratio = new ClinVarPathoratio(
                        indelCount,
                        copyCount,
                        snvSpliceCount,
                        snvNonsenseCount,
                        snvMissenseCount,
                        lastPathoLoc);

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

    public static void initOMIMMap() {
        if (!omimMap.isEmpty()) {
            return;
        }

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
            preparedStatement4HGMDIndelFlankingCount.setString(1, var.getChrStr());
            preparedStatement4HGMDIndelFlankingCount.setInt(2, var.getStartPosition() - width);
            preparedStatement4HGMDIndelFlankingCount.setInt(3, var.getStartPosition() + width);
            ResultSet rs = preparedStatement4HGMDIndelFlankingCount.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.INTEGER_NA;
    }

    public static String getHGMDBySite(Variant var, int distance) {
        try {
            if (var.isSnv()) { // only query for SNVs
                StringBuilder sb = new StringBuilder();
                int count = 0;
                preparedStatement4HGMDSite.setString(1, var.getChrStr());
                preparedStatement4HGMDSite.setInt(2, var.getStartPosition() + distance);
                ResultSet rs = preparedStatement4HGMDSite.executeQuery();
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

        return Data.STRING_NA;
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
            preparedStatement4ClinVarPathogenicIndelFlankingCount.setString(1, var.getChrStr());
            preparedStatement4ClinVarPathogenicIndelFlankingCount.setInt(2, var.getStartPosition() - width);
            preparedStatement4ClinVarPathogenicIndelFlankingCount.setInt(3, var.getStartPosition() + width);
            ResultSet rs = preparedStatement4ClinVarPathogenicIndelFlankingCount.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.INTEGER_NA;
    }

    public static int getClinVarAllIndelFlankingCount(Variant var) {
        try {
            int width = 9;       
            preparedStatement4ClinVarAllIndelFlankingCount.setString(1, var.getChrStr());
            preparedStatement4ClinVarAllIndelFlankingCount.setInt(2, var.getStartPosition() - width);
            preparedStatement4ClinVarAllIndelFlankingCount.setInt(3, var.getStartPosition() + width);
            ResultSet rs = preparedStatement4ClinVarAllIndelFlankingCount.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.INTEGER_NA;
    }

    public static ClinVarPathoratio getClinPathoratio(String geneName) {
        ClinVarPathoratio clinVarPathoratio = clinVarPathoratioMap.get(geneName);

        if (clinVarPathoratio == null) {
            clinVarPathoratio = new ClinVarPathoratio(Data.INTEGER_NA, Data.INTEGER_NA,
                    Data.INTEGER_NA, Data.INTEGER_NA, Data.INTEGER_NA, Data.STRING_NA);
        }

        return clinVarPathoratio;
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            clinGen = new ClinGen(Data.STRING_NA, Data.STRING_NA);
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
