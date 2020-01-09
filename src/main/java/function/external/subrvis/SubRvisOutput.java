package function.external.subrvis;

import global.Data;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SubRvisOutput {

    public static String getHeader() {
        return "Variant ID,"
                + "Gene Name,"
                + SubRvisManager.getHeader();
    }

    private String domainName = Data.STRING_NA;
    private float domainPercentile = Data.FLOAT_NA;
    private float mtrDomainPercentile = Data.FLOAT_NA;
    private String exonName = Data.STRING_NA;
    private float exonPercentile = Data.FLOAT_NA;
    private float mtrExonPercentile = Data.FLOAT_NA;

    public SubRvisOutput(String geneName, String chr, int pos) {
        SubRvisGene geneDomain = SubRvisManager.getGeneDomain(geneName, chr, pos);
        if (geneDomain != null) {
            domainName = geneDomain.getId();
            domainPercentile = geneDomain.getPercentile();
            mtrDomainPercentile = geneDomain.getMTRPercentile();
        }

        SubRvisGene geneExon = SubRvisManager.getExonDomain(geneName, chr, pos);
        if (geneExon != null) {
            exonName = geneExon.getId();
            exonPercentile = geneExon.getPercentile();
            mtrExonPercentile = geneExon.getMTRPercentile();
        }
    }

    public SubRvisOutput(CSVRecord record) {
        domainPercentile = FormatManager.getFloat(record.get("subRVIS Domain Percentile"));
        mtrDomainPercentile = FormatManager.getFloat(record.get("MTR Domain Percentile"));
        exonPercentile = FormatManager.getFloat(record.get("subRVIS Exon Percentile"));
        mtrExonPercentile = FormatManager.getFloat(record.get("MTR Exon Percentile"));
    }

    public float getDomainPercentile() {
        return domainPercentile;
    }

    public float getMTRDomainPercentile() {
        return mtrDomainPercentile;
    }

    public float getExonPercentile() {
        return exonPercentile;
    }

    public float getMTRExonPercentile() {
        return mtrExonPercentile;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(domainName);
        sj.add(FormatManager.getFloat(domainPercentile));
        sj.add(FormatManager.getFloat(mtrDomainPercentile));
        sj.add(exonName);
        sj.add(FormatManager.getFloat(exonPercentile));
        sj.add(FormatManager.getFloat(mtrExonPercentile));

        return sj;
    }

    public boolean isValid() {
        return SubRvisCommand.isSubRVISDomainPercentileValid(domainPercentile)
                && SubRvisCommand.isSubRVISExonPercentileValid(exonPercentile)
                && SubRvisCommand.isMTRDomainPercentileValid(mtrDomainPercentile)
                && SubRvisCommand.isMTRExonPercentileValid(mtrExonPercentile);
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
