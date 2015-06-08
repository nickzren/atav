package function.variant.base;

import utils.FormatManager;
import utils.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nick
 */
public class Variant {

    public int variantId;
    public String variantIdStr;
    public String newVariantIdStr;
    public String type;
    public String allele;
    public String refAllele;
    public String rsNumber;
    public Region region;
    public String positionStr;
    public float cscorePhred;
    //Indel attributes
    public String indelType;

    public Variant(int v_id, boolean isIndel, ResultSet rset) throws Exception {
        variantId = v_id;

        initBasic(rset);

        if (isIndel) {
            initIndel(rset);
        }

        initStrVariantId();
        
        initNewStrVariantId();
    }

    public Variant(int v_id, boolean isIndel,
            String alt, String ref, String rs,
            int pos, String chr) throws Exception {
        variantId = v_id;

        allele = alt;
        refAllele = ref;
        rsNumber = rs;

        region = new Region(chr, pos, pos);

        type = "snv";

        if (isIndel) {
            type = "indel";
        }
    }

    private void initBasic(ResultSet rset) throws SQLException {
        allele = rset.getString("allele");
        refAllele = rset.getString("ref_allele");
        rsNumber = FormatManager.getString(rset.getString("rs_number"));
        cscorePhred = FormatManager.getFloat(rset.getString("cscore_phred"));

        int position = rset.getInt("seq_region_pos");

        int id = rset.getInt("seq_region_id");

        String chrStr = RegionManager.getChrById(id);

        region = new Region(id, chrStr, position, position);

        type = "snv";
    }

    public boolean isAutosome() {
        return region.getChrNum() < 23 || region.getChrNum() == 26;
    }

    private void initIndel(ResultSet rset) throws SQLException {
        int len = rset.getInt("length");
        indelType = rset.getString("indel_type").substring(0, 3).toUpperCase();

        type = "indel";

        region.setLength(len);
        region.setEndPosition(region.getStartPosition() + len - 1);
    }

    public int getVariantId() {
        return variantId;
    }

    public String getType() {
        return type;
    }

    public String getPositionStr() {
        return positionStr;
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

    private String initStrVariantId() {
        variantIdStr = "";

        String chrStr = region.getChrStr();

        if (region.isInsideXPseudoautosomalRegions()) {
            chrStr = "XY";
        }

        if (isSnv()) {
            positionStr = chrStr + "_" + region.getStartPosition();
            variantIdStr = positionStr + "_" + allele;
        } else if (isIndel()) {
            String varID_allele = "";
            if (indelType.equals("INS")) {
                varID_allele = allele.substring(allele.length() - region.getLength());

                positionStr = chrStr + "_" + region.getStartPosition()
                        + "_" + (region.getStartPosition() + 1);
            } else if (indelType.equals("DEL")) {
                varID_allele = refAllele.substring(refAllele.length() - region.getLength());

                positionStr = chrStr + "_" + region.getStartPosition()
                        + "_" + region.getEndPosition();
            } else {
                LogManager.writeAndPrint("Error indel type: " + indelType);
            }

            variantIdStr = positionStr + "_" + indelType + "_" + varID_allele;
        }

        return variantIdStr;
    }

    public String getVariantIdStr() {
        return variantIdStr;
    }

    public void initNewStrVariantId() {
        String chrStr = region.getChrStr();

        if (region.isInsideXPseudoautosomalRegions()) {
            chrStr = "XY";
        }

        newVariantIdStr = chrStr + "-" + region.getStartPosition()
                + "-" + this.refAllele + "-" + this.allele;
    }

    public String getNewVariantIdStr() {
        return newVariantIdStr;
    }

    public void setVariantIdStr(String id) {
        variantIdStr = id;
    }

    public boolean isSnv() {
        return type.equals("snv");
    }

    public boolean isIndel() {
        return type.equals("indel");
    }
}
