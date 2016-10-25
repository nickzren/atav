package function.variant.base;

import utils.FormatManager;
import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class Variant extends Region {

    public int variantId;
    public String variantIdStr;
    public String allele;
    public String refAllele;
    public String rsNumber;
    //Indel attributes
    private boolean isIndel;

    public Variant(String chr, int v_id, ResultSet rset) throws Exception {
        variantId = v_id;

        int pos = rset.getInt("POS");
        allele = rset.getString("ALT");
        refAllele = rset.getString("REF");
        rsNumber = FormatManager.getString(rset.getString("rs_number"));

        isIndel = rset.getInt("indel") == 1;

        initRegion(chr, pos, pos);

        variantIdStr = chrStr + "-" + pos + "-" + refAllele + "-" + allele;
    }

    public int getVariantId() {
        return variantId;
    }

    /*
     * snv id and indel id could be identical, indel id will now return as negative number 
     */
    public int getVariantIdNegative4Indel() {
        if (isIndel) {
            return -variantId;
        } else {
            return variantId;
        }
    }

    public String getVariantIdStr() {
        return variantIdStr;
    }

    public String getType() {
        if (isIndel) {
            return "indel";
        } else {
            return "snv";
        }
    }

    public String getAllele() {
        return allele;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public String getRsNumber() {
        return rsNumber;
    }

    public boolean isSnv() {
        return !isIndel;
    }

    public boolean isIndel() {
        return isIndel;
    }

    public boolean isDel() {
        return refAllele.length() > allele.length();
    }

    public String getSiteId() {
        return getChrStr() + "-" + getStartPosition();
    }
}
