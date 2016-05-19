package function.coverage.base;

import function.variant.base.Region;
import function.annotation.base.GeneManager;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
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

        if (name.contains("(")) {
            nameType = "boundary";
        } else {
            nameType = "symbol";
        }

        initChr();
    }

    private void initChr() {
        try {
            chr = "";

            String GENE_CHR = "SELECT name "
                    + "FROM _VAR_TYPE__gene_hit g, _VAR_TYPE_ v, seq_region r "
                    + "WHERE g.gene_name = '_GENE_' "
                    + "AND g._VAR_TYPE__id = v._VAR_TYPE__id "
                    + "AND v.seq_region_id = r.seq_region_id "
                    + "AND coord_system_id = 2 "
                    + "LIMIT 1";

            String geneChrSql = GENE_CHR.replaceAll("_GENE_", name);

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

    private String getStdName() {
        if (getType().equalsIgnoreCase("boundary")) {
            String[] fields = name.trim().replace("(", "").replace(")", "").split(" ");
            return fields[0];
        } else {
            return name;
        }
    }

    public void populateSlaveList() {
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
            exonList.add(new Exon(exon_id, stable_id, seq_region_id, chr, seq_region_start, seq_region_end));
        }
    }

    public InputList getExonList() {
        if (exonList == null) {
            exonList = new InputList();
        }
        return exonList;
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
}
