package function.cohort.family;

import function.cohort.base.Sample;
import java.util.ArrayList;

/**
 *
 * @author jaimee
 */
public class Family {
    private String familyId;
    private ArrayList<Sample> controlList = new ArrayList<Sample>();
    private ArrayList<Sample> caseList = new ArrayList<Sample>();
    
    
    public Family(Sample sample) {
        
        familyId = sample.getFamilyId();
        
    }
    
    public void addFamilyMember(Sample sample) {
        if (sample.getBroadPhenotype().equals("Healthy Family Member")){
              controlList.add(sample);
              sample.setPheno((byte) 0);
        } else {
            caseList.add(sample);
            sample.setPheno( (byte) 1);
        }
    }
}
