package function.external.exac;

import global.Data;
import utils.ErrorManager;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class Exac {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float meanCoverage;
    private int sampleCovered10x;
    private float[] af;
    private String[] gts;
    private float vqslod;

    public Exac(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = ref.length() == alt.length();

        initCoverage();

        initAF();
    }

    public Exac(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");
            af = new float[ExacManager.EXAC_POP.length];
            gts = new String[ExacManager.EXAC_POP.length];
            
            isSnv = ref.length() == alt.length();

            initCoverage();
            
            setAF(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initCoverage() {
        try {
            String sql = ExacManager.getSql4Cvg(chr, pos);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                meanCoverage = rs.getFloat("mean_cvg");
                sampleCovered10x = rs.getInt("covered_10x");
            } else {
                meanCoverage = Data.FLOAT_NA;
                sampleCovered10x = Data.INTEGER_NA;
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initAF() {
        af = new float[ExacManager.EXAC_POP.length];
        gts = new String[ExacManager.EXAC_POP.length];

        try {
            String sql = ExacManager.getSqlByVariant(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                setAF(rs);
            } else if (meanCoverage > 0) {
                resetAF(0);
            } else {
                resetAF(Data.FLOAT_NA);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void setAF(ResultSet rs) throws SQLException {
        for (int i = 0; i < ExacManager.EXAC_POP.length; i++) {
            float af = rs.getFloat(ExacManager.EXAC_POP[i] + "_af");

            this.af[i] = af;
            gts[i] = rs.getString(ExacManager.EXAC_POP[i] + "_gts");
        }

        vqslod = rs.getFloat("vqslod");
    }

    private void resetAF(float value) {
        for (int i = 0; i < ExacManager.EXAC_POP.length; i++) {
            af[i] = value;
            gts[i] = Data.STRING_NA;
        }

        vqslod = Data.FLOAT_NA;
    }

    private float getMaxAF() {
        float value = Data.FLOAT_NA;

        for (int i = 0; i < ExacManager.EXAC_POP.length; i++) {
            if (af[i] != Data.FLOAT_NA
                    && ExacCommand.exacPop.contains(ExacManager.EXAC_POP[i])) {
                value = Math.max(value, af[i]);
            }
        }

        return value;
    }

    public boolean isValid() {
        return ExacCommand.isExacAFValid(getMaxAF())
                && ExacCommand.isExacVqslodValid(vqslod, isSnv)
                && ExacCommand.isExacMeanCoverageValid(meanCoverage);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        for (int i = 0; i < ExacManager.EXAC_POP.length; i++) {
            sj.add(FormatManager.getFloat(af[i]));

            if (gts[i].equals(Data.STRING_NA)) {
                sj.add(gts[i]);
            } else {
                sj.add("'" + gts[i] + "'");
            }
        }

        sj.add(FormatManager.getFloat(vqslod));
        sj.add(FormatManager.getFloat(meanCoverage));
        sj.add(FormatManager.getInteger(sampleCovered10x));

        return sj.toString();
    }
}
