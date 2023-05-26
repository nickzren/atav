package function.cohort.family;

import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


/**
 *
 * @author jaimee
 */
public class FamilyManager {
    private static HashSet<String> includeFamilyIdSet = new HashSet<>();
    private static HashMap<String, Family> familyMap = new HashMap<>();
    
    public static void init() {
    
        initFromFamilyId(FamilyCommand.inputFamilyId);
        initFamily();
    }
     
    private static void initFromFamilyId(String input) {
        if (input.isEmpty()) {
            return;
        }
        String[] list = input.split(",");
        includeFamilyIdSet.addAll(Arrays.asList(list));
    }
    
    private static void initFamily(){
        for (Sample sample : SampleManager.getList()) {
            
            String familyId = sample.getFamilyId();
            
            if (includeFamilyIdSet.contains(familyId)){
                if (familyMap.containsKey(familyId)){
                    familyMap.get(familyId).addFamilyMember(sample);
                            
                } else{   
                    Family family = new Family(sample);
                    family.addFamilyMember(sample);
                    familyMap.put(familyId, family);
                
                }
            }
        }
    }
    
}
