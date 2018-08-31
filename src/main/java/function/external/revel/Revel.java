package function.external.revel;

import global.Data;
import java.sql.ResultSet;
import java.util.StringJoiner;
import utils.DBManager;
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
    
    public Revel(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        
        initScore();
    }
    
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
    
    private void initScore() {
        try {
            String sql = RevelManager.getSqlByVariant(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                revel = rs.getFloat("revel");
            } else {
                revel = Data.FLOAT_NA;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    public boolean isValid() {
        return RevelCommand.isRevelValid(revel);
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
