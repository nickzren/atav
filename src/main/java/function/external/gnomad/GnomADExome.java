package function.external.gnomad;

import global.Data;
import utils.ErrorManager;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class GnomADExome {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float meanCoverage;
    private int sampleCovered10x;
    private float[] af;
    private String[] gts;
    private String filter;
    private float abMedian;
    private int gqMedian;
    private float asRf;

    public GnomADExome(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = true;

        if (ref.length() != alt.length()) {
            isSnv = false;
        }

        initCoverage();

        initAF();
    }

    public GnomADExome(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");

            isSnv = true;

            if (ref.length() != alt.length()) {
                isSnv = false;
            }

            af = new float[GnomADManager.GNOMAD_EXOME_POP.length];
            gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

            initCoverage();

            setAF(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initCoverage() {
        try {
            String sql = GnomADManager.getSql4Cvg(chr, pos);

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
        af = new float[GnomADManager.GNOMAD_EXOME_POP.length];
        gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

        try {
            String sql = GnomADManager.getSqlByVariant(chr, pos, ref, alt);

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
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            float af = rs.getFloat(GnomADManager.GNOMAD_EXOME_POP[i] + "_af");

            this.af[i] = af;
            gts[i] = rs.getString(GnomADManager.GNOMAD_EXOME_POP[i] + "_gts");
        }

        filter = rs.getString("filter");
        abMedian = rs.getFloat("AB_MEDIAN");
        gqMedian = rs.getInt("GQ_MEDIAN");
        asRf = rs.getFloat("AS_RF");
    }

    private void resetAF(float value) {
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            af[i] = value;
            gts[i] = Data.STRING_NA;
        }

        filter = Data.STRING_NA;
        abMedian = Data.FLOAT_NA;
        gqMedian = Data.INTEGER_NA;
        asRf = Data.FLOAT_NA;
    }

    private float getMaxAF() {
        float value = Data.FLOAT_NA;

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            if (af[i] != Data.FLOAT_NA
                    && GnomADCommand.gnomADExomePop.contains(GnomADManager.GNOMAD_EXOME_POP[i])) {
                value = Math.max(value, af[i]);
            }
        }

        return value;
    }

    public boolean isValid() {
        return GnomADCommand.isGnomADExomeAFValid(getMaxAF())
                && GnomADCommand.isGnomADExomeAsRfValid(asRf, isSnv);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            sb.append(FormatManager.getFloat(af[i])).append(",");

            if (gts[i].equals(Data.STRING_NA)) {
                sb.append(gts[i]).append(",");
            } else {
                sb.append("'").append(gts[i]).append("',");
            }
        }

        sb.append(filter).append(",");
        sb.append(FormatManager.getFloat(abMedian)).append(",");
        sb.append(FormatManager.getInteger(gqMedian)).append(",");
        sb.append(FormatManager.getFloat(asRf)).append(",");
        sb.append(FormatManager.getFloat(meanCoverage)).append(",");
        sb.append(FormatManager.getInteger(sampleCovered10x)).append(",");

        return sb.toString();
    }
}
