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
public class GnomADExome {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;
    private boolean isMNV;

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

        isSnv = ref.length() == alt.length();
        
        isMNV = ref.length() > 1 && alt.length() > 1
                && alt.length() == ref.length();

        initAF();
    }

    public GnomADExome(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");
            af = new float[GnomADManager.GNOMAD_EXOME_POP.length];
            gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

            isSnv = ref.length() == alt.length();

            setAF(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initAF() {
        af = new float[GnomADManager.GNOMAD_EXOME_POP.length];
        gts = new String[GnomADManager.GNOMAD_EXOME_POP.length];

        try {
            String sql = GnomADManager.getSql4ExomeVariant(chr, pos, ref, alt, isMNV);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                setAF(rs);
            } else {
                resetAF(Data.FLOAT_NA);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void setAF(ResultSet rs) throws SQLException {
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            af[i] = rs.getFloat(GnomADManager.GNOMAD_EXOME_POP[i] + "_af");
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
            gts[i] = "NA";
        }

        filter = "NA";
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
                && GnomADCommand.isGnomADExomeAsRfValid(asRf, isSnv)
                && GnomADCommand.isGnomADExomeABMedianValid(abMedian);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            sj.add(FormatManager.getFloat(af[i]));

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
