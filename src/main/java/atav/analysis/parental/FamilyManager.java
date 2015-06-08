package atav.analysis.parental;

import atav.analysis.base.Sample;
import atav.manager.data.SampleManager;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.LogManager;
import java.util.ArrayList;
import java.util.HashMap;

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
            if (!sample.getPaternalId().equals("0")
                    && !sample.getMaternalId().equals("0")) {
                if (familyMap.containsKey(sample.getFamilyId())) {
                    Family family = familyMap.get(sample.getFamilyId());
                    family.addChild(sample);
                } else {
                    Family family = new Family(sample);
                    familyMap.put(sample.getFamilyId(), family);
                }
            }
        }

        for (Family family : familyMap.values()) {
            familyList.add(family);
        }

        if (familyList.isEmpty()) {
            ErrorManager.print("There is no family in your sample file.");
        } else {
            LogManager.writeAndPrint(familyList.size() + " families are "
                    + "available within your sample file.");
        }
    }

    public static ArrayList<Family> getList() {
        return familyList;
    }
}
