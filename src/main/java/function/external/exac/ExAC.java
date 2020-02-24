package function.external.exac;

import global.Data;
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
public class ExAC {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;
    private boolean isMNV;

    private float meanCoverage;
    private int sampleCovered10x;
    private float[] af;
    private float maxAF;
    private String[] gts;
    private float vqslod;

    public ExAC(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        isSnv = ref.length() == alt.length();

        isMNV = ref.length() > 1 && alt.length() > 1
                && alt.length() == ref.length();

        initCoverage();

        initAF();
    }

    public ExAC(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");

            isSnv = ref.length() == alt.length();

            initCoverage();

            af = new float[ExACManager.EXAC_POP.length];
            gts = new String[ExACManager.EXAC_POP.length];

            setAF(rs);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public ExAC(String chr, int pos, String ref_allele, String alt_allele, CSVRecord record) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref_allele;
        this.alt = alt_allele;

        isSnv = ref.length() == alt.length();

        maxAF = Data.FLOAT_NA;
        this.af = new float[ExACManager.EXAC_POP.length];
        for (int i = 0; i < ExACManager.EXAC_POP.length; i++) {
            af[i] = FormatManager.getFloat(record.get("ExAC " + ExACManager.EXAC_POP[i] + " af"));
            if (af[i] != Data.FLOAT_NA
                    && ExACCommand.exacPop.contains(ExACManager.EXAC_POP[i])) {
                maxAF = Math.max(maxAF, af[i]);
            }
        }

        vqslod = FormatManager.getFloat(record.get("ExAC vqslod"));
        meanCoverage = FormatManager.getFloat(record.get("ExAC Mean Coverage"));
    }

    private void initCoverage() {
        try {
            String sql = ExACManager.getSql4Cvg(chr, pos);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                meanCoverage = rs.getFloat("mean_cvg");
                sampleCovered10x = rs.getInt("covered_10x");
            } else {
                meanCoverage = Data.FLOAT_NA;
                sampleCovered10x = Data.INTEGER_NA;
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initAF() {
        af = new float[ExACManager.EXAC_POP.length];
        gts = new String[ExACManager.EXAC_POP.length];

        try {
            String sql = ExACManager.getSqlByVariant(chr, pos, ref, alt, isMNV);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                setAF(rs);
            } else if (meanCoverage > 0) {
                resetAF(0);
            } else {
                resetAF(Data.FLOAT_NA);
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void setAF(ResultSet rs) throws SQLException {
        maxAF = Data.FLOAT_NA;
        for (int i = 0; i < ExACManager.EXAC_POP.length; i++) {
            af[i] = rs.getFloat(ExACManager.EXAC_POP[i] + "_af");
            if (af[i] != Data.FLOAT_NA
                    && ExACCommand.exacPop.contains(ExACManager.EXAC_POP[i])) {
                maxAF = Math.max(maxAF, af[i]);
            }
            gts[i] = rs.getString(ExACManager.EXAC_POP[i] + "_gts");
        }

        vqslod = rs.getFloat("vqslod");
    }

    private void resetAF(float value) {
        for (int i = 0; i < ExACManager.EXAC_POP.length; i++) {
            af[i] = value;
            gts[i] = Data.STRING_NA;
        }

        vqslod = Data.FLOAT_NA;
    }

    public boolean isValid() {
        return ExACCommand.isExacAFValid(maxAF)
                && ExACCommand.isExacVqslodValid(vqslod, isSnv)
                && ExACCommand.isExacMeanCoverageValid(meanCoverage);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        for (int i = 0; i < ExACManager.EXAC_POP.length; i++) {
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

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
