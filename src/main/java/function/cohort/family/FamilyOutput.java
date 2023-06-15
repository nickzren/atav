package function.cohort.family;

import function.cohort.base.CalledVariant;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author jaimee
 */
public class FamilyOutput extends Output {

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("Family ID");
        sj.add("Model - TBD");
        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getCarrierDataHeader());
        sj.merge(getCohortLevelHeader());
        sj.merge(getExternalDataHeader());

        return sj.toString();
    }
    // The value will be dynamically updated per family
    private String inheritanceModel;

    public FamilyOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample sample) {
        StringJoiner sj = new StringJoiner(",");
        sj.add(sample.getFamilyId());
        sj.add(FormatManager.getString(inheritanceModel));
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, calledVar.getCarrier(sample.getId()), sample);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }

    /*
        dominate model: all cases are HET calls, all controls are HOM REF
        recessive model: all cases are HOM calls, controls are either HET or REF
     */
    public void calculateInheritanceModel(Family family) {
        boolean case_dom = true; // All cases are HET
        boolean case_rec = true; // All cases are HOM
        for (Sample sample : family.getCaseList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());

            if (geno != Index.HET) {
                case_dom = false;
            }

            if (geno != Index.HOM) {
                case_rec = false;
            }
        }

        boolean control_dom = true; // All controls are REF
        boolean control_rec = true; // All controls are either REF or HET
        for (Sample sample : family.getControlList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());

            if (geno != Index.REF) {
                control_dom = false;
            }

            if (geno != Index.REF && geno != Index.HET) {
                control_rec = false;
            }
        }

        inheritanceModel = Data.STRING_NA;
        if (control_dom && case_dom) {
            inheritanceModel = "DOMINANT";
        } else if (control_rec && case_rec) {
            inheritanceModel = "RECESSIVE";
        }
    }

    public String getInheritanceModel() {
        return inheritanceModel;
    }
}
