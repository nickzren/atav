package function.cohort.family;

import function.cohort.base.Sample;
import java.util.ArrayList;
import utils.LogManager;

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
        } else{
            LogManager.writeAndPrint("Invalid Family: " + sampleList.get(0).getFamilyId());
            return false;
        }
    }
        
    public static ArrayList<Family> getFamilyList(){
        return familyList;
    }
}