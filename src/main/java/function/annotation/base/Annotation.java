package function.annotation.base;

import global.Data;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Annotation {

    public String effect;
    public String geneName;
    public int stableId;
    public String HGVS_c;
    public String HGVS_p;
    public float polyphenHumdiv;
    public float polyphenHumvar;
    private String chr;
    private int pos;

    public void init(ResultSet rset, String chr) throws SQLException {
        this.chr = chr;
        pos = rset.getInt("POS");
        stableId = FormatManager.getInt(rset, "transcript_stable_id");

        if (stableId < 0) {
            stableId = Data.NA;
        }

        effect = EffectManager.getEffectById(rset.getInt("effect_id"));
        HGVS_c = FormatManager.getString(rset.getString("HGVS_c"));
        HGVS_p = FormatManager.getString(rset.getString("HGVS_p"));
        polyphenHumdiv = MathManager.devide(FormatManager.getInt(rset, "polyphen_humdiv"), 1000);
        polyphenHumvar = MathManager.devide(FormatManager.getInt(rset, "polyphen_humvar"), 1000);
        geneName = FormatManager.getString(rset.getString("gene"));
    }

    public String getStableId() {
        if (stableId == Data.NA) {
            return "NA";
        }

        StringBuilder idSB = new StringBuilder(String.valueOf(stableId));

        int zeroStringLength = TranscriptManager.TRANSCRIPT_LENGTH - idSB.length() - 4;

        for (int i = 0; i < zeroStringLength; i++) {
            idSB.insert(0, 0);
        }
        
        idSB.insert(0, "ENST");

        return idSB.toString();
    }

    public boolean isValid() {
        return PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar)
                && GeneManager.isValid(this, chr, pos);
    }
}
