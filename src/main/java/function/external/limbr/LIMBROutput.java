package function.external.limbr;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class LIMBROutput {

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + LIMBRManager.getTitle();
    }

    private LIMBRGene geneDomain;
    private LIMBRGene geneExon;

    public LIMBROutput(String geneName, String chr, int pos) {
        geneDomain = LIMBRManager.getGeneDomain(geneName, chr, pos);
        geneExon = LIMBRManager.getExonDomain(geneName, chr, pos);
    }

    public LIMBRGene getGeneDomain() {
        return geneDomain;
    }

    public LIMBRGene getGeneExon() {
        return geneExon;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        if (geneDomain != null) {
            sj.add(geneDomain.getId());
            sj.add(FormatManager.getFloat(geneDomain.getScore()));
            sj.add(FormatManager.getFloat(geneDomain.getPercentiles()));
        } else {
            sj.add("NA,NA,NA");
        }

        if (geneExon != null) {
            sj.add(geneExon.getId());
            sj.add(FormatManager.getFloat(geneExon.getScore()));
            sj.add(FormatManager.getFloat(geneExon.getPercentiles()));
        } else {
            sj.add("NA,NA,NA");
        }

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
