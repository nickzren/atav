package function.annotation.base;

import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
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

    private static int currentVariantID = Data.INTEGER_NA;
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isMNV;
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

    public float revel;
    public float primateAI;
    private int ensembleMissenseValidCount;
    private int ensembleMissenseNACount;

    private boolean isValid;

    public void init(ResultSet rset, String chr) throws SQLException {
        this.chr = chr;
        pos = rset.getInt("POS");
        ref = rset.getString("REF");
        alt = rset.getString("ALT");

        isMNV = ref.length() > 1 && alt.length() > 1
                && alt.length() == ref.length();

        int variantID = rset.getInt("variant_id");
        // only need to init once per variant
        if (currentVariantID != variantID) {
            currentVariantID = variantID;
            initRevel();
            initPrimateAI();
        }

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

        checkValid();
    }

    private void checkValid() {
        isValid = GeneManager.isValid(this, chr, pos)
                && TranscriptManager.isValid(chr, stableId)
                && isPolyphenAndTrapValid(chr, pos, ref, alt,
                        polyphenHumdiv, polyphenHumvar, effect, effectID, geneName)
                && isEnsembleMissenseValid();
    }

    public void setValid(boolean value) {
        isValid = value;
    }

    public boolean isValid() {
        return isValid;
    }

    public static boolean isPolyphenAndTrapValid(String chr, int pos, String ref, String alt,
            float polyphenHumdiv, float polyphenHumvar, String effect, int effectID, String geneName) {
        boolean isPolyphenValid = PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar);
        boolean isValid = isPolyphenValid;

        float trapScore = Data.FLOAT_NA;
        if (TrapCommand.minTrapScore != Data.NO_FILTER
                || TrapCommand.minTrapScoreNonCoding != Data.NO_FILTER) {
            boolean isMNV = ref.length() > 1 && alt.length() > 1 && alt.length() == ref.length();
            trapScore = TrapManager.getScore(chr, pos, alt, isMNV, geneName);
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

    private void initRevel() {
        if (RevelCommand.isInclude) {
            revel = RevelManager.getRevel(chr, pos, ref, alt, isMNV);
        }
    }

    private void initPrimateAI() {
        if (PrimateAICommand.isInclude) {
            primateAI = PrimateAIManager.getPrimateAI(chr, pos, ref, alt, isMNV);
        }
    }

    /*
        1. only applied when --ensemble-missense applied
        2. all three filters required to applied 
     */
    private boolean isEnsembleMissenseValid() {
        if (AnnotationLevelFilterCommand.ensembleMissense) {
            ensembleMissenseValidCount = 0;
            ensembleMissenseNACount = 0;

            if (effect.startsWith("missense_variant")) {
                doEnsembleMissenseFilterCount(
                        PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv),
                        polyphenHumdiv == Data.FLOAT_NA);

                doEnsembleMissenseFilterCount(
                        RevelCommand.isMinRevelValid(revel),
                        revel == Data.FLOAT_NA);

                doEnsembleMissenseFilterCount(
                        PrimateAICommand.isMinPrimateAIValid(primateAI),
                        primateAI == Data.FLOAT_NA);

                return ensembleMissenseValidCount >= 2
                        || (ensembleMissenseValidCount >= 1 && ensembleMissenseNACount >= 2)
                        || ensembleMissenseNACount >= 3;
            }
        }

        return true;
    }

    // valid for all ATAV filters means the value is either pass cutoff or NA
    private void doEnsembleMissenseFilterCount(boolean isValid, boolean isNA) {
        if (isValid) {
            if (isNA) {
                ensembleMissenseNACount++;
            } else {
                ensembleMissenseValidCount++;
            }
        }
    }
}
