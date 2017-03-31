package function.external.gnomad;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class GnomADOutput {

    GnomADExome gnomADExome;

    public static String getTitle() {
        return "Variant ID,"
                + GnomADManager.getTitle();
    }

    public GnomADOutput(ResultSet rs) {
        gnomADExome = new GnomADExome(rs);
    }

    public boolean isValid() {
        return gnomADExome.isValid();
    }

    @Override
    public String toString() {
        return gnomADExome.toString();
    }
}