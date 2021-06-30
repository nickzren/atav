package function.cohort.vargeno;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Tier Flag");
        sj.add("Dominant and ClinGen Haploinsufficient");
        sj.add("Previously Pathogenic Reported");
        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getCarrierDataHeader_pgl());
//        sj.merge(getCohortLevelHeader());
//        sj.add("LOO AF");
        sj.merge(getExternalDataHeader());

        return sj.toString();
    }

    public VarGenoOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample sample) {
        StringJoiner sj = new StringJoiner(",");

        Carrier carrier = calledVar.getCarrier(sample.getId());
        byte tierFlag = this.calledVar.isMetTier2InclusionCriteria() ? 2 : Data.BYTE_NA;

        sj.add(FormatManager.getByte(tierFlag));
        sj.add(FormatManager.getByte(isDominantAndClinGenHaploinsufficient(carrier)));
        sj.add(FormatManager.getByte(isPreviouslyPathogenicReported(carrier)));

        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData_pgl(sj, calledVar.getCarrier(sample.getId()), sample);
//        getGenoStatData(sj);
//        sj.add(FormatManager.getDouble(getLooAf()));
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
