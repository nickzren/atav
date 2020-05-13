package function.variant.base;

import function.AnalysisBase;
import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
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
    protected int nextVariantId;

    protected static ResultSet getAnnotationList(Region region) throws SQLException {
        String sql = "SELECT variant_id, POS, REF, ALT, rs_number, transcript_stable_id, "
                + "effect_id, HGVS_c, HGVS_p, polyphen_humdiv, polyphen_humvar, gene "
                + "FROM variant_chr" + region.getChrStr() + " ";

        // case only filter - add tmp table
        if (CohortLevelFilterCommand.isCaseOnlyValid2CreateTempTable()) {
            sql += ", tmp_case_variant_id_chr" + region.getChrStr() + " ";
        }

        // effect filter - add tmp table
        if (EffectManager.isUsed()) {
            sql += "," + EffectManager.TMP_EFFECT_ID_TABLE + " ";
        }

        // gene filter - add tmp table
        if (GeneManager.isUsed()) {
            sql += "," + GeneManager.TMP_GENE_TABLE + region.getChrStr() + " ";
        }

        // region filter
        // if start == end position , then avoid doing SQL range query
        if (region.getStartPosition() == region.getEndPosition()
                && region.getStartPosition() != Data.INTEGER_NA) {
            sql = addFilter2SQL(sql, " POS = " + region.getStartPosition() + " ");
        } else {
            if (region.getStartPosition() != Data.INTEGER_NA) {
                sql = addFilter2SQL(sql, " POS >= " + region.getStartPosition() + " ");
            }

            if (region.getEndPosition() != Data.INTEGER_NA) {
                sql = addFilter2SQL(sql, " POS <= " + region.getEndPosition() + " ");
            }
        }

        // case only filter - join tmp table
        if (CohortLevelFilterCommand.isCaseOnlyValid2CreateTempTable()) {
            sql = addFilter2SQL(sql, " variant_id = case_variant_id ");
        }

        // effect filter - join tmp table
        if (EffectManager.isUsed()) {
            sql = addFilter2SQL(sql, " effect_id = input_effect_id ");
        }

        // gene filter - join tmp table
        if (GeneManager.isUsed()) {
            sql = addFilter2SQL(sql, " gene = input_gene ");
        }
        
        // QUAL >= 30, MQ >= 40, PASS+LIKELY+INTERMEDIATE, & >= 3 DP or DP Bin >= 3
        if (GenotypeLevelFilterCommand.isHighQualityCallVariantOnly()) {
            sql = addFilter2SQL(sql, " has_high_quality_call = 1 ");
        }

        sql += "ORDER BY POS,variant_id,effect_id,transcript_stable_id;";
        
        return DBManager.executeConcurReadOnlyQuery(sql);
    }

    private static String addFilter2SQL(String sql, String filterSql) {
        if (sql.contains("WHERE")) {
            return sql += "AND" + filterSql;
        } else {
            return sql += "WHERE" + filterSql;
        }
    }
}
