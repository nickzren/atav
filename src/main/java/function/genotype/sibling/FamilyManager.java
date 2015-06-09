package function.genotype.sibling;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import function.genotype.sibling.Family;
import java.util.ArrayList;
import java.util.HashMap;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class FamilyManager {

    private static ArrayList<Family> familyList = new ArrayList<Family>();

    public static void init() {
        initList();
    }

    private static void initList() {
        HashMap<String, Family> familyMap = new HashMap<String, Family>();

        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()
                    && !sample.getPaternalId().equals("0")
                    && !sample.getMaternalId().equals("0")) {
                if (familyMap.containsKey(sample.getFamilyId())) {
                    Family sibling = familyMap.get(sample.getFamilyId());
                    sibling.addChild(sample);
                } else {
                    Family sibling = new Family(sample);
                    familyMap.put(sample.getFamilyId(), sibling);
                }
            }
        }

        for (Family family : familyMap.values()) {
            familyList.add(family);
        }

        if (familyList.isEmpty()) {
            ErrorManager.print("There is no sibling in your sample file.");
        } else {
            LogManager.writeAndPrint("There are " + familyList.size()
                    + " siblings available now.");
        }
    }

    public static ArrayList<Family> getList() {
        return familyList;
    }
}
