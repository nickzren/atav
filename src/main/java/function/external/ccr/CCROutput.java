package function.external.ccr;

import global.Data;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
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

    private String regionName = Data.STRING_NA;
    private float percentile = Data.FLOAT_NA;

    public CCROutput(List<String> geneList, String chr, int pos) {
        // go through all qualified genes per variant
        for (String geneName : geneList) {
            CCRGene gene = CCRManager.getGene(geneName, chr, pos);

            if (gene != null) {
                regionName = gene.getId();
                percentile = gene.getPercentiles();
                break;
            }
        }
    }
    
    public CCROutput(CSVRecord record) {
        percentile = FormatManager.getFloat(record.get("CCR Percentile"));
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(regionName);
        sj.add(FormatManager.getFloat(percentile));

        return sj;
    }
    
    public boolean isValid() {
        return CCRCommand.isCCRPercentileValid(percentile);
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
