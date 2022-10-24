package function.variant.base;

import function.cohort.vcf.VCFCommand;
import global.Data;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.StringJoiner;

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

    public Variant() {
    }

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

    public void initByVariantIDStr(String v_id_str) {
        variantIdStr = v_id_str; 
        String[] values = variantIdStr.split("-");

        String chr = values[0];
        int pos = Integer.valueOf(values[1]);
        refAllele = values[2];
        allele = values[3];

        isIndel = refAllele.length() != allele.length();

        isMNV = refAllele.length() > 1 && allele.length() > 1
                && allele.length() == refAllele.length();

        initRegion(chr, pos, pos);
    }

    public int getVariantId() {
        return variantId;
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
            if (VCFCommand.isOutputVCF) {
                return Data.VCF_NA;
            }

            return Data.STRING_NA;
        }

        return "rs" + rsNumber;
    }

    public boolean isSNV() {
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

    public void getVariantData(StringJoiner sj) {
        sj.add(variantIdStr);
        sj.add(getType());
        sj.add(refAllele);
        sj.add(allele);
        sj.add(getRsNumberStr());
    }

    public String getATAVLINK() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"=HYPERLINK(\"\"http://atavdb.org/variant/");
        sb.append(variantIdStr);
        sb.append("\"\",\"\"ATAV\"\")\"");

        return sb.toString();
    }
}
