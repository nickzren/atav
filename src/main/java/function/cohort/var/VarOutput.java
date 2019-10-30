package function.cohort.var;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class VarOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getExternalDataHeader());
        sj.merge(getCohortLevelHeader());

        return sj.toString();
    }

    public VarOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        calledVar.getExternalData(sj);
        getGenoStatData(sj);

        return sj.toString();
    }
}
