package function.external.gnomad;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class GnomADExomeOutput {

    GnomADExome gnomADExome;

    public static String getHeader() {
        return "Variant ID,"
                + GnomADManager.getExomeHeader();
    }

    public GnomADExomeOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        gnomADExome = new GnomADExome(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public GnomADExomeOutput(ResultSet rs) {
        gnomADExome = new GnomADExome(rs);
    }

    public boolean isValid() {
        return gnomADExome.isValid(false);
    }

    @Override
    public String toString() {
        return gnomADExome.toString();
    }
}
