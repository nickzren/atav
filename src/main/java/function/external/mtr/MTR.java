package function.external.mtr;

import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
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
    private float mtr;
    private float mtrFDR;
    private float mtrCentile;

    public MTR(String chr, int pos) {
        this.chr = chr;
        this.pos = pos;

        initMTR();
    }

    public MTR(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");

            initMTR();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public MTR(String chr, int pos, CSVRecord record) {
        this.chr = chr;
        this.pos = pos;

        mtr = FormatManager.getFloat(record, "MTR");
        mtrFDR = FormatManager.getFloat(record, "MTR FDR");
        mtrCentile = FormatManager.getFloat(record, "MTR Centile");
    }

    private void initMTR() {
        try {
            PreparedStatement preparedStatement = MTRManager.getPreparedStatement4Site();
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                mtr = getFloat((Float) rs.getObject("MTR"));
                mtrFDR = getFloat((Float) rs.getObject("FDR"));
                mtrCentile = getFloat((Float) rs.getObject("MTR_centile"));
            } else {
                mtr = Data.FLOAT_NA;
                mtrFDR = Data.FLOAT_NA;
                mtrCentile = Data.FLOAT_NA;
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getVariantPos() {
        return chr + "-" + pos;
    }

    public boolean isValid() {
        return MTRCommand.isMaxMTRValid(mtr)
                && MTRCommand.isMaxMTRFDRValid(mtrFDR)
                && MTRCommand.isMaxMTRCentileValid(mtrCentile);
    }

    private float getFloat(Float f) {
        if (f == null) {
            return Data.FLOAT_NA;
        }

        return f;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getFloat(mtr));
        sj.add(FormatManager.getFloat(mtrFDR));
        sj.add(FormatManager.getFloat(mtrCentile));

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
