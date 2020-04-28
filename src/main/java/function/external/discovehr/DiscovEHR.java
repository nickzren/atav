package function.external.discovehr;

import global.Data;
import java.sql.ResultSet;
import org.apache.commons.csv.CSVRecord;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DiscovEHR {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private float af;

    public DiscovEHR(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        initAF();
    }

    public DiscovEHR(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");

            initAF();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public DiscovEHR(CSVRecord record) {                
        af = FormatManager.getFloat(record, "DiscovEHR AF");
    }
    
    private void initAF() {
        try {
            String sql = DiscovEHRManager.getSql4AF(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                af = rs.getFloat("af");
            } else {
                af = Data.FLOAT_NA;
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public boolean isValid() {
        return DiscovEHRCommand.isDiscovEHRAFValid(af);
    }

    @Override
    public String toString() {
        return FormatManager.getFloat(af);
    }
}
