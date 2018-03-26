package function.variant.base;

import global.Data;
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
    public int rsNumber;
    //Indel attributes
    private boolean isIndel;
    private boolean isMNV;

    public Variant(String chr, int v_id, ResultSet rset) throws Exception {
        variantId = v_id;

        int pos = rset.getInt("POS");
        allele = rset.getString("ALT");
        refAllele = rset.getString("REF");
        rsNumber = FormatManager.getInt(rset, "rs_number");

        isIndel = refAllele.length() != allele.length();

        isMNV = refAllele.length() > 1 && allele.length() > 1
                && allele.length() == refAllele.length();

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

    public int getRsNumber() {
        return rsNumber;
    }

    public String getRsNumberStr() {
        if (rsNumber == Data.INTEGER_NA) {
            return Data.STRING_NA;
        }

        return "rs" + rsNumber;
    }

    public boolean isSnv() {
        return !isIndel;
    }

    public boolean isIndel() {
        return isIndel;
    }
    
    public boolean isMNV() {
        return isMNV;
    }

    public boolean isDel() {
        return refAllele.length() > allele.length();
    }

    public String getSiteId() {
        return getChrStr() + "-" + getStartPosition();
    }

    public void getVariantData(StringBuilder sb) {
        sb.append(variantIdStr).append(",");
        sb.append(getType()).append(",");
        sb.append(refAllele).append(",");
        sb.append(allele).append(",");
        sb.append(getRsNumberStr()).append(",");
    }
}
