package function.external.subrvis;

import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SubRvisOutput {

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + SubRvisManager.getTitle();
    }

    private String domainName = Data.STRING_NA;
    private float domainScore = Data.FLOAT_NA;
    private float domainOEratio = Data.FLOAT_NA;
    private String exonName = Data.STRING_NA;
    private float exonScore = Data.FLOAT_NA;
    private float exonOEratio = Data.FLOAT_NA;

    public SubRvisOutput(String geneName, String chr, int pos) {
        SubRvisGene geneDomain = SubRvisManager.getGeneDomain(geneName, chr, pos);
        if (geneDomain != null) {
            domainName = geneDomain.getId();
            domainScore = geneDomain.getScore();
            domainOEratio = geneDomain.getOEratio();
        }

        SubRvisGene geneExon = SubRvisManager.getExonDomain(geneName, chr, pos);
        if (geneExon != null) {
            exonName = geneExon.getId();
            exonScore = geneExon.getScore();
            exonOEratio = geneExon.getOEratio();
        }
    }

    public float getDomainScore() {
        return domainScore;
    }

    public float getDomainOEratio() {
        return domainOEratio;
    }

    public float getExonScore() {
        return exonScore;
    }

    public float getExonOEratio() {
        return exonOEratio;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(domainName);
        sj.add(FormatManager.getFloat(domainScore));
        sj.add(FormatManager.getFloat(domainOEratio));
        sj.add(exonName);
        sj.add(FormatManager.getFloat(exonScore));
        sj.add(FormatManager.getFloat(exonOEratio));

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
