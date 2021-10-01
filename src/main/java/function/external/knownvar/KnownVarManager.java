package function.external.knownvar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import function.external.base.DataManager;
import function.variant.base.Variant;
import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class KnownVarManager {

    public static final String hgmdTable = "knownvar.hgmd_2021_2";
    public static final String clinVarTable = "knownvar.clinvar_2021_08_31";
    public static final String clinVarPathoratioTable = "knownvar.clinvar_pathoratio_2021_08_31";
    private static final Multimap<String, HGMD> hgmdMultiMap = ArrayListMultimap.create();
    private static final Multimap<String, ClinVar> clinVarMultiMap = ArrayListMultimap.create();
    private static final HashMap<String, ClinVarPathoratio> clinVarPathoratioMap = new HashMap<>();

    private static PreparedStatement preparedStatement4HGMDVariantFlankingCount;
    private static PreparedStatement preparedStatement4ClinVarPLPVariantFlankingCount;

    private static void initPreparedStatement() {
        String sql = "SELECT count(*) as count From " + hgmdTable
                + " WHERE chr=? AND pos BETWEEN ? AND ?"
                + " AND variantClass like '%DM%' AND is_in_clinvar = 0";
        preparedStatement4HGMDVariantFlankingCount = DBManager.initPreparedStatement(sql);

        sql = "SELECT count(*) as count "
                + "From " + clinVarTable + " "
                + "WHERE chr=? "
                + "AND pos BETWEEN ? AND ? "
                + "AND (ClinSig like 'Pathogenic%' "
                + "OR ClinSig like 'Likely_pathogenic%' "
                + "OR (ClinSig like 'Conflicting_interpretations_of_pathogenicity%' AND (ClinSigConf like 'pathogenic%' OR ClinSigConf like 'Likely_pathogenic%')))";
        preparedStatement4ClinVarPLPVariantFlankingCount = DBManager.initPreparedStatement(sql);
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("HGMD DM Site Count");
        sj.add("HGMD DM 2bpflanks Count");
        sj.add("HGMD PMID");
        sj.add("HGMD Class");
        sj.add("ClinVar PLP Site Count");
        sj.add("ClinVar PLP 2bpflanks Count");
        sj.add("ClinVar PLP 25bpflanks Count");
        sj.add("ClinVar ClinRevStar");
        sj.add("ClinVar ClinSig");
        sj.add("ClinVar ClinSigConf");
        sj.add("ClinVar Pathogenic Indel Count");
        sj.add("Clinvar Pathogenic CNV Count");
        sj.add("ClinVar Pathogenic SNV Splice Count");
        sj.add("ClinVar Pathogenic SNV Nonsense Count");
        sj.add("ClinVar Pathogenic SNV Missense Count");
        sj.add("ClinVar Pathogenic Last Pathogenic Location");

        return sj.toString();
    }

    public static String getVersion() {
        return "HGMD: " + DataManager.getVersion(hgmdTable) + "\n"
                + "ClinVar: " + DataManager.getVersion(clinVarTable) + "\n"
                + "ClinVarPathoratio: " + DataManager.getVersion(clinVarPathoratioTable) + "\n";

    }

    public static void init() throws SQLException {
        if (KnownVarCommand.isInclude) {
            initPreparedStatement();

            initHGMDMap();

            initClinVarMap();

            initClinVarPathoratioMap();

//            initRecessiveCarrierMap();
//            initDBDSMMap();
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
                byte isInClinVar = FormatManager.getByte(rs, "is_in_clinvar");

                HGMD hgmd = new HGMD(chr, pos, ref, alt,
                        variantClass, pmid, diseaseName, isInClinVar);

                hgmdMultiMap.put(hgmd.getSiteId(), hgmd);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initClinVarMap() {
        try {
            String sql = "SELECT chr,pos,ref,alt,ClinRevStar,ClinSig,ClinSigConf,DiseaseName From " + clinVarTable;

            ResultSet rs = DBManager.executeQuery(sql);

            while (rs.next()) {
                String chr = rs.getString("chr");
                int pos = rs.getInt("pos");
                String ref = rs.getString("ref");
                String alt = rs.getString("alt");

                String ClinRevStar = FormatManager.getInteger(FormatManager.getInt(rs, "ClinRevStar"));
                String ClinSig = FormatManager.getString(rs.getString("ClinSig"));
                String ClinSigConf = FormatManager.getString(rs.getString("ClinSigConf"));
                String DiseaseName = FormatManager.getString(rs.getString("DiseaseName"));

                ClinVar clinVar = new ClinVar(chr, pos, ref, alt,
                        ClinRevStar,
                        ClinSig,
                        ClinSigConf,
                        DiseaseName);

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

//    private static void initRecessiveCarrierMap() {
//        try {
//            String sql = "SELECT * From " + recessiveCarrierTable;
//
//            ResultSet rs = DBManager.executeQuery(sql);
//
//            while (rs.next()) {
//                String geneName = rs.getString("geneName").toUpperCase();
//
//                recessiveCarrierSet.add(geneName);
//            }
//
//            rs.close();
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
//    private static void initDBDSMMap() {
//        try {
//            String sql = "SELECT * From " + dbDSMTable;
//
//            ResultSet rs = DBManager.executeQuery(sql);
//
//            while (rs.next()) {
//                String chr = rs.getString("chr");
//                int pos = rs.getInt("pos");
//                String ref = rs.getString("ref");
//                String alt = rs.getString("alt");
//                String disease = FormatManager.getString(rs.getString("Disease"));
//                String classification = FormatManager.getString(rs.getString("Classification"));
//                String pubmedID = FormatManager.getString(rs.getString("PubmedID"));
//
//                DBDSM dbDSM = new DBDSM(chr, pos, ref, alt,
//                        disease, classification, pubmedID);
//
//                dbDSMMultiMap.put(dbDSM.getSiteId(), dbDSM);
//            }
//
//            rs.close();
//        } catch (Exception e) {
//            ErrorManager.send(e);
//        }
//    }
    public static Multimap<String, HGMD> getHGMDMultiMap() {
        return hgmdMultiMap;
    }

    public static HGMDOutput getHGMDOutput(Variant var) {
        Collection<HGMD> collection = hgmdMultiMap.get(var.getSiteId());

        HGMDOutput output = new HGMDOutput(var, collection);

        return output;
    }

    public static int getHGMDFlankingCount(Variant var, int width) {
        try {
            preparedStatement4HGMDVariantFlankingCount.setString(1, var.getChrStr());
            preparedStatement4HGMDVariantFlankingCount.setInt(2, var.getStartPosition() - width);
            preparedStatement4HGMDVariantFlankingCount.setInt(3, var.getStartPosition() + width);
            ResultSet rs = preparedStatement4HGMDVariantFlankingCount.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.INTEGER_NA;
    }

    public static Multimap<String, ClinVar> getClinVarMultiMap() {
        return clinVarMultiMap;
    }

    public static ClinVarOutput getClinVarOutput(Variant var) {
        Collection<ClinVar> collection = clinVarMultiMap.get(var.getSiteId());

        ClinVarOutput output = new ClinVarOutput(var, collection);

        return output;
    }

    public static int getClinVarPathogenicVariantFlankingCount(Variant var, int width) {
        try {
            preparedStatement4ClinVarPLPVariantFlankingCount.setString(1, var.getChrStr());
            preparedStatement4ClinVarPLPVariantFlankingCount.setInt(2, var.getStartPosition() - width);
            preparedStatement4ClinVarPLPVariantFlankingCount.setInt(3, var.getStartPosition() + width);
            ResultSet rs = preparedStatement4ClinVarPLPVariantFlankingCount.executeQuery();
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

//    public static int getRecessiveCarrier(String geneName) {
//        if (recessiveCarrierSet.contains(geneName)) {
//            return 1;
//        }
//
//        return 0;
//    }
//    public static Multimap<String, DBDSM> getDBDSMMultiMap() {
//        return dbDSMMultiMap;
//    }
//    public static DBDSMOutput getDBDSMOutput(Variant var) {
//        Collection<DBDSM> collection = dbDSMMultiMap.get(var.getSiteId());
//
//        DBDSMOutput output = new DBDSMOutput(var, collection);
//
//        return output;
//    }
}
