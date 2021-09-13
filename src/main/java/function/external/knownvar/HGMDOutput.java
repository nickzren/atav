package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.util.Collection;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class HGMDOutput {

    private Variant var;

    private HGMD hgmd;

    // new columns
    private int siteCount;
    private int variant10bpflanks;

    private boolean isHGMDDM = false;

    public HGMDOutput(Variant var, Collection<HGMD> collection) {
        this.var = var;

        hgmd = getHGMD(collection);

        siteCount = KnownVarManager.getHGMDFlankingCount(var, 0);
        variant10bpflanks = KnownVarManager.getHGMDFlankingCount(var, 10);
    }

    /*
     1. get HGMD by matching chr-pos-ref-alt
     2. or return site accumulated HGMD
     */
    private HGMD getHGMD(Collection<HGMD> collection) {
        HGMD hgmd = new HGMD(
                var.getChrStr(),
                var.getStartPosition(),
                var.getRefAllele(),
                var.getAllele(),
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA,
                Data.BYTE_NA);

        boolean isFirstSite = true;

        for (HGMD tmpHgmd : collection) {
            String idStr = var.getVariantIdStr();

            if (idStr.equals(tmpHgmd.getVariantId())) {
                isHGMDDM = tmpHgmd.isHGMDDM();
                
                return tmpHgmd;
            }

            if (var.isIndel()) { // site values only for SNVs
                continue;
            }

            if (isFirstSite) {
                isFirstSite = false;
                hgmd.setDiseaseName("?Site - " + tmpHgmd.getDiseaseName());
                hgmd.setPmid(tmpHgmd.getPmid());
                hgmd.setVariantClass(tmpHgmd.getVariantClass());
            } else {
                hgmd.append(
                        tmpHgmd.getDiseaseName(),
                        tmpHgmd.getPmid(),
                        tmpHgmd.getVariantClass());
            }
        }

        return hgmd;
    }

    public HGMD getHGMD() {
        return hgmd;
    }

    public boolean isHGMDDM() {
        return isHGMDDM;
    }
    
    public boolean isHGMDDMSite() {
        return siteCount > 0;
    }

    public boolean is10bpFlankingValid() {
        return variant10bpflanks > 0;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(siteCount));
        sj.add(FormatManager.getInteger(variant10bpflanks));
        sj.add(hgmd.getDiseaseName());
        sj.add(hgmd.getPmid());
        sj.add(hgmd.getVariantClass());

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
