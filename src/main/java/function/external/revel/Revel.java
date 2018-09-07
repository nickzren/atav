package function.external.revel;

import java.sql.ResultSet;
import java.util.StringJoiner;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Revel {
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private String aaref;
    private String aaalt;
    private float revel;
    
    public Revel(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref");
            alt = rs.getString("alt");
            aaref = rs.getString("aaref");
            aaalt = rs.getString("aaalt");
            revel = rs.getFloat("revel");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    public boolean isValid() {
        return RevelCommand.isMinRevelValid(revel);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public String getRevel() {
        return FormatManager.getFloat(revel);
    }
    
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(getVariantId());
        sj.add(aaref);
        sj.add(aaalt);
        sj.add(getRevel());

        return sj.toString();
    }
}
