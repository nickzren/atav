package function.annotation.base;

import function.variant.base.Region;
import utils.DBManager;
import utils.ErrorManager;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Gene {

    private String name;
    private String chr;
    private ArrayList<Exon> exonList = new ArrayList<>();

    private boolean isValid = true;

    private int index; // index for line in gene boundary file

    public Gene(String name) {
        this.name = name.trim();

        if (name.contains("(")) { // gene boundary input
            initExonList(this.name);
        } else { // gene input
            initChr();
        }
    }

    private void initChr() {
        try {
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
                } else {
                    isValid = false;
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

    public boolean contains(Region r) {
        return exonList.stream().anyMatch((exon) -> (exon.contains(r)));
    }

    private void initExonList(String boundary) {
        String[] fields = boundary.trim().replace("(", "").replace(")", "").split("( )+");
        name = fields[0];
        chr = fields[1];

        String[] exons = fields[2].trim().split(",");
        for (String exon : exons) {
            String[] r = exon.split("\\W");
            int seq_region_start = Integer.parseInt(r[0]);
            int seq_region_end = Integer.parseInt(r[2]);
            String idStr = "Exon_" + seq_region_start + "_" + seq_region_end;
            exonList.add(new Exon(idStr, chr, seq_region_start, seq_region_end));
        }
    }

    public ArrayList<Exon> getExonList() {
        return exonList;
    }

    public int getLength() {
        int length = 0;

        for (Exon exon : exonList) {
            length = length + exon.getLength();
        }

        return length;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return name;
    }
}
