package function.cohort.family;

import function.cohort.base.Sample;
import java.util.ArrayList;

/**
 *
 * @author jaimee
 */
public class FamilyManager {
    private static ArrayList<Family> familyList = new ArrayList<>();
    
    public static boolean initFamily(ArrayList<Sample> sampleList){
        Family family = new Family();
        
        for (Sample sample : sampleList){
            family.add(sample);
        }
        
        if (family.isValid()){
            familyList.add(family);
            return true;
        }
        return false;
    }
        
    public static ArrayList<Family> getFamilyList(){
        return familyList;
    }
}