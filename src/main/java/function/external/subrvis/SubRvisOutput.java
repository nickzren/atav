package function.external.subrvis;

import global.Data;
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

    private String domainName = "NA";
    private float domainScore = Data.NA;
    private float domainOEratio = Data.NA;
    private String exonName = "NA";
    private float exonScore = Data.NA;
    private float exonOEratio = Data.NA;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(domainName).append(",");
        sb.append(FormatManager.getFloat(domainScore)).append(",");
        sb.append(FormatManager.getFloat(domainOEratio)).append(",");
        sb.append(exonName).append(",");
        sb.append(FormatManager.getFloat(exonScore)).append(",");
        sb.append(FormatManager.getFloat(exonOEratio)).append(",");

        return sb.toString();
    }
}
