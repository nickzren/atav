package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Index;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends CollapsingOutput {

    public static String getTitle() {
        return "Family ID,"
                + initVarTitleStr("1") + ","
                + initVarTitleStr("2");
    }

    private static String initVarTitleStr(String var) {
        String varTitle = getVariantDataTitle()
                + getAnnotationDataTitle()
                + getExternalDataTitle()
                + getGenoStatDataTitle()
                + getCarrierDataTitle()
                + "LOO AF,";
        
        String[] list = varTitle.split(",");

        varTitle = "";

        boolean isFirst = true;

        for (String s : list) {
            if (isFirst) {
                varTitle += s + " (#" + var + ")";
                isFirst = false;
            } else {
                varTitle += "," + s + " (#" + var + ")";
            }
        }

        return varTitle;
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        calledVar.getVariantData(sb);
        calledVar.getAnnotationData(sb);
        calledVar.getExternalData(sb);
        getGenoStatData(sb);
        getCarrierData(sb, calledVar.getCarrier(sample.getId()), sample);

        sb.append(FormatManager.getDouble(looAF)).append(",");

        return sb.toString();
    }

    public boolean isHomOrRef(byte geno) {
        return geno == Index.HOM || geno == Index.REF;
    }
}
