package function.external.subrvis;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SubRvisOutput {

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + SubRvisManager.getHeader();
    }

    private String domainName = Data.STRING_NA;
    private float domainScorePercentile = Data.FLOAT_NA;
    private float mtrDomainPercentile = Data.FLOAT_NA;
    private String exonName = Data.STRING_NA;
    private float exonScorePercentile = Data.FLOAT_NA;
    private float mtrExonPercentile = Data.FLOAT_NA;

    public SubRvisOutput(String geneName, String chr, int pos) {
        SubRvisGene geneDomain = SubRvisManager.getGeneDomain(geneName, chr, pos);
        if (geneDomain != null) {
            domainName = geneDomain.getId();
            domainScorePercentile = geneDomain.getScorePercentile();
            mtrDomainPercentile = geneDomain.getMTRPercentile();
        }

        SubRvisGene geneExon = SubRvisManager.getExonDomain(geneName, chr, pos);
        if (geneExon != null) {
            exonName = geneExon.getId();
            exonScorePercentile = geneExon.getScorePercentile();
            mtrExonPercentile = geneExon.getMTRPercentile();
        }
    }

    public float getDomainScorePercentile() {
        return domainScorePercentile;
    }

    public float getMTRDomainPercentile() {
        return mtrDomainPercentile;
    }

    public float getExonScorePercentile() {
        return exonScorePercentile;
    }

    public float getMTRExonPercentile() {
        return mtrExonPercentile;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(domainName);
        sj.add(FormatManager.getFloat(domainScorePercentile));
        sj.add(FormatManager.getFloat(mtrDomainPercentile));
        sj.add(exonName);
        sj.add(FormatManager.getFloat(exonScorePercentile));
        sj.add(FormatManager.getFloat(mtrExonPercentile));

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
