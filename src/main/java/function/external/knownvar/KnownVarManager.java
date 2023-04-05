package function.external.knownvar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import function.external.base.DataManager;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    public static final String hgmdTable = "knownvar.hgmd_2022_4";
    public static final String clinVarTable = "knownvar.clinvar_2023_03_20";
    public static final String clinVarPathoratioTable = "knownvar.clinvar_pathoratio_2023_03_20";
    public static final String clingenFile = Data.ATAV_HOME + "data/clingen/ClinGen_gene_curation_list_GRCh37.tsv";
    public static final String genemap2File = Data.ATAV_HOME + "data/omim/genemap2.txt";
    public static final String recessiveCarrierTable = "knownvar.RecessiveCarrier_2015_12_09";
    public static final String acmgTable = "knownvar.acmg_v3_1";
    public static final String dbDSMTable = "knownvar.dbDSM_2016_09_28";

    private static final Multimap<String, HGMD> hgmdMultiMap = ArrayListMultimap.create();
    private static final Multimap<String, ClinVar> clinVarMultiMap = ArrayListMultimap.create();
    private static final HashMap<String, ClinVarPathoratio> clinVarPathoratioMap = new HashMap<>();
    private static final HashMap<String, String> clinGenMap = new HashMap<>();
    private static final HashMap<String, String> omimMap = new HashMap<>();
    private static final HashSet<String> recessiveCarrierSet = new HashSet<>();
    private static final HashMap<String, String> acmgMap = new HashMap<>();
    private static final Multimap<String, DBDSM> dbDSMMultiMap = ArrayListMultimap.create();

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
                + "OR (ClinSig like 'Conflicting_interpretations_of_pathogenicity%' AND (ClinSigConf like '%pathogenic%' and ClinSigConf not like '%benign%')))";
        preparedStatement4ClinVarPLPVariantFlankingCount = DBManager.initPreparedStatement(sql);
    }

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("HGMD DM Site Count");
        sj.add("HGMD DM 2bpflanks Count");
        sj.add("HGMD Disease");
        sj.add("HGMD PMID");
        sj.add("HGMD Class");
        sj.add("ClinVar PLP Site Count");
        sj.add("ClinVar PLP 2bpflanks Count");
        sj.add("ClinVar PLP 25bpflanks Count");
        sj.add("ClinVar ClinRevStar");
        sj.add("ClinVar Benign");
        sj.add("ClinVar ClinSig");
        sj.add("ClinVar ClinSigConf");
        sj.add("ClinVar DiseaseName");
        sj.add(getGeneHeader());

        return sj.toString();
    }

    public static String getGeneHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("ClinVar Pathogenic Indel Count");
        sj.add("Clinvar Pathogenic CNV Count");
        sj.add("ClinVar Pathogenic SNV Splice Count");
        sj.add("ClinVar Pathogenic SNV Nonsense Count");
        sj.add("ClinVar Pathogenic SNV Missense Count");
        sj.add("ClinVar Pathogenic Last Pathogenic Location");
        sj.add("ClinGen");
        sj.add("OMIM Disease");
        sj.add("OMIM Inheritance");
        sj.add("ACMG Disease");

        return sj.toString();
    }

    public static String getVersion() {
        return "HGMD: " + DataManager.getVersion(hgmdTable) + "\n"
                + "ClinVar: " + DataManager.getVersion(clinVarTable) + "\n"
                + "ClinVarPathoratio: " + DataManager.getVersion(clinVarPathoratioTable) + "\n"
                + "ACMG: " + DataManager.getVersion(acmgTable) + "\n";
    }

    public static void init() throws SQLException {
        if (KnownVarCommand.isInclude) {
            initPreparedStatement();

            initHGMDMap();

            initClinVarMap();

            init4Gene();

            if (KnownVarCommand.isKnownVarOnly
                    || KnownVarCommand.isKnownVarPathogenicOnly) {
                VariantManager.reset2KnownVarSet();
            }
        }
    }

    public static void init4Gene() throws SQLException {
        initClinVarPathoratioMap();

        initClinGenMap();

        initOMIMMap();

        initACMGMap();
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

            if (KnownVarCommand.isKnownVarPathogenicOnly) {
                sql += " WHERE ClinSig like 'Pathogenic%' "
                        + "OR ClinSig like 'Likely_pathogenic%' "
                        + "OR (ClinSig like 'Conflicting_interpretations_of_pathogenicity%' AND (ClinSigConf like '%pathogenic%' and ClinSigConf not like '%benign%'))";
            }

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

    public static void main(String[] args) {
        initClinGenMap();
    }

    private static void initClinGenMap() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(clingenFile)));
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");

                String gene = tmp[0];
                String haploinsufficiencyDescription = tmp[5].replace("Dosage sensitivity unlikely", "Unlikely");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Gene associated with autosomal recessive phenotype", "Recessive evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("No evidence available", "No evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Little evidence for dosage pathogenicity", "Little evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Some evidence for dosage pathogenicity", "Some evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Sufficient evidence for dosage pathogenicity", "Sufficient evidence");

                clinGenMap.put(gene.toUpperCase(), haploinsufficiencyDescription);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initOMIMMap() {
        if (!omimMap.isEmpty()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(genemap2File)));
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");

                if (tmp.length < 13) {
                    continue;
                }

                String phenotype = tmp[12]; // Phenotypes

                String g = tmp[8]; // Approved Gene Symbol
                if (!g.isEmpty() && !phenotype.isEmpty()) {
                    omimMap.put(g.toUpperCase(), phenotype);
                }

                String[] geneSymbols = tmp[6].replaceAll("( )+", "").split(","); // Gene Symbols
                for (String gene : geneSymbols) {
                    if (!gene.isEmpty() && !phenotype.isEmpty() && !omimMap.containsKey(gene.toUpperCase())) {
                         omimMap.put(gene.toUpperCase(), phenotype);
                    }
                }
            }
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
                String gene = rs.getString("gene").toUpperCase();
                String acmg = rs.getString("ACMG");
                acmgMap.put(gene.toUpperCase(), acmg);
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

    public static String getClinGen(String geneName) {
        String value = clinGenMap.get(geneName);

        if (value == null) {
            return Data.STRING_NA;
        }

        return value;
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
