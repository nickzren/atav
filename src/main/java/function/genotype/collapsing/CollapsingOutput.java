package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CollapsingOutput extends Output {

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(getVariantDataTitle());
        sj.merge(getAnnotationDataTitle());
        sj.merge(getCarrierDataTitle());
        sj.merge(getGenoStatDataTitle());
        sj.add("LOO AF");
        sj.merge(getExternalDataTitle());

        return sj.toString();
    }

    HashSet<String> regionBoundaryNameSet; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameSet = RegionBoundaryManager.getNameSet(
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
