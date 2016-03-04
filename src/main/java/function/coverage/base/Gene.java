package function.coverage.base;

import function.variant.base.Region;
import global.Data;
import function.annotation.base.GeneManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class Gene {

    String name;
    String nameType;
    String chr;
    InputList exonList = null;
    Region translatedRegion = null;

    public Gene(String name) {
        this.name = name.trim();

        if (name.startsWith("ENSG")) {
            nameType = "stable_id";
        } else if (name.contains("(")) {
            nameType = "Slave"; //for now
        } else {
            nameType = "symbol";
        }

        initChr();
    }

    private void initChr() {
        try {
            chr = "";

            String geneChrSql = SqlQuery.GENE_CHR.replaceAll("_GENE_", name);

            String sql = geneChrSql.replaceAll("_VAR_TYPE_", "snv");

            ResultSet rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                chr = rset.getString("name");
            } else {
                sql = geneChrSql.replaceAll("_VAR_TYPE_", "indel");

                rset = DBManager.executeQuery(sql);

                if (rset.next()) {
                    chr = rset.getString("name");
                }
            }

            rset.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getChr() {
        return chr;
    }
    
    public int getStartPosition() {
        int start = 0;
        if (exonList == null) { // gene name only file
            return start;
        }

        if (exonList.isEmpty()) { // gene name & valid regions file
            return start;
        }
        start = Integer.MAX_VALUE;
        for (Iterator it = exonList.iterator(); it.hasNext();) {
            Exon exon = (Exon) it.next();
            int current = exon.covRegion.getStartPosition();
            if (current < start) {
                start = current;
            }
        }
        return start;
    }

    public int getEndPosition() {
        int end = 0;
        if (exonList == null) { // gene name only file
            return end;
        }

        if (exonList.isEmpty()) { // gene name & valid regions file
            return end;
        }
        for (Iterator it = exonList.iterator(); it.hasNext();) {
            Exon exon = (Exon) it.next();
            int current = exon.covRegion.getEndPosition();
            if (current > end) {
                end = current;
            }
        }
        return end;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return nameType;
    }

    public boolean isValid() {
        if (name.length() > 0) {
            return GeneManager.isValid(getStdName());
        }

        return false;
    }

    public boolean contains(Region r) {
        if (exonList == null) { // gene name only file
            return true;
        }

        if (exonList.isEmpty()) { // gene name & valid regions file
            return false;
        }

        for (Iterator it = exonList.iterator(); it.hasNext();) {
            Exon exon = (Exon) it.next();
            if (exon.contains(r)) {
                return true;
            }
        }

        return false;
    }

    private int getFirstCCDSTranscriptId() throws Exception {
        String str;
        if (nameType.equalsIgnoreCase("Slave") || nameType.equals("stable_id")) {
            System.out.println("Should not happen");
            return Data.NA;
        }

        str = "SELECT t.transcript_id "
                + "FROM _DB_HSC_.transcript t, "
                + "_DB_HSC_.object_xref ox, _DB_HSC_.xref x "
                + "WHERE t.gene_id = ox.ensembl_id "
                + "AND ox.ensembl_object_type = 'Gene' "
                + "AND ox.xref_id = x.xref_id "
                + "AND external_db_id = 1100 "
                + "AND display_label = '_GENE_' "
                + "AND t.stable_id like 'ENST%' ";

        str = str.replaceAll("_GENE_", name);
        str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);

        ArrayList<Integer> candidates = DBUtils.getIntList(str, "transcript_id");
        for (int i = 0; i < candidates.size(); i++) {
            int transcriptid = candidates.get(i);
            if (isCCDS(transcriptid)) {
                return transcriptid;
            }
        }
        return Data.NA;
    }

    public int getCanonicalTranscriptId() throws Exception {
        String str;
        if (nameType.equalsIgnoreCase("Slave")) {
            System.out.println("Should not happen");
            return Data.NA;
        }
        if (nameType.equals("stable_id")) {
            str = "SELECT t.transcript_id "
                    + "FROM _DB_HSC_.transcript t, _DB_HSC_.gene g "
                    + "WHERE t.gene_id = g.gene_id "
                    + "AND t.transcript_id = g.canonical_transcript_id "
                    + "AND g.stable_id = '_GENE_' ";
        } else {
            str = "SELECT t.transcript_id "
                    + "FROM _DB_HSC_.transcript t, _DB_HSC_.gene g, "
                    + "_DB_HSC_.object_xref ox, _DB_HSC_.xref x "
                    + "WHERE t.gene_id = ox.ensembl_id "
                    + "AND t.transcript_id = g.canonical_transcript_id "
                    + "AND ox.ensembl_object_type = 'Gene' "
                    + "AND ox.xref_id = x.xref_id "
                    + "AND external_db_id = 1100 "
                    + "AND display_label = '_GENE_' "
                    + "AND t.stable_id like 'ENST%' ";
        }
        str = str.replaceAll("_GENE_", name);
        str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);

        return DBUtils.getUniqueInt(str, "transcript_id");
    }

    public boolean isCCDS(int transcriptid) throws Exception {
        String str = "SELECT t.transcript_id "
                + "FROM _DB_HSC_.transcript t, _DB_HSC_.object_xref ox,_DB_HSC_.xref x "
                + "WHERE transcript_id = ensembl_id "
                + "AND x.xref_id = ox.xref_id "
                + "AND ensembl_object_type = 'Transcript' "
                + "AND external_db_id = 3800 "
                + "AND t.transcript_id ='_TRANSCRIPT_' ";

        str = str.replaceAll("_TRANSCRIPT_", new Integer(transcriptid).toString());
        str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);

        return DBUtils.isEmpty(str);
    }

    public boolean isCCDS() throws Exception {
        int ConalticalID = getCanonicalTranscriptId();
        if (ConalticalID == Data.NA) {
            return false;
        }
        return isCCDS(ConalticalID);
    }

    public HashMap<Integer, Double> getCoverageFromTable() {
        HashMap<Integer, Double> result = new HashMap<Integer, Double>();
        String strQuery = "SELECT sample_id, coverage_ratio FROM gene_coverage_summary c,"
                + SampleManager.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE gene = '" + name + "' AND c.sample_id = t.id "
                + "AND min_coverage = " + GenotypeLevelFilterCommand.minCoverage;

        try {
            ResultSet rs = DBManager.executeQuery(strQuery);

            while (rs.next()) {
                int sample_id = rs.getInt("sample_id");
                int coverage_ratio = rs.getInt("coverage_ratio");
                result.put(sample_id, (double) coverage_ratio / 10000.0); //scaled by 10000
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return result;
    }

    private void filterByUTR(Region translated) {
        if (translated != null) {
            InputList FilteredExons = new InputList();
            for (Iterator it = exonList.iterator(); it.hasNext();) {
                Exon exon = (Exon) it.next();
                CoveredRegion region = exon.getCoveredRegion().intersect(translated);
                if (region != null) {
                    exon.setRegion(region);
                    FilteredExons.add(exon);
                }
            }

            exonList = FilteredExons;
        }
    }

    public void filterByUTR() {
        Region translated = getTranslatedRegion();
        filterByUTR(translated);
    }

    public void filterByUTRFromTranscriptID(int transcriptid) {
        Region translated = getTranslatedRegionFromTranscriptID(transcriptid);
        filterByUTR(translated);
    }

    private String getStdName() {
        if (getType().equalsIgnoreCase("Slave")) {
            String[] fields = name.trim().replace("(", "").replace(")", "").split(" ");
            return fields[0];
        } else {
            return name;
        }
    }

    public void populateSlaveList() {
        //ExonList = dbUtil.getExonList(getExonString());

        exonList = new InputList();

        String[] fields = name.trim().replace("(", "").replace(")", "").split(" ");
        name = fields[0];
        chr = fields[1];

        int seq_region_id = 0;

        String[] exons = fields[2].trim().split(",");
        for (int i = 0; i < exons.length; i++) {
            int exon_id = i + 1;
            //System.out.println(exons[i]);
            String[] r = exons[i].split("\\W");
            int seq_region_start = Integer.parseInt(r[0]);
            int seq_region_end = Integer.parseInt(r[2]);
            //String chr = fields[1];
            String stable_id = "Exon_" + seq_region_start + "_" + seq_region_end;
            exonList.add(new Exon(exon_id, stable_id, seq_region_id, chr, seq_region_start, seq_region_end, ""));
        }
    }

    public void populateExonList() {
        exonList = DBUtils.getExonList(getExonString());
    }

    public int populateExonListFromTranscriptID() throws Exception {
        int transcriptid = getFirstCCDSTranscriptId();
        String sql = getExonStringfromTranscriptID(transcriptid);
        exonList = DBUtils.getExonList(sql);
        return transcriptid;
    }

    public Region getTranslatedRegion() {
        if (translatedRegion == null) {
            translatedRegion = DBUtils.getTranslatedRegion(getUTRString());
        }
        return translatedRegion;
    }

    public Region getTranslatedRegionFromTranscriptID(int transcriptid) {
        if (translatedRegion == null) {
            translatedRegion = DBUtils.getTranslatedRegion(getUTRStringFromTranscriptID(transcriptid));
        }
        return translatedRegion;
    }

    public InputList getExonList() {
        if (exonList == null) {
            exonList = new InputList();
        }
        return exonList;
    }

    private String getExonStringfromTranscriptID(int transcriptid) { //please refactor this name if necessary
        if (isValid() && transcriptid > 0) {
            String str = SqlQuery.GENE_EXON_TRANSCRIPTID;
            str = str.replaceAll("_TRANSCRIPTID_", new Integer(transcriptid).toString());
            str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);
            return str;
        } else {
            return "";
        }
    }

    public String getExonString() {
        if (isValid()) {
            String str;

            if (nameType.equals("symbol")) {
                str = SqlQuery.GENE_EXON_NAME;

            } else {
                str = SqlQuery.GENE_EXON_STABLEID;
            }
            str = str.replaceAll("_GENE_", name);
            str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);
            return str;
        } else {
            return "";
        }
    }

    public String getUTRString() {  //should be merged with getExon 
        if (isValid()) {
            String str;
            if (nameType.equals("symbol")) {
                str = SqlQuery.GENE_UTR_NAME;

            } else {
                str = SqlQuery.GENE_UTR_STABLEID;
            }
            str = str.replaceAll("_GENE_", name);
            str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);
            return str;
        } else {
            return "";
        }
    }

    public String getUTRStringFromTranscriptID(int transcriptid) {  //should be merged with getExon 
        if (isValid()) {
            String str = SqlQuery.GENE_UTR_TRANSCRIPTID;
            str = str.replaceAll("_GENE_", name);
            str = str.replaceAll("_TRANSCRIPTID_", new Integer(transcriptid).toString());
            str = str.replaceAll("_DB_HSC_", DBManager.homoSapiensCoreName);
            return str;
        } else {
            return "";
        }
    }

    public int getLength() {
        int CumResult = 0;
        if (exonList != null) {
            for (Iterator it = exonList.iterator(); it.hasNext();) {
                Exon exon = (Exon) it.next();
                CumResult = CumResult + exon.getCoveredRegion().getLength();
            }
        }
        return CumResult;
    }

    public String getChrFromExon() throws Exception {
        if (exonList != null && exonList.size() > 0) {
            Iterator it = exonList.iterator();
            Exon exon = (Exon) it.next();
            return exon.covRegion.getChrStr();
        } else {
            return chr;
        }

    }

    public String getChrStr() {
        return chr;
    }

    @Override
    public String toString() {
        if (name.contains(" ")) {
            return name.substring(0, name.indexOf(" "));
        } else {
            return name;
        }
    }

    private String parse(String r) {
        return r.trim();
//        String[] items = r.trim().toUpperCase().replaceAll("\\W+", ".").split("\\W");
//        if (items.length > 0) {
//            return items[0];
//        } else {
//            return "";
//        }
    }
}
