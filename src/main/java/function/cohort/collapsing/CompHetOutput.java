package function.cohort.collapsing;

import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends CollapsingOutput {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.merge(initVarHeaderStr("1"));
        sj.merge(initVarHeaderStr("2"));

        return sj.toString();
    }

    private static StringJoiner initVarHeaderStr(String var) {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getCarrierDataHeader());
        sj.merge(getCohortLevelHeader());
        sj.add("LOO AF");
        sj.merge(getExternalDataHeader());

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
}
