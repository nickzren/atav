package function.external.limbr;

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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (geneDomain != null) {
            sb.append(geneDomain.getId()).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getScore())).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getPercentiles())).append(",");
        } else {
            sb.append("NA,NA,NA,");
        }

        if (geneExon != null) {
            sb.append(geneExon.getId()).append(",");
            sb.append(FormatManager.getFloat(geneExon.getScore())).append(",");
            sb.append(FormatManager.getFloat(geneExon.getPercentiles())).append(",");
        } else {
            sb.append("NA,NA,NA,");
        }

        return sb.toString();
    }
}
