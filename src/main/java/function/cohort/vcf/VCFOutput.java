package function.cohort.vcf;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import function.variant.base.Output;
import global.Data;
import global.Index;
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
        infoSJ.add("Effect="+calledVar.getEffect());
        infoSJ.add("Gene="+calledVar.getGeneName());
        infoSJ.add("Hom Case="+FormatManager.getInteger(calledVar.genoCount[Index.HOM][Index.CASE]));
        infoSJ.add("Het Case="+FormatManager.getInteger(calledVar.genoCount[Index.HET][Index.CASE]));
        infoSJ.add("Hom Ref Case="+FormatManager.getInteger(calledVar.genoCount[Index.REF][Index.CASE]));
        infoSJ.add("Hom Ctrl="+FormatManager.getInteger(calledVar.genoCount[Index.HOM][Index.CTRL]));
        infoSJ.add("Het Ctrl="+FormatManager.getInteger(calledVar.genoCount[Index.HET][Index.CTRL]));
        infoSJ.add("Hom Ref Ctrl="+FormatManager.getInteger(calledVar.genoCount[Index.REF][Index.CTRL]));
        infoSJ.add("Case AF="+FormatManager.getFloat(calledVar.af[Index.CASE]));
        infoSJ.add("Ctrl AF="+FormatManager.getFloat(calledVar.af[Index.CTRL]));
        sj.add(infoSJ.toString());
        
        StringJoiner formatSJ = new StringJoiner(":");
        formatSJ.add("GT");
        formatSJ.add("DP Bin");
        formatSJ.add("GQ");
        formatSJ.add("Qual");
        formatSJ.add("FILTER");
        sj.add(formatSJ.toString());
        
        for (Sample sample : SampleManager.getList()) {
            Carrier carrier = calledVar.getCarrier(sample.getId());
            
            formatSJ = new StringJoiner(":");
            formatSJ.add(FormatManager.getInteger(calledVar.getGT(sample.getIndex())));
            formatSJ.add(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA));
            formatSJ.add(FormatManager.getShort(calledVar.getDPBin(sample.getIndex())));
            formatSJ.add(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA));
            formatSJ.add(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA));
            formatSJ.add(carrier != null ? carrier.getFILTER() : Data.STRING_NA);
            sj.add(formatSJ.toString());
        }

        return sj.toString();
    }
}
