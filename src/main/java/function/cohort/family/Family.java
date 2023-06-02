package function.cohort.family;

import function.cohort.base.Sample;
import global.Index;
import java.util.ArrayList;

/**
 *
 * @author jaimee
 */
public class Family {
    private String familyId;
    private ArrayList<Sample> controlList = new ArrayList<Sample>();
    private ArrayList<Sample> caseList = new ArrayList<Sample>();

    public void add(Sample sample) {
        if (sample.getPheno() == Index.CTRL){
            controlList.add(sample);
        } else {
            caseList.add(sample);
        }
    }
    
    public boolean isValid(){
        return controlList.size() >= 1 && caseList.size() >= 2;
    }
    
    public ArrayList<Sample> getControlList(){
        return controlList;
    }
    
    public ArrayList<Sample> getCaseList(){
        return caseList;
    }
}
