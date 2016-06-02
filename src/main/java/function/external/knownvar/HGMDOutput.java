package function.external.knownvar;

import java.util.Collection;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class HGMDOutput {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private HGMD hgmd;

    // new columns
    private String m2Site;
    private String m1Site;
    private int siteCount;
    private String p1Site;
    private String p2Site;
    private int indel9bpflanks;

    public HGMDOutput(String chr, int pos, String ref, String alt,
            Collection<HGMD> collection) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        hgmd = getHGMD(collection);

        m2Site = KnownVarManager.getHGMDBySite(chr, pos, -2);
        m1Site = KnownVarManager.getHGMDBySite(chr, pos, -1);
        p1Site = KnownVarManager.getHGMDBySite(chr, pos, 1);
        p2Site = KnownVarManager.getHGMDBySite(chr, pos, 2);
        
        siteCount = collection.size();
        
        indel9bpflanks = KnownVarManager.getHGMDIndelFlankingCount(chr, pos);
    }

    /*
     1. get HGMD by matching chr-pos-ref-alt
     2. or return site accumulated HGMD
     */
    private HGMD getHGMD(Collection<HGMD> collection) {
        HGMD hgmd = new HGMD(chr, pos, ref, alt, "NA", "NA", "NA");

        boolean isFirstSiteHgmd = true;

        for (HGMD tmpHgmd : collection) {
            if (getVariantId().equals(tmpHgmd.getVariantId())) {
                return tmpHgmd;
            } else {
                if (isFirstSiteHgmd) {
                    isFirstSiteHgmd = false;
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
        }

        return hgmd;
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
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
