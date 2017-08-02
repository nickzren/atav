package function.external.bis;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class BisOutput {

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + BisManager.getTitle();
    }

    private BisGene geneDomain;
    private BisGene geneExon;

    public BisOutput(String geneName, String chr, int pos) {
        geneDomain = BisManager.getGeneDomain(geneName, chr, pos);
        geneExon = BisManager.getExonDomain(geneName, chr, pos);
    }

    public BisGene getGeneDomain() {
        return geneDomain;
    }

    public BisGene getGeneExon() {
        return geneExon;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (geneDomain != null) {
            sb.append(geneDomain.getId()).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getScore0005())).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getScore0001())).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getScore00005())).append(",");
            sb.append(FormatManager.getFloat(geneDomain.getScore00001())).append(",");
        } else {
            sb.append("NA,NA,NA,NA,NA,");
        }

        if (geneExon != null) {
            sb.append(geneExon.getId()).append(",");
            sb.append(FormatManager.getFloat(geneExon.getScore0005())).append(",");
            sb.append(FormatManager.getFloat(geneExon.getScore0001())).append(",");
            sb.append(FormatManager.getFloat(geneExon.getScore00005())).append(",");
            sb.append(FormatManager.getFloat(geneExon.getScore00001())).append(",");
        } else {
            sb.append("NA,NA,NA,NA,NA,");
        }

        return sb.toString();
    }
}
