package function.external.gnomad;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class GnomADGenomeOutput {

    GnomADGenome gnomADGenome;

    public static String getTitle() {
        return "Variant ID,"
                + GnomADManager.getGenomeTitle();
    }

    public GnomADGenomeOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        gnomADGenome = new GnomADGenome(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public GnomADGenomeOutput(boolean isIndel, ResultSet rs) {
        gnomADGenome = new GnomADGenome(isIndel, rs);
    }

    public boolean isValid() {
        return gnomADGenome.isValid();
    }

    @Override
    public String toString() {
        return gnomADGenome.toString();
    }
}
