package function.genotype.trio;

/**
 *
 * @author nick
 */
public class DenovoSummary {

    public String familyId;
    public int denovoVarAutosomesNum;
    public int possiblyDenovoVarAutosomesNum;
    public int newlyRecessiveVarAutosomesNum;
    public int possiblyNewlyRecessiveVarAutosomesNum;
    public int denovoVarXNum;
    public int possiblyDenovoVarXNum;
    public int newlyRecessiveVarXNum;
    public int possiblyNewlyRecessiveVarXNum;
    public int denovoVarYNum;
    public int possiblyDenovoVarYNum;
    public static final String title = "Family ID,"
            + "de novo variants (autosomes),"
            + "possibly de novo variants (autosomes),"
            + "newly recessive variants (autosomes),"
            + "possible newly recessive variants (autosomes),"
            + "de novo variants (X chr),"
            + "possibly de novo variants (X chr),"
            + "newly recessive variants (X chr),"
            + "possible newly recessive variants (X chr),"
            + "de novo variants (Y chr),"
            + "possibly de novo variants (Y chr)\n";

    public DenovoSummary(String id) {
        familyId = id;

        denovoVarAutosomesNum = 0;
        possiblyDenovoVarAutosomesNum = 0;
        newlyRecessiveVarAutosomesNum = 0;
        possiblyNewlyRecessiveVarAutosomesNum = 0;
        denovoVarXNum = 0;
        possiblyDenovoVarXNum = 0;
        newlyRecessiveVarXNum = 0;
        possiblyNewlyRecessiveVarXNum = 0;
        denovoVarYNum = 0;
        possiblyDenovoVarYNum = 0;
    }

    public boolean isQualified() {
        if (denovoVarAutosomesNum > 0
                || possiblyDenovoVarAutosomesNum > 0
                || newlyRecessiveVarAutosomesNum > 0
                || possiblyNewlyRecessiveVarAutosomesNum > 0
                || denovoVarXNum > 0
                || possiblyDenovoVarXNum > 0
                || newlyRecessiveVarXNum > 0
                || possiblyNewlyRecessiveVarXNum > 0
                || denovoVarYNum > 0
                || possiblyDenovoVarYNum > 0) {
            return true;
        }

        return false;
    }
}