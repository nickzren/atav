package function.external.mtr;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class MTR {

    private String chr;
    private int pos;
    private String feature; // transcript stable id
    private float mtr;
    private float fdr;
    private float mtrCentile;

    public MTR(String chr, int pos, String transcript) {
        this.chr = chr;
        this.pos = pos;
        this.feature = transcript;

        initMTR();
    }

    public MTR(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("Genomic_position");

            initMTR();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initMTR() {
        try {
            String sql = MTRManager.getSql4MTR(chr, pos, feature);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                mtr = getFloat((Float) rs.getObject("MTR"));
                fdr = getFloat((Float) rs.getObject("FDR"));
                mtrCentile = getFloat((Float) rs.getObject("MTR_centile"));
            } else {
                mtr = Data.NA;
                fdr = Data.NA;
                mtrCentile = Data.NA;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getVariantPos() {
        return chr + "-" + pos;
    }

    public boolean isValid() {
        return MTRCommand.isMTRValid(mtr)
                && MTRCommand.isFDRValid(fdr)
                && MTRCommand.isMTRCentileValid(mtrCentile);
    }

    private float getFloat(Float f) {
        if (f == null) {
            return Data.NA;
        }

        return f;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getFloat(mtr)).append(",");
        sb.append(FormatManager.getFloat(fdr)).append(",");
        sb.append(FormatManager.getFloat(mtrCentile)).append(",");

        return sb.toString();
    }
}
