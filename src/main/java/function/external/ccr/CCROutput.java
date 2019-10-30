package function.external.ccr;

import java.util.List;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CCROutput {

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + CCRManager.getHeader();
    }

    private CCRGene gene;

    public CCROutput(List<String> geneList, String chr, int pos) {        
        // go through all qualified genes per variant
        for(String geneName : geneList) {
            gene = CCRManager.getGene(geneName, chr, pos);
            
            if(gene != null) {
                break;
            }
        }
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