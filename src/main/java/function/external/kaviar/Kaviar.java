package function.external.kaviar;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Kaviar {

    private String variantId;
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float alleleFreq;
    private int alleleCount;
    private int alleleNumber;

    public Kaviar(String id) {
        initBasic(id);

        initKaviarValue();
    }

    private void initBasic(String id) {
        variantId = id;

        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }

    private void initKaviarValue() {
        try {
            String sql = KaviarManager.getSql(isSnv, chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                alleleFreq = rs.getFloat("allele_frequency");
                alleleCount = rs.getInt("allele_count");
                alleleNumber = rs.getInt("allele_number");
            } else {
                alleleFreq = Data.NA;
                alleleCount = Data.NA;
                alleleNumber = Data.NA;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public float getAlleleFreq() {
        return alleleFreq;
    }

    public int getAlleleCount() {
        return alleleCount;
    }

    public int getAlleleNumber() {
        return alleleNumber;
    }

    public boolean isValid() {
        if (KaviarCommand.isMaxAlleleFreqValid(alleleFreq)
                && KaviarCommand.isMaxAlleleCountValid(alleleCount)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(variantId).append(",");
        sb.append(FormatManager.getDouble(alleleFreq)).append(",");
        sb.append(FormatManager.getInteger(alleleCount)).append(",");
        sb.append(FormatManager.getInteger(alleleNumber));

        return sb.toString();
    }
}
