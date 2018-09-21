package function.external.revel;

import java.sql.ResultSet;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class RevelOutput {
    
    Revel revel;
    
    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add("Reference Amino Acid");
        sj.add("Alternate Amino Acid");
        sj.add(RevelManager.getTitle());

        return sj.toString();
    }
    
    public RevelOutput(ResultSet rs) {
        revel = new Revel(rs);
    }

    public boolean isValid() {
        return revel.isValid();
    }

    @Override
    public String toString() {
        return revel.toString();
    }
}
