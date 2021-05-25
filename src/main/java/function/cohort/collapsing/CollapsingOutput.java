package function.cohort.collapsing;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Index;
import java.util.List;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getCarrierDataHeader());
        sj.merge(getCohortLevelHeader());
        sj.add("LOO AF");
        sj.merge(getExternalDataHeader());

        return sj.toString();
    }

    List<String> regionBoundaryNameList; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameList = RegionBoundaryManager.getNameList(
                calledVar.getChrStr(),
                calledVar.getStartPosition());
    }
    
    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    @Override
    public boolean isQualifiedGeno(byte geno) {
        if (CollapsingCommand.isRecessive && geno == Index.HET) {
            return false;
        }

        return super.isQualifiedGeno(geno);
    }

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
