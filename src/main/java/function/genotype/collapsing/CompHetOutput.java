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
        sj.merge(initVarTitleStr("1"));
        sj.merge(initVarTitleStr("2"));

        return sj.toString();
    }

    private static StringJoiner initVarTitleStr(String var) {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(getVariantDataTitle());
        sj.merge(getAnnotationDataTitle());
        sj.merge(getCarrierDataTitle());
        sj.merge(getCohortLevelTitle());
        sj.add("LOO AF");
        sj.merge(getExternalDataTitle());

        String[] list = sj.toString().split(",");

        sj = new StringJoiner(",");
        for (String s : list) {
            sj.add(s + " (#" + var + ")");
        }

        return sj;
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
        sj.add(FormatManager.getDouble(getLooAf()));
        calledVar.getExternalData(sj);

        return sj;
    }

    public boolean isHomOrRef(byte geno) {
        return geno == Index.HOM || geno == Index.REF;
    }
}
