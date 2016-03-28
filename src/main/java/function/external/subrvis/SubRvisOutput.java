package function.external.subrvis;

import function.external.genomes.GenomesManager;
import global.Data;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SubRvisOutput {

    public static final String title
            = "Variant ID,"
            + "Gene Name,"
            + SubRvisManager.getTitle()
            + GenomesManager.getTitle();

    private String domainName = "NA";
    private float domainScore = Data.NA;
    private String exonName = "NA";
    private float exonScore = Data.NA;

    public SubRvisOutput(String geneName, String chr, int pos) {
        SubRvisGene geneDomain = SubRvisManager.getGeneDomain(geneName, chr, pos);
        if (geneDomain != null) {
            domainName = geneDomain.getId();
            domainScore = geneDomain.getScore();
        }

        SubRvisGene geneExon = SubRvisManager.getExonDomain(geneName, chr, pos);
        if (geneExon != null) {
            exonName = geneExon.getId();
            exonScore = geneExon.getScore();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(domainName).append(",");
        sb.append(FormatManager.getFloat(domainScore)).append(",");
        sb.append(exonName).append(",");
        sb.append(FormatManager.getFloat(exonScore)).append(",");

        return sb.toString();
    }
}
