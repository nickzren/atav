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

    String name;
    String boundary;
    String chr;
    ArrayList<Exon> exonList = new ArrayList<>();

    int index; // index for line in gene boundary file

    public Gene(String name) {
        this.name = name.trim();

        if (name.contains("(")) {
            boundary = this.name;
            this.name = boundary.substring(0, boundary.indexOf(" "));
        }

        initChr();
    }

    private void initChr() {
        try {
            String sql = "SELECT chrom FROM hgnc WHERE gene = '" + name + "'";

            ResultSet rset = DBManager.executeQuery(sql);

            if (rset.next()) {
                chr = rset.getString("chrom");
            }else{
                ErrorManager.print("Invalid gene: " + this.name);
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

    public void initExonList() {
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

    @Override
    public String toString() {
        return name;
    }
}
