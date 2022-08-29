package function.external.evs;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class EvsOutput {

    Evs evs;

    public static String getHeader() {
        return "Variant ID,"
                + EvsManager.getHeader();
    }

    public EvsOutput(ResultSet rs) {
        evs = new Evs(rs);
    }

    public boolean isValid() {
        return evs.isValid(false);
    }

    @Override
    public String toString() {
        return evs.toString();
    }
}
