package function.variant.base;

import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nick
 */
public class Variant {

    public int variantId;
    public String variantIdStr;
    public String allele;
    public String refAllele;
    public String rsNumber;
    public Region region;
    public float cscorePhred;
    //Indel attributes
    public String indelType;
    private boolean isIndel;

    public Variant(int v_id, boolean isIndel, ResultSet rset) throws Exception {
        variantId = v_id;

        initBasic(rset);

        if (isIndel) {
            initIndel(rset);
        }

        initVariantIdStr();
        
        this.isIndel = isIndel;
    }

    private void initBasic(ResultSet rset) throws SQLException {
        allele = rset.getString("allele");
        refAllele = rset.getString("ref_allele");
        rsNumber = FormatManager.getString(rset.getString("rs_number"));
        cscorePhred = FormatManager.getFloat(rset.getString("cscore_phred"));

        int position = rset.getInt("seq_region_pos");

        int id = rset.getInt("seq_region_id");

        String chrStr = RegionManager.getChrById(id);

        region = new Region(chrStr, position, position);
    }

    public boolean isAutosome() {
        return region.getChrNum() < 23 || region.getChrNum() == 26;
    }

    private void initIndel(ResultSet rset) throws SQLException {
        int len = rset.getInt("length");
        indelType = rset.getString("indel_type").substring(0, 3).toUpperCase();

        region.setLength(len);
        region.setEndPosition(region.getStartPosition() + len - 1);
    }

    public int getVariantId() {
        return variantId;
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

    public Region getRegion() {
        return region;
    }

    public float getCscore() {
        return cscorePhred;
    }

    public void initVariantIdStr() {
        String chrStr = region.getChrStr();

        if (region.isInsideXPseudoautosomalRegions()) {
            chrStr = "XY";
        }

        variantIdStr = chrStr + "-" + region.getStartPosition()
                + "-" + refAllele + "-" + allele;
    }

    public String getVariantIdStr() {
        return variantIdStr;
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
        return region.getChrStr() + "-" + region.getStartPosition();
    }
}
