package function.cohort.sibling;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import function.cohort.base.Sample;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Mother");
        sj.add("Father");
        sj.add("Flag");
        sj.add("Child1");
        sj.add("Child1 Trio Comp Het Flag");
        sj.add("Child2");
        sj.add("Child2 Trio Comp Het Flag");
        sj.merge(initVarHeaderStr("1"));
        sj.merge(initVarHeaderStr("2"));

        return sj.toString();
    }

    private static StringJoiner initVarHeaderStr(String var) {
        StringJoiner sj = new StringJoiner(",");
        
        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getExternalDataHeader());
        sj.merge(getCohortLevelHeader());
        sj.add("Child1 GT");
        sj.add("Child1 DP Bin");
        sj.add("Child2 GT");
        sj.add("Child2 DP Bin");

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

    public StringJoiner getStringJoiner(Sample child1, Sample child2) {
        StringJoiner sj = new StringJoiner(",");

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        calledVar.getExternalData(sj);
        getGenoStatData(sj);

        sj.add(getGenoStr(calledVar.getGT(child1.getIndex())));
        sj.add(FormatManager.getShort(calledVar.getDPBin(child1.getIndex())));
        sj.add(getGenoStr(calledVar.getGT(child2.getIndex())));
        sj.add(FormatManager.getShort(calledVar.getDPBin(child2.getIndex())));

        return sj;
    }
}
