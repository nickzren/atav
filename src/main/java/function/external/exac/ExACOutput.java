package function.external.exac;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ExACOutput {

    ExAC exac;

    public static String getTitle() {
        return "Variant ID,"
                + ExACManager.getTitle();
    }

    public ExACOutput(ResultSet rs) {
        exac = new ExAC(rs);
    }

    public boolean isValid() {
        return exac.isValid();
    }

    @Override
    public String toString() {
        return exac.toString();
    }
}
