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
    private boolean isIndel;
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
    public float trapScore;
    private int ensembleMissenseValidCount;
    private int ensembleMissenseNACount;

    private boolean isValid;

    public void init(ResultSet rset, String chr) throws SQLException {
        this.chr = chr;
        pos = rset.getInt("POS");
        ref = rset.getString("REF");
        alt = rset.getString("ALT");
        isIndel = ref.length() != alt.length();
        isMNV = ref.length() > 1 && alt.length() > 1
                && alt.length() == ref.length();

        int variantID = rset.getInt("variant_id");
        // only need to init once per variant
        if (currentVariantID != variantID) {
            currentVariantID = variantID;
            initRevel();
            initPrimateAI();
            initTraP();
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
                && PolyphenManager.isValid(polyphenHumdiv, polyphenHumvar, effect)
                && TrapCommand.isValid(trapScore, effect)
                && isEnsembleMissenseValid();
    }

    public void setValid(boolean value) {
        isValid = value;
    }

    public boolean isValid() {
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

    private void initTraP() {
        if (TrapCommand.isInclude) {
            trapScore = isIndel ? Data.FLOAT_NA : TrapManager.getScore(chr, pos, alt, isMNV, geneName);
        }
    }

    /*
        1. only applied when --ensemble-missense applied
        2. it required to use --polyphen-humdiv, --min-revel-score and --min-primate-ai
     */
    private boolean isEnsembleMissenseValid() {
        if (AnnotationLevelFilterCommand.ensembleMissense) {
            ensembleMissenseValidCount = 0;
            ensembleMissenseNACount = 0;

            if (effect.startsWith("missense_variant")) {
                doEnsembleMissenseFilterCount(
                        PolyphenManager.isValid(polyphenHumdiv, polyphenHumvar, effect),
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
