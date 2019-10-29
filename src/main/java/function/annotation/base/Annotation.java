package function.annotation.base;

import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
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
    private String allele;
    public String effect;
    public int effectID;
    public String geneName;
    public int stableId;
    public String HGVS_c;
    public String HGVS_p;
    public float polyphenHumdiv = Data.FLOAT_NA;
    public float polyphenHumdivCCDS = Data.FLOAT_NA;
    public float polyphenHumvar = Data.FLOAT_NA;
    public float polyphenHumvarCCDS = Data.FLOAT_NA;
    public boolean isCCDS;
    public boolean hasCCDS;

    public void init(ResultSet rset, String chr) throws SQLException {
        this.chr = chr;
        pos = rset.getInt("POS");
        allele = rset.getString("ALT");
        stableId = rset.getInt("transcript_stable_id");

        if (stableId < 0) {
            stableId = Data.INTEGER_NA;
        }

        effectID = rset.getInt("effect_id");
        effect = EffectManager.getEffectById(effectID);
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
        if (GeneManager.isValid(this, chr, pos)
                && TranscriptManager.isValid(chr, stableId)) {
            boolean isPolyphenValid = PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                    && PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar);

            boolean isValid = isPolyphenValid;

            float trapScore = Data.FLOAT_NA;
            if (TrapCommand.minTrapScore != Data.NO_FILTER
                    || TrapCommand.minTrapScoreNonCoding != Data.NO_FILTER) {

                trapScore = TrapManager.getScore(chr, pos, allele, false, geneName);
            }

            // trap filter apply to missense variants when it failed to pass polyphen filter but exclude NA TraP
            // trap filter apply to missense variants when polyphen filter not applied
            // trap filter apply to annotation that effect less damaging than missense_variant and not 5_prime_UTR_premature_start_codon_gain_variant
            if (effect.startsWith("missense_variant")) {
                // when polyphen filter failed, to save variant needs to make sure trap filter used
                if (!isPolyphenValid && TrapCommand.minTrapScore != Data.NO_FILTER) {
                    isValid = trapScore == Data.FLOAT_NA ? false : TrapCommand.isTrapScoreValid(trapScore);
                } else if (AnnotationLevelFilterCommand.polyphenHumdiv.equals(Data.NO_FILTER_STR)
                        && AnnotationLevelFilterCommand.polyphenHumvar.equals(Data.NO_FILTER_STR)) {
                    isValid = TrapCommand.isTrapScoreValid(trapScore);
                }
            } else if (effectID > EffectManager.MISSENSE_VARIANT_ID
                    && !effect.equals("5_prime_UTR_premature_start_codon_gain_variant")) {
                isValid = TrapCommand.isTrapScoreValid(trapScore)
                        && TrapCommand.isTrapScoreNonCodingValid(trapScore);
            }

            return isValid;
        }

        return false;
    }
    
    public String getStableId() {
        if (stableId == Data.INTEGER_NA) {
            return Data.STRING_NA;
        }

        StringBuilder idSB = new StringBuilder(String.valueOf(stableId));

        int zeroStringLength = TranscriptManager.TRANSCRIPT_LENGTH - idSB.length() - 4;

        for (int i = 0; i < zeroStringLength; i++) {
            idSB.insert(0, 0);
        }

        idSB.insert(0, "ENST");

        return idSB.toString();
    }
}
