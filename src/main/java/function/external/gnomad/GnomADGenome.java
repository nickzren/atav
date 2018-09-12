package function.external.gnomad;

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
public class GnomADGenome {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    private float[] maf;
    private String[] gts;
    private String filter;
    private float abMedian;
    private int gqMedian;
    private float asRf;

    public GnomADGenome(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = ref.length() == alt.length();

        initMaf();
    }

    public GnomADGenome(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");
            maf = new float[GnomADManager.GNOMAD_GENOME_POP.length];
            gts = new String[GnomADManager.GNOMAD_GENOME_POP.length];

            isSnv = ref.length() == alt.length();

            setMaf(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initMaf() {
        maf = new float[GnomADManager.GNOMAD_GENOME_POP.length];
        gts = new String[GnomADManager.GNOMAD_GENOME_POP.length];

        try {
            String sql = GnomADManager.getSql4MafGenome(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                setMaf(rs);
            } else {
                resetMaf(Data.FLOAT_NA);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void setMaf(ResultSet rs) throws SQLException {
        for (int i = 0; i < GnomADManager.GNOMAD_GENOME_POP.length; i++) {
            float af = rs.getFloat(GnomADManager.GNOMAD_GENOME_POP[i] + "_af");

            if (af > 0.5) {
                af = 1 - af;
            }

            maf[i] = af;
            gts[i] = rs.getString(GnomADManager.GNOMAD_GENOME_POP[i] + "_gts");
        }

        filter = rs.getString("filter");
        abMedian = rs.getFloat("AB_MEDIAN");
        gqMedian = rs.getInt("GQ_MEDIAN");
        asRf = rs.getFloat("AS_RF");
    }

    private void resetMaf(float value) {
        for (int i = 0; i < GnomADManager.GNOMAD_GENOME_POP.length; i++) {
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

        for (int i = 0; i < GnomADManager.GNOMAD_GENOME_POP.length; i++) {
            if (maf[i] != Data.FLOAT_NA
                    && GnomADCommand.gnomADGenomePop.contains(GnomADManager.GNOMAD_GENOME_POP[i])) {
                value = Math.max(value, maf[i]);
            }
        }

        return value;
    }

    public boolean isValid() {
        return GnomADCommand.isGnomADGenomeMafValid(getMaxMaf())
                && GnomADCommand.isGnomADGenomeAsRfValid(asRf, isSnv)
                && GnomADCommand.isGnomADGenomeABMedianValid(abMedian);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        for (int i = 0; i < GnomADManager.GNOMAD_GENOME_POP.length; i++) {
            sj.add(FormatManager.getFloat(maf[i]));

            if (gts[i].equals("NA")) {
                sj.add(gts[i]);
            } else {
                sj.add("'" + gts[i] + "'");
            }
        }

        sj.add(filter);
        sj.add(FormatManager.getFloat(abMedian));
        sj.add(FormatManager.getInteger(gqMedian));
        sj.add(FormatManager.getFloat(asRf));

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
