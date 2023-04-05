package function.external.exac;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class ExACOutput {

    ExAC exac;

    public static String getHeader() {
        return "Variant ID,"
                + ExACManager.getHeader();
    }

    public ExACOutput(ResultSet rs) {
        exac = new ExAC(rs);
    }

    public boolean isValid() {
        return exac.isValid(null);
    }

    @Override
    public String toString() {
        return exac.toString();
    }
}
