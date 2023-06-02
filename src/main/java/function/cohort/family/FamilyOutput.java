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
 * @author nick
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
    
    public void calculateInheritanceModel(Family family){
        boolean control_dom = true; // All controls are REF
        boolean control_rec = true; // No controls are HOM
        for (Sample sample: family.getControlList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());
            switch(geno){
                case Index.HET:
                    control_dom = false;
                    break;
                case Index.HOM:
                    control_dom = false;
                    control_rec = false;
                    break;
            }           
        }
                
        boolean case_dom = true;
        boolean case_rec = true;
        for (Sample sample: family.getCaseList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());
            switch (geno) {
                case Index.HET:
                    case_rec = false;
                    break;
                case Index.HOM:
                    case_dom = false;
                    break;
            }
        }
        
        if (control_dom && case_dom){
            inheritanceModel = "DOMINANT";
        } else if (control_rec && case_rec){
            inheritanceModel = "RECESSIVE";
        } else {
            inheritanceModel = Data.STRING_NA;
        }
    }
    
    public String getInheritanceModel(){
        return inheritanceModel;
    }
}
