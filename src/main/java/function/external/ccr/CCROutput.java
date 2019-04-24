package function.external.ccr;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CCROutput {

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + CCRManager.getTitle();
    }

    private CCRGene gene;

    public CCROutput(String geneName, String chr, int pos) {
        gene = CCRManager.getGene(geneName, chr, pos);
    }

    public CCRGene getGene() {
        return gene;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        if (gene != null) {
            sj.add(gene.getId());
            sj.add(FormatManager.getFloat(gene.getPercentiles()));
        } else {
            sj.add("NA,NA");
        }

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}