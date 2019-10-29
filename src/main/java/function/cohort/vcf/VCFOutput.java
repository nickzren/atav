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

    public static String getTitle() {
        StringJoiner sj = new StringJoiner("\t");

        sj.add("#CHROM");
        sj.add("POS");
        sj.add("ID");
        sj.add("REF");
        sj.add("ALT");
        sj.add("INFO");
        sj.add("FORMAT");

        for (Sample sample : SampleManager.getList()) {
            sj.add(sample.getName());
        }

        return sj.toString();
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
            Carrier carrier = calledVar.getCarrier(sample.getId());

            formatSJ = new StringJoiner(":");
            formatSJ.add(FormatManager.getByte(calledVar.getGT(sample.getIndex())));
            // return DP carrier, return DP Bin for non-carrier 
            formatSJ.add(FormatManager.getShort(carrier != null ? carrier.getDP() : calledVar.getDPBin(sample.getIndex())));
            formatSJ.add(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA));
            sj.add(formatSJ.toString());
        }

        return sj.toString();
    }
}
