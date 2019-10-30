package function.external.gnomad;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class GnomADGenomeOutput {

    GnomADGenome gnomADGenome;

    public static String getHeader() {
        return "Variant ID,"
                + GnomADManager.getGenomeHeader();
    }

    public GnomADGenomeOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        gnomADGenome = new GnomADGenome(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public GnomADGenomeOutput(ResultSet rs) {
        gnomADGenome = new GnomADGenome(rs);
    }

    public boolean isValid() {
        return gnomADGenome.isValid();
    }

    @Override
    public String toString() {
        return gnomADGenome.toString();
    }
}
