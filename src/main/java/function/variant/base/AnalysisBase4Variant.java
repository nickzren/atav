package function.variant.base;

import function.AnalysisBase;
import function.annotation.base.Annotation;
import function.annotation.base.FunctionManager;
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
    protected int totalNumOfRegionList;
    protected int analyzedRecords;
    protected int nextVariantId;
    protected boolean isIndel;

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    protected void printTotalAnnotationCount(String varType) {
        LogManager.writeLog("Total " + varType.toUpperCase() + "s: " + analyzedRecords + "\n");
        System.out.println("\n");
    }

    protected void clearData() throws SQLException {
        rset.close();
    }

    protected static ResultSet getAnnotationList(String varType,
            Region region) throws SQLException {
        System.out.print("It is collecting " + varType.toUpperCase() + "s..." + "                    \r");

        String sqlCode = getSQL4AnnotationList(varType, region);

        return DBManager.executeReadOnlyQuery(sqlCode);
    }

    private static String getSQL4AnnotationList(String varType, Region region) {
        boolean isIndel = varType.equals("indel");

        String[] functionList;

        if (isIndel) {
            functionList = FunctionManager.indelFunctionList;
        } else {
            functionList = FunctionManager.snvFunctionList;
        }

        String sqlCode = "SELECT v." + varType + "_id, v.seq_region_id, "
                + "v.seq_region_pos, v.cscore_phred, ";

        if (isIndel) {
            sqlCode += "v.length, v.indel_type, ";
        }

        sqlCode += "allele, ref_allele, rs_number, gene_name, effect_type, codon_change, ";

        if (!isIndel) {
            sqlCode += "polyphen_humdiv, polyphen_humvar, ";
        }

        sqlCode += "amino_acid_change, transcript_stable_id, g.hit_type FROM "
                + varType + " AS v ";

        if ((isIndel && !FunctionManager.isINDELAllFunction())
                || (!isIndel && !FunctionManager.isSNVAllFunction())) {
            sqlCode += joinGeneHit(varType);

            if (FunctionManager.isHitTypeContained(functionList)) {
                sqlCode = addHitTypeListToSQL(functionList, sqlCode, isIndel);

                if (FunctionManager.isEffectTypeContained(functionList, isIndel)) {
                    sqlCode += " Or g.hit_type = 'CODING_FUNCTION' OR g.hit_type = 'TRANSCRIBE_FUNCTION'";
                }

                sqlCode += ") LEFT ";
            } else {
                sqlCode += "AND (g.hit_type = 'CODING_FUNCTION' OR g.hit_type = 'TRANSCRIBE_FUNCTION') ";
            }
        } else {
            sqlCode += " LEFT " + joinGeneHit(varType) + " LEFT ";
        }

        sqlCode += joinEffect(varType);

        if ((isIndel && !FunctionManager.isINDELAllFunction())
                || (!isIndel && !FunctionManager.isSNVAllFunction())) {
            sqlCode = addEffectTypeListToSQL(functionList, sqlCode, isIndel);
        }

        sqlCode = RegionManager.addRegionToSQL(region, sqlCode, isIndel);

        sqlCode += " ORDER BY v.seq_region_pos,v." + varType + "_id,"
                + "gene_name,codon_change,"
                + "amino_acid_change,transcript_stable_id";

        return sqlCode;
    }
  
    private static String joinGeneHit(String varType) {
        return " JOIN " + varType + "_gene_hit AS g "
                + "ON g." + varType + "_id = v." + varType + "_id ";
    }

    private static String joinEffect(String varType) {
        return " JOIN " + varType + "_effect AS f "
                + "ON f." + varType + "_id = v." + varType + "_id ";
    }

    private static String addHitTypeListToSQL(String[] functionList, String sqlCode, boolean isIndel) {
        boolean isFirst = true;
        for (String function : functionList) {
            for (int j = 0; j < FunctionManager.HIT_TYPE_FULL_LIST.length; j++) {
                if (function.equals(FunctionManager.HIT_TYPE_FULL_LIST[j])) {
                    if (isFirst) {
                        sqlCode += " AND (";
                        isFirst = false;
                    } else {
                        sqlCode += " OR ";
                    }

                    sqlCode += "g.hit_type='" + FunctionManager.HIT_TYPE_FULL_LIST[j] + "'";
                }
            }
        }

        return sqlCode;
    }

    private static String addEffectTypeListToSQL(String[] functionList, String sqlCode, boolean isIndel) {
        boolean isFirst = false;
        for (String function : functionList) {
            if (isIndel) {
                for (int j = 0; j < FunctionManager.INDEL_EFFECT_FULL_LIST.length; j++) {
                    if (function.equals(FunctionManager.INDEL_EFFECT_FULL_LIST[j])) {
                        if (!isFirst) {
                            sqlCode += " AND (";
                            isFirst = true;
                        } else {
                            sqlCode += " OR ";
                        }

                        sqlCode += "f.effect_type = '" + FunctionManager.INDEL_EFFECT_FULL_LIST[j] + "'";
                    }
                }
            } else {
                for (int j = 0; j < FunctionManager.SNV_EFFECT_FULL_LIST.length; j++) {
                    if (function.equals(FunctionManager.SNV_EFFECT_FULL_LIST[j])) {
                        if (!isFirst) {
                            isFirst = true;
                            sqlCode += " AND (";
                        } else {
                            sqlCode += " OR ";
                        }

                        sqlCode += "f.effect_type = '" + FunctionManager.SNV_EFFECT_FULL_LIST[j] + "'";
                    }
                }
            }
        }

        if (isFirst) {
            sqlCode += ")";
        }

        return sqlCode;
    }
}
