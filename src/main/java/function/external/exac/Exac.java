package function.external.exac;

import global.Data;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class Exac {

    private float meanCoverage;
    private int sampleCovered10x;
    private float[] maf;
    private String[] gts;
    private float vqslod;

    public Exac() {
        meanCoverage = Data.NA;
        sampleCovered10x = Data.NA;

        maf = new float[Data.EXAC_POP.length];
        gts = new String[Data.EXAC_POP.length];

        for (int i = 0; i < Data.EXAC_POP.length; i++) {
            maf[i] = Data.NA;
            gts[i] = "NA";
        }

        vqslod = Data.NA;
    }

    public void initCvg(ResultSet rs) {
        try {
            meanCoverage = rs.getFloat("mean_cvg");
            sampleCovered10x = rs.getInt("covered_10x");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void initMaf(ResultSet rs) {
        try {
            for (int i = 0; i < Data.EXAC_POP.length; i++) {
                float af = rs.getFloat(Data.EXAC_POP[i] + "_af");

                if (af > 0.5) {
                    af = 1 - af;
                }

                maf[i] = af;
                gts[i] = rs.getString(Data.EXAC_POP[i] + "_gts");
            }

            vqslod = rs.getFloat("vqslod");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    /**
     * reset maf to 0 when mean coverage > 0 and no exac variant exists at the
     * site
     */
    public void resetMaf() {
        for (int i = 0; i < Data.EXAC_POP.length; i++) {
            maf[i] = 0;
        }
    }

    public float getMaxMaf() {
        float value = Data.NA;

        for (int i = 0; i < Data.EXAC_POP.length; i++) {
            if (maf[i] != Data.NA
                    && CommandValue.exacPop.contains(Data.EXAC_POP[i])) {
                value = Math.max(value, maf[i]);
            }
        }

        return value;
    }

    public float getMeanCoverage() {
        return meanCoverage;
    }

    public float getVqslod() {
        return vqslod;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Data.EXAC_POP.length; i++) {
            sb.append(FormatManager.getDouble(maf[i])).append(",");
            
            if (gts[i].equals("NA")) {
                sb.append(gts[i]).append(",");
            } else {
                sb.append("'").append(gts[i]).append("',");
            }
        }

        sb.append(FormatManager.getDouble(vqslod)).append(",");
        sb.append(FormatManager.getDouble(meanCoverage)).append(",");
        sb.append(FormatManager.getInteger(sampleCovered10x));

        return sb.toString();
    }
}
