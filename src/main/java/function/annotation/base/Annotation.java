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

    private String chr;
    private int pos;
    public String effect;
    public String geneName;
    public int stableId;
    public String HGVS_c;
    public String HGVS_p;
    public float polyphenHumdiv;
    public float polyphenHumdivCCDS;
    public float polyphenHumvar;
    public float polyphenHumvarCCDS;
    public boolean isCCDS;

    public void init(ResultSet rset, String chr) throws SQLException {
        this.chr = chr;
        pos = rset.getInt("POS");
        stableId = rset.getInt("transcript_stable_id");

        if (stableId < 0) {
            stableId = Data.INTEGER_NA;
        }

        effect = EffectManager.getEffectById(rset.getInt("effect_id"));
        HGVS_c = FormatManager.getString(rset.getString("HGVS_c"));
        HGVS_p = FormatManager.getString(rset.getString("HGVS_p"));
        geneName = FormatManager.getString(rset.getString("gene"));

        polyphenHumdiv = MathManager.devide(FormatManager.getInt(rset, "polyphen_humdiv"), 1000);
        polyphenHumvar = MathManager.devide(FormatManager.getInt(rset, "polyphen_humvar"), 1000);

        isCCDS = TranscriptManager.isCCDSTranscript(chr, stableId);

        polyphenHumdivCCDS = isCCDS ? polyphenHumdiv : Data.FLOAT_NA;
        polyphenHumvarCCDS = isCCDS ? polyphenHumvar : Data.FLOAT_NA; 
    }

    public boolean isValid() {
        return PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar)
                && GeneManager.isValid(this, chr, pos)
                && TranscriptManager.isValid(chr, stableId);
    }
}
