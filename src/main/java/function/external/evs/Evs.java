package function.external.evs;

import global.Data;
import java.sql.ResultSet;
import java.util.StringJoiner;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Evs {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    // from af table
    private float allMaf;
    private String allGenotypeCount;
    private String filterStatus;

    public Evs(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        
        initAF();
    }

    public Evs(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("position");
            ref = rs.getString("ref_allele");
            alt = rs.getString("alt_allele");
            allMaf = rs.getFloat("all_maf");
            allGenotypeCount = rs.getString("all_genotype_count");
            filterStatus = rs.getString("FilterStatus");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initAF() {
        try {
            String sql = EvsManager.getSqlByVariant(chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                allMaf = rs.getFloat("all_maf");
                allGenotypeCount = rs.getString("all_genotype_count");
                filterStatus = rs.getString("FilterStatus");
            } else {
                allGenotypeCount = Data.STRING_NA;
                filterStatus = Data.STRING_NA;
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private float getMaxMaf() {
        float maf = Data.FLOAT_NA;

        if (allMaf != Data.FLOAT_NA) {
            maf = Math.max(allMaf, maf);
        }

        return maf;
    }

    public boolean isValid() {
        return EvsCommand.isEvsStatusValid(filterStatus)
                && EvsCommand.isEvsMafValid(getMaxMaf());
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getFloat(allMaf));
        sj.add(allGenotypeCount);
        sj.add(filterStatus);

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
