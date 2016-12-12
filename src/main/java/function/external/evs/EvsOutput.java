package function.external.evs;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class EvsOutput {

    Evs evs;

    public static String getTitle() {
        return "Variant ID,"
                + EvsManager.getTitle();
    }

    public EvsOutput(ResultSet rs) {
        evs = new Evs(rs);
    }

    public boolean isValid() {
        return evs.isValid();
    }

    @Override
    public String toString() {
        return evs.toString();
    }
}
