package function.cohort.vcf;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
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
        sj.add(calledVar.getRsNumberStr());
        sj.add(calledVar.getRefAllele());
        sj.add(calledVar.getAllele());
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);

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
            // --output-case-only
            if (VCFCommand.isOutputCaseOnly
                    && !sample.isCase()) {
                continue;
            }

            Carrier carrier = calledVar.getCarrier(sample.getId());

            formatSJ = new StringJoiner(":");
            formatSJ.add(calledVar.getGT4VCF(sample.getIndex()));
            // return DP for carrier, return DP Bin for non-carrier 
            formatSJ.add(FormatManager.getShort(carrier != null ? carrier.getDP() : calledVar.getDPBin(sample.getIndex())));
            formatSJ.add(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA));
            sj.add(formatSJ.toString());
        }

        return sj.toString();
    }
}
