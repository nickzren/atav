package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.util.Collection;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class HGMDOutput {

    private Variant var;

    private HGMD hgmd;

    // new columns
    private String m2Site;
    private String m1Site;
    private int siteCount;
    private String p1Site;
    private String p2Site;
    private int indel9bpflanks;

    public HGMDOutput(Variant var, Collection<HGMD> collection) {
        this.var = var;

        hgmd = getHGMD(collection);

        m2Site = KnownVarManager.getHGMDBySite(var, -2);
        m1Site = KnownVarManager.getHGMDBySite(var, -1);
        p1Site = KnownVarManager.getHGMDBySite(var, 1);
        p2Site = KnownVarManager.getHGMDBySite(var, 2);

        siteCount = var.isSnv() ? collection.size() : Data.NA; // only for SNVs

        indel9bpflanks = var.isIndel() ? KnownVarManager.getHGMDIndelFlankingCount(var) : Data.NA; // only for INDELs
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
                "NA",
                "NA",
                "NA");

        boolean isFirstSite = true;

        for (HGMD tmpHgmd : collection) {
            String idStr = var.getVariantIdStr().replaceAll("XY", "X");

            if (idStr.equals(tmpHgmd.getVariantId())) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(m2Site).append(",");
        sb.append(m1Site).append(",");
        sb.append(FormatManager.getInteger(siteCount)).append(",");
        sb.append(hgmd.getDiseaseName()).append(",");
        sb.append(hgmd.getPmid()).append(",");
        sb.append(hgmd.getVariantClass()).append(",");
        sb.append(p1Site).append(",");
        sb.append(p2Site).append(",");
        sb.append(FormatManager.getInteger(indel9bpflanks)).append(",");

        return sb.toString();
    }
}
