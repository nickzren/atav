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

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float maf;
    private int alleleCount;
    private int alleleNumber;

    public Kaviar(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = ref.length() == alt.length();

        initKaviarValue();
    }

    public Kaviar(boolean isIndel, ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref");
            alt = rs.getString("alt");

            isSnv = !isIndel;

            maf = rs.getFloat("allele_frequency");

            if (maf > 0.5) {
                maf = 1 - maf;
            }

            alleleCount = rs.getInt("allele_count");
            alleleNumber = rs.getInt("allele_number");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initKaviarValue() {
        try {
            String sql = KaviarManager.getSql(isSnv, chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                maf = rs.getFloat("allele_frequency");

                if (maf > 0.5) {
                    maf = 1 - maf;
                }

                alleleCount = rs.getInt("allele_count");
                alleleNumber = rs.getInt("allele_number");
            } else {
                maf = Data.FLOAT_NA;
                alleleCount = Data.INTEGER_NA;
                alleleNumber = Data.INTEGER_NA;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public float getMaf() {
        return maf;
    }

    public int getAlleleCount() {
        return alleleCount;
    }

    public int getAlleleNumber() {
        return alleleNumber;
    }

    public boolean isValid() {
        if (KaviarCommand.isMaxMafValid(maf)
                && KaviarCommand.isMaxAlleleCountValid(alleleCount)) {
            return true;
        }

        return false;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getFloat(maf)).append(",");
        sb.append(FormatManager.getInteger(alleleCount)).append(",");
        sb.append(FormatManager.getInteger(alleleNumber)).append(",");

        return sb.toString();
    }
}
