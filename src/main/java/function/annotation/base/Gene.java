package function.annotation.base;

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

        if (name.contains("(")) { // input gene boundary
            initExonList(this.name);
        } else { // input gene
            initChr();
        }
    }

    private void initChr() {
        try {
            String sql = "SELECT chrom FROM hgnc WHERE gene = '" + name + "'";

            ResultSet rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                chr = rset.getString("chrom");
            } else {
                isValid = false;
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

    public boolean contains(String chr, int pos) {
        return exonList.stream().anyMatch((exon) -> (exon.contains(chr, pos)));
    }

    private void initExonList(String boundary) {
        String[] fields = boundary.trim().replace("(", "").replace(")", "").split("( )+");
        name = fields[0];
        chr = fields[1];
        int count = 0;

        String[] exons = fields[2].trim().split(",");
        for (String exon : exons) {
            String[] r = exon.split("\\W");
            int seq_region_start = Integer.parseInt(r[0]);
            int seq_region_end = Integer.parseInt(r[2]);
            exonList.add(new Exon(count++, chr, seq_region_start, seq_region_end));
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
