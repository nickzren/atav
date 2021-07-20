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
        sj.add("Dominant and Haploinsufficient Gene");
        sj.add("Previously Pathogenic Reported Variant");
        sj.add("Hot Zone");
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
        Carrier carrier = calledVar.getCarrier(sample.getId());
        
        StringJoiner sj = new StringJoiner(",");

        byte tierFlag = Data.BYTE_NA;
        if (calledVar.isCaseVarTier1(carrier)) {
            tierFlag = 1;
            Output.tier1SingleVarCount++;
        } else if (calledVar.isMetTier2InclusionCriteria()
                && calledVar.isCaseVarTier2()) {
            tierFlag = 2;
            Output.tier2SingleVarCount++;
        }

        sj.add(FormatManager.getByte(tierFlag));
        sj.add(FormatManager.getByte(calledVar.isDominantAndHaploinsufficient(carrier)));
        sj.add(FormatManager.getByte(calledVar.isPreviouslyPathogenicReported(carrier)));
        sj.add(FormatManager.getByte(calledVar.isHotZone()));
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData_pgl(sj, calledVar.getCarrier(sample.getId()), sample);
//        getGenoStatData(sj);
//        sj.add(FormatManager.getDouble(getLooAf()));
        calledVar.getExternalData(sj);

        return sj.toString();
    }
}
