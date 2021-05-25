package function.cohort.vcf;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.variant.base.Output;
import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class VCFOutput extends Output {
    
    public static final String HOM = "1/1";
    public static final String HET = "1/0";
    public static final String REF = "0/0";
    public static final String NA = "./.";

    public static String getHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("##fileformat=VCFv4.2\n");
        sb.append("##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">\n");
        sb.append("##INFO=<ID=AF,Number=1,Type=Float,Description=\"Allele Frequency\">\n");
        sb.append("##INFO=<ID=ANN,Number=.,Type=String,Description=\"Consequence annotations: Effect|Gene|Transcript|HGVS_c|HGVS_p|Polyphen_Humdiv|Polyphen_Humvar\">\n");
        sb.append("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n");
        sb.append("##FORMAT=<ID=GQ,Number=1,Type=Integer,Description=\"Genotype Quality\">\n");
        sb.append("##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Read Depth\">\n");

        StringJoiner sj = new StringJoiner("\t");

        sj.add("#CHROM");
        sj.add("POS");
        sj.add("ID");
        sj.add("REF");
        sj.add("ALT");
        sj.add("QUAL");
        sj.add("FILTER");
        sj.add("INFO");
        sj.add("FORMAT");

        for (Sample sample : SampleManager.getList()) {
            if (CohortLevelFilterCommand.isCaseOnly
                    && !sample.isCase()) {
                continue;
            }
            
            sj.add(sample.getName());
        }

        sb.append(sj.toString());

        return sb.toString();
    }

    public VCFOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\t");

        sj.add(calledVar.getChrStr());
        sj.add(FormatManager.getInteger(calledVar.getStartPosition()));
        sj.add(calledVar.getVariantIdStr());
        sj.add(calledVar.getRefAllele());
        sj.add(calledVar.getAllele());
        sj.add(Data.VCF_NA);
        sj.add(Data.VCF_NA);

        StringJoiner infoSJ = new StringJoiner(";");
        infoSJ.add("NS=" + FormatManager.getInteger(calledVar.getNS()));
        infoSJ.add("AF=" + FormatManager.getFloat(calledVar.getAF()));
        infoSJ.add("ANN=" + calledVar.getAllAnnotation());
        sj.add(infoSJ.toString());

        StringJoiner formatSJ = new StringJoiner(":");
        formatSJ.add("GT");
        formatSJ.add("DP");
        formatSJ.add("GQ");
        sj.add(formatSJ.toString());

        for (Sample sample : SampleManager.getList()) {
            // output case samples only
            if (CohortLevelFilterCommand.isCaseOnly
                    && !sample.isCase()) {
                continue;
            }

            Carrier carrier = calledVar.getCarrier(sample.getId());

            formatSJ = new StringJoiner(":");
            formatSJ.add(VCFManager.getGT(calledVar.getGT(sample.getIndex())));
            // return DP for carrier, return DP Bin for non-carrier 
            formatSJ.add(FormatManager.getShort(carrier != null ? carrier.getDP() : calledVar.getDPBin(sample.getIndex())));
            formatSJ.add(FormatManager.getShort(carrier != null ? carrier.getGQ() : Data.SHORT_NA));
            sj.add(formatSJ.toString());
        }

        return sj.toString();
    }
}
