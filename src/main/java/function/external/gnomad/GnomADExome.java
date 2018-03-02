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
    private float[] maf;
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

        isSnv = ref.length() == alt.length();

        initCoverage();

        initMaf();
    }

    public GnomADExome(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");
            maf = new float[GnomADManager.GNOMAD_EXOME_POP.length];
            gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

            isSnv = ref.length() == alt.length();

            initCoverage();

            setMaf(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initCoverage() {
        try {
            String sql = GnomADManager.getSql4CvgExome(chr, pos);

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

    private void initMaf() {
        maf = new float[GnomADManager.GNOMAD_EXOME_POP.length];
        gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

        try {
            String sql = GnomADManager.getSql4MafExome(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                setMaf(rs);
            } else if (meanCoverage > 0) {
                resetMaf(0);
            } else {
                resetMaf(Data.FLOAT_NA);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void setMaf(ResultSet rs) throws SQLException {
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            float af = rs.getFloat(GnomADManager.GNOMAD_EXOME_POP[i] + "_af");

            if (af > 0.5) {
                af = 1 - af;
            }

            maf[i] = af;
            gts[i] = rs.getString(GnomADManager.GNOMAD_EXOME_POP[i] + "_gts");
        }

        filter = rs.getString("filter");
        abMedian = rs.getFloat("AB_MEDIAN");
        gqMedian = rs.getInt("GQ_MEDIAN");
        asRf = rs.getFloat("AS_RF");
    }

    private void resetMaf(float value) {
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            maf[i] = value;
            gts[i] = "NA";
        }

        filter = "NA";
        abMedian = Data.FLOAT_NA;
        gqMedian = Data.INTEGER_NA;
        asRf = Data.FLOAT_NA;
    }

    private float getMaxMaf() {
        float value = Data.FLOAT_NA;

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            if (maf[i] != Data.FLOAT_NA
                    && GnomADCommand.gnomADExomePop.contains(GnomADManager.GNOMAD_EXOME_POP[i])) {
                value = Math.max(value, maf[i]);
            }
        }

        return value;
    }

    public boolean isValid() {
        return GnomADCommand.isGnomADExomeMafValid(getMaxMaf())
                && GnomADCommand.isGnomADExomeAsRfValid(asRf, isSnv)
                && GnomADCommand.isGnomADExomeABMedianValid(abMedian);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            sb.append(FormatManager.getFloat(maf[i])).append(",");

            if (gts[i].equals("NA")) {
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
