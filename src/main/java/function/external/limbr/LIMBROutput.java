package function.external.limbr;

import global.Data;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class LIMBROutput {

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + LIMBRManager.getHeader();
    }

    private String domainName = Data.STRING_NA;
    private float domainScore = Data.FLOAT_NA;
    private float domainPercentile = Data.FLOAT_NA;
    private String exonName = Data.STRING_NA;
    private float exonScore = Data.FLOAT_NA;
    private float exonPercentile = Data.FLOAT_NA;

    public LIMBROutput(String geneName, String chr, int pos) {
        LIMBRGene geneDomain = LIMBRManager.getGeneDomain(geneName, chr, pos);
        if (geneDomain != null) {
            domainName = geneDomain.getId();
            domainScore = geneDomain.getScore();
            domainPercentile = geneDomain.getPercentiles();
        }

        LIMBRGene geneExon = LIMBRManager.getExonDomain(geneName, chr, pos);
        if (geneExon != null) {
            exonName = geneExon.getId();
            exonScore = geneExon.getScore();
            exonPercentile = geneExon.getPercentiles();
        }
    }

    public LIMBROutput(CSVRecord record) {
        domainPercentile = FormatManager.getFloat(record.get("LIMBR Domain Percentile"));
        exonPercentile = FormatManager.getFloat(record.get("LIMBR Exon Percentile"));
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(domainName);
        sj.add(FormatManager.getFloat(domainScore));
        sj.add(FormatManager.getFloat(domainPercentile));
        sj.add(exonName);
        sj.add(FormatManager.getFloat(exonScore));
        sj.add(FormatManager.getFloat(exonPercentile));

        return sj;
    }

    public boolean isValid() {
        return LIMBRCommand.isLIMBRDomainPercentileValid(domainPercentile)
                && LIMBRCommand.isLIMBRExonPercentileValid(exonPercentile);
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
