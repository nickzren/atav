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
    public float cscorePhred;
    //Indel attributes
    public String indelType;
    private boolean isIndel;

    public Variant(int v_id, boolean isIndel, ResultSet rset) throws Exception {
        variantId = v_id;

        allele = rset.getString("allele");
        refAllele = rset.getString("ref_allele");
        rsNumber = FormatManager.getString(rset.getString("rs_number"));
        cscorePhred = FormatManager.getFloat(rset.getString("cscore_phred"));

        if (isIndel) {
            indelType = rset.getString("indel_type").substring(0, 3).toUpperCase();
        }

        this.isIndel = isIndel;

        int position = rset.getInt("seq_region_pos");

        int id = rset.getInt("seq_region_id");

        initRegion(RegionManager.getChrById(id), position, position);

        String chrStr = getChrStr();

        if (isInsideXPseudoautosomalRegions()) {
            chrStr = "XY";
        }

        variantIdStr = chrStr + "-" + position + "-" + refAllele + "-" + allele;
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

    public float getCscore() {
        return cscorePhred;
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
