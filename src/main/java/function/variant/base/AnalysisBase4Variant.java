package function.variant.base;

import function.AnalysisBase;
import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import global.Data;
import utils.DBManager;
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
                + "effect_id, HGVS_c, HGVS_p, polyphen_humdiv, polyphen_humvar, gene, indel_length "
                + "FROM variant_chr" + region.getChrStr() + " ";

        // effect filter - add tmp table
        if (EffectManager.isUsed()) {
            sql += "," + EffectManager.TMP_EFFECT_ID_TABLE + " ";
        }

        // gene filter - add tmp table
        if (GeneManager.isUsed()) {
            sql += "," + GeneManager.TMP_GENE_TABLE + region.getChrStr() + " ";
        }

        // region filter
        if (region.getStartPosition() != Data.INTEGER_NA) {
            sql = addFilter2SQL(sql, " POS >= " + region.getStartPosition() + " ");
        }

        if (region.getEndPosition() != Data.INTEGER_NA) {
            sql = addFilter2SQL(sql, " POS <= " + region.getEndPosition() + " ");
        }

        // effect filter - join tmp table
        if (EffectManager.isUsed()) {
            sql = addFilter2SQL(sql, " effect_id = input_effect_id ");
        }

        // gene filter - join tmp table
        if (GeneManager.isUsed()) {
            sql = addFilter2SQL(sql, " gene = input_gene ");
        }

        // QUAL >= 30, MQ >= 40, PASS+LIKELY+INTERMEDIATE, & >= 3 DP
        if (GenotypeLevelFilterCommand.isHighQualityCallVariantOnly()) {
            sql = addFilter2SQL(sql, " has_high_quality_call = 1 ");
        }

        sql += "ORDER BY POS,variant_id,effect_id,transcript_stable_id;";
        
        return DBManager.executeReadOnlyQuery(sql);
    }

    private static String addFilter2SQL(String sql, String filterSql) {
        if (sql.contains("WHERE")) {
            return sql += "AND" + filterSql;
        } else {
            return sql += "WHERE" + filterSql;
        }
    }
}
