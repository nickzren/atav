package function.external.exac;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ExacOutput {

    Exac exac;

    public static String getTitle() {
        return "Variant ID,"
                + ExacManager.getTitle();
    }

    public ExacOutput(ResultSet rs) {
        exac = new Exac(rs);
    }

    public boolean isValid() {
        return exac.isValid();
    }

    @Override
    public String toString() {
        return exac.toString();
    }
}
