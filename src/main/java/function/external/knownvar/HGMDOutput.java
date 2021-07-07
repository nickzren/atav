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
    private String m2Site;
    private String m1Site;
    private int siteCount;
    private String p1Site;
    private String p2Site;
    private int snv2bpflanks;
    private int indel9bpflanks;

    public HGMDOutput(Variant var, Collection<HGMD> collection) {
        this.var = var;

        hgmd = getHGMD(collection);

        m2Site = KnownVarManager.getHGMDBySite(var, -2);
        m1Site = KnownVarManager.getHGMDBySite(var, -1);
        p1Site = KnownVarManager.getHGMDBySite(var, 1);
        p2Site = KnownVarManager.getHGMDBySite(var, 2);

        siteCount = var.isSnv() ? collection.size() : Data.INTEGER_NA; // only for SNVs

        snv2bpflanks = var.isSnv() ? KnownVarManager.getHGMDFlankingCount(var, var.isSnv(), 2) : Data.INTEGER_NA; // for SNVs
        indel9bpflanks = var.isIndel() ? KnownVarManager.getHGMDFlankingCount(var, var.isSnv(), 9) : Data.INTEGER_NA; // for indels
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
                Data.STRING_NA);

        boolean isFirstSite = true;

        for (HGMD tmpHgmd : collection) {
            String idStr = var.getVariantIdStr();

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

    public HGMD getHGMD() {
        return hgmd;
    }
    
    public boolean hasIndel9bpFlanks() {
        return indel9bpflanks > 0;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(m2Site);
        sj.add(m1Site);
        sj.add(FormatManager.getInteger(siteCount));
        sj.add(hgmd.getDiseaseName());
        sj.add(hgmd.getPmid());
        sj.add(hgmd.getVariantClass());
        sj.add(p1Site);
        sj.add(p2Site);
        sj.add(FormatManager.getInteger(snv2bpflanks));
        sj.add(FormatManager.getInteger(indel9bpflanks));

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
