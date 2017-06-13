package function.external.gnomad;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class GnomADExomeOutput {

    GnomADExome gnomADExome;

    public static String getTitle() {
        return "Variant ID,"
                + GnomADManager.getExomeTitle();
    }

    public GnomADExomeOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        gnomADExome = new GnomADExome(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public GnomADExomeOutput(boolean isIndel, ResultSet rs) {
        gnomADExome = new GnomADExome(isIndel, rs);
    }

    public boolean isValid() {
        return gnomADExome.isValid();
    }

    @Override
    public String toString() {
        return gnomADExome.toString();
    }
}
