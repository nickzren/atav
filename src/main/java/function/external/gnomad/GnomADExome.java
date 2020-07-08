package function.external.gnomad;

import global.Data;
import java.sql.PreparedStatement;
import utils.ErrorManager;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
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

    private String filter;
    private byte segdup;
    private byte lcr;
    private byte decoy;
    private float rf_tp_probability;
    private float qd;
    private float pab_max;

    private int global_AN;
    private int global_nhet;
    private int global_nhomalt;
    private int global_nhemi;

    private int controls_AN;
    private int controls_nhet;
    private int controls_nhomalt;
    private int controls_nhemi;

    private int non_neuro_AN;
    private int non_neuro_nhet;
    private int non_neuro_nhomalt;
    private int non_neuro_nhemi;

    private float[] af;
    private float maxAF;

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
            ref = rs.getString("ref");
            alt = rs.getString("alt");

            isSnv = ref.length() == alt.length();

            af = new float[GnomADManager.GNOMAD_EXOME_POP.length];

            setAF(rs);
        } catch (SQLException e) {
            ErrorManager.send(e);
        }
    }

    public GnomADExome(String chr, int pos, String ref_allele, String alt_allele, CSVRecord record) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref_allele;
        this.alt = alt_allele;

        isSnv = ref.length() == alt.length();

        rf_tp_probability = FormatManager.getFloat(record, "gnomAD Exome rf_tp_probability");

        maxAF = Data.FLOAT_NA;
        af = new float[GnomADManager.GNOMAD_EXOME_POP.length];
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            af[i] = FormatManager.getFloat(record, "gnomAD Exome " + GnomADManager.GNOMAD_EXOME_POP[i] + "_AF");
            if (af[i] != Data.FLOAT_NA
                    && GnomADCommand.gnomADExomePopSet.contains(GnomADManager.GNOMAD_EXOME_POP[i])) {
                maxAF = Math.max(maxAF, af[i]);
            }
        }
    }

    private void initAF() {
        af = new float[GnomADManager.GNOMAD_EXOME_POP.length];

        try {
            PreparedStatement preparedStatement = GnomADManager.getPreparedStatement4VariantExome(isMNV);
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            preparedStatement.setString(3, ref);
            preparedStatement.setString(4, alt);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                setAF(rs);
            } else {
                resetAF(Data.FLOAT_NA);
            }
            
            rs.close();
        } catch (SQLException e) {
            ErrorManager.send(e);
        }
    }

    private void setAF(ResultSet rs) throws SQLException {
        filter = rs.getString("filter");
        segdup = FormatManager.getByte(rs, "segdup");
        lcr = FormatManager.getByte(rs, "lcr");
        decoy = FormatManager.getByte(rs, "decoy");
        rf_tp_probability = FormatManager.getFloat(rs, "rf_tp_probability");
        qd = FormatManager.getFloat(rs, "qd");
        pab_max = FormatManager.getFloat(rs, "pab_max");

        global_AN = FormatManager.getInt(rs, "global_AN");
        global_nhet = FormatManager.getInt(rs, "global_nhet");
        global_nhomalt = FormatManager.getInt(rs, "global_nhomalt");
        global_nhemi = FormatManager.getInt(rs, "global_nhemi");

        controls_AN = FormatManager.getInt(rs, "controls_AN");
        controls_nhet = FormatManager.getInt(rs, "controls_nhet");
        controls_nhomalt = FormatManager.getInt(rs, "controls_nhomalt");
        controls_nhemi = FormatManager.getInt(rs, "controls_nhemi");

        non_neuro_AN = FormatManager.getInt(rs, "non_neuro_AN");
        non_neuro_nhet = FormatManager.getInt(rs, "non_neuro_nhet");
        non_neuro_nhomalt = FormatManager.getInt(rs, "non_neuro_nhomalt");
        non_neuro_nhemi = FormatManager.getInt(rs, "non_neuro_nhemi");

        maxAF = Data.FLOAT_NA;
        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            af[i] = FormatManager.getFloat(rs, GnomADManager.GNOMAD_EXOME_POP[i] + "_af");
            if (af[i] != Data.FLOAT_NA
                    && GnomADCommand.gnomADExomePopSet.contains(GnomADManager.GNOMAD_EXOME_POP[i])) {
                maxAF = Math.max(maxAF, af[i]);
            }
        }
    }

    private void resetAF(float value) {
        filter = Data.STRING_NA;
        segdup = Data.BYTE_NA;
        lcr = Data.BYTE_NA;
        decoy = Data.BYTE_NA;
        rf_tp_probability = Data.FLOAT_NA;
        qd = Data.FLOAT_NA;
        pab_max = Data.FLOAT_NA;

        global_AN = Data.INTEGER_NA;
        global_nhet = Data.INTEGER_NA;
        global_nhomalt = Data.INTEGER_NA;
        global_nhemi = Data.INTEGER_NA;

        controls_AN = Data.INTEGER_NA;
        controls_nhet = Data.INTEGER_NA;
        controls_nhomalt = Data.INTEGER_NA;
        controls_nhemi = Data.INTEGER_NA;

        non_neuro_AN = Data.INTEGER_NA;
        non_neuro_nhet = Data.INTEGER_NA;
        non_neuro_nhomalt = Data.INTEGER_NA;
        non_neuro_nhemi = Data.INTEGER_NA;

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            af[i] = value;
        }
    }

    public boolean isValid() {
        return GnomADCommand.isGnomADExomeAFValid(maxAF)
                && GnomADCommand.isGnomADExomeRfTpProbabilityValid(rf_tp_probability, isSnv);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(filter);
        sj.add(FormatManager.getByte(segdup));
        sj.add(FormatManager.getByte(lcr));
        sj.add(FormatManager.getByte(decoy));
        sj.add(FormatManager.getFloat(rf_tp_probability));
        sj.add(FormatManager.getFloat(qd));
        sj.add(FormatManager.getFloat(pab_max));

        for (int i = 0; i < GnomADManager.GNOMAD_EXOME_POP.length; i++) {
            sj.add(FormatManager.getFloat(af[i]));

            switch (i) {
                case 0:
                    sj.add(FormatManager.getInteger(global_AN));
                    sj.add(FormatManager.getInteger(global_nhet));
                    sj.add(FormatManager.getInteger(global_nhomalt));
                    sj.add(FormatManager.getInteger(global_nhemi));
                    break;
                case 1:
                    sj.add(FormatManager.getInteger(controls_AN));
                    sj.add(FormatManager.getInteger(controls_nhet));
                    sj.add(FormatManager.getInteger(controls_nhomalt));
                    sj.add(FormatManager.getInteger(controls_nhemi));
                    break;
                case 2:
                    sj.add(FormatManager.getInteger(non_neuro_AN));
                    sj.add(FormatManager.getInteger(non_neuro_nhet));
                    sj.add(FormatManager.getInteger(non_neuro_nhomalt));
                    sj.add(FormatManager.getInteger(non_neuro_nhemi));
                    break;
                default:
                    break;
            }
        }

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
