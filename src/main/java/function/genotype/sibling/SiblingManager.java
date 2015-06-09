package function.genotype.sibling;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.ErrorManager;
import utils.LogManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class SiblingManager {
    
    private static ArrayList<Sibling> siblingList = new ArrayList<Sibling>();
    
    public static void init() {
        initList();
    }
    
    private static void initList() {
        HashMap<String, Sibling> siblingMap = new HashMap<String, Sibling>();
        
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()
                    && !sample.getPaternalId().equals("0")
                    && !sample.getMaternalId().equals("0")) {
                if (siblingMap.containsKey(sample.getFamilyId())) {
                    Sibling sibling = siblingMap.get(sample.getFamilyId());
                    sibling.addChild(sample);
                } else {
                    Sibling sibling = new Sibling(sample);
                    siblingMap.put(sample.getFamilyId(), sibling);
                }
            }
        }
        
        for (Sibling sibling : siblingMap.values()) {
            if (sibling.isValid()) {
                siblingList.add(sibling);
            }
        }
        
        if (siblingList.isEmpty()) {
            ErrorManager.print("There is no sibling in your sample file.");
        } else {
            LogManager.writeAndPrint("There are " + siblingList.size()
                    + " siblings available now.");
        }
    }
    
    public static ArrayList<Sibling> getList() {
        return siblingList;
    }
}
