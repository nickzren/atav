package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends CollapsingOutput {

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add(initVarTitleStr("1"));
        sj.add(initVarTitleStr("2"));

        return sj.toString();
    }

    private static String initVarTitleStr(String var) {
        String varTitle = getVariantDataTitle()
                + getAnnotationDataTitle()
                + getCarrierDataTitle()
                + getGenoStatDataTitle()
                + "LOO AF,"
                + getExternalDataTitle();
        
        StringJoiner sj = new StringJoiner(",");

        for (String s : varTitle.split(",")) {
            sj.add(s + " (#" + var + ")");
        }

        return sj.toString();
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public StringJoiner getStringJoiner(Sample sample) {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, calledVar.getCarrier(sample.getId()), sample);
        getGenoStatData(sj);
        sj.add(FormatManager.getDouble(looAF));
        calledVar.getExternalData(sj);

        return sj;
    }

    public boolean isHomOrRef(byte geno) {
        return geno == Index.HOM || geno == Index.REF;
    }
}
