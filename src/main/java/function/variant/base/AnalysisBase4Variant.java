package function.variant.base;

import function.AnalysisBase;
import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import utils.DBManager;
import utils.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase4Variant extends AnalysisBase {

    protected ResultSet rset;
    protected Region region;
    protected Annotation annotation = new Annotation();
    protected int analyzedRecords;
    protected int nextVariantId;

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant " + analyzedRecords + "                     \r");
    }

    protected static ResultSet getAnnotationList(Region region) throws SQLException {
        String sql = "SELECT variant_id, POS, REF, ALT, rs_number, transcript_stable_id, "
                + "effect_id, HGVS_c, HGVS_p, polyphen_humdiv, polyphen_humvar, gene, indel "
                + "FROM variant_chr" + region.getChrStr() + " "
                + "WHERE POS >= " + region.getStartPosition() + " ";

        // region filter
        if (region.getEndPosition() > 0) {
            sql += "AND POS <= " + region.getEndPosition() + " ";
        }

        // effect filter
        if (EffectManager.isUsed()) {
            sql += "AND effect_id IN " + EffectManager.getEffectIdList4SQL() + " ";
        }

        // gene filter
        if (GeneManager.isUsed()) {
            sql += "AND g.gene in " + GeneManager.getAllGeneByChr(region.getChrStr()) + " ";
        }

        return DBManager.executeReadOnlyQuery(sql);
    }
}
