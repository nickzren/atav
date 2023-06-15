package function.cohort.family;

import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import utils.LogManager;

/**
 *
 * @author jaimee
 */
public class FamilyManager {

    private static ArrayList<Family> familyList = new ArrayList<>();
    private static HashMap<String, Family> familyMap = new HashMap<>();

    public static void initFamily() {
        List<String> filterFamilyList = new ArrayList<>();
        
        if (!FamilyCommand.inputFamilyId.isEmpty()) {
            String[] inputFamilyList = FamilyCommand.inputFamilyId.split(",");
            filterFamilyList = Arrays.asList(inputFamilyList);
        }

        for (Sample sample : SampleManager.getList()) {
            String familyId = sample.getFamilyId();

            if (!sample.getName().equals(familyId)) {
                if (familyMap.containsKey(familyId)) {
                    familyMap.get(familyId).add(sample);
                } else if (filterFamilyList.isEmpty()
                        || filterFamilyList.contains(familyId)) {
                    Family family = new Family();
                    family.add(sample);
                    familyMap.put(familyId, family);
                }
            }
        }

        for (Entry<String, Family> familyEntry : familyMap.entrySet()) {
            Family family = familyEntry.getValue();
            if (family.isValid()) {
                familyList.add(family);
            } else {
                LogManager.writeAndPrint("Invalid Family: " + familyEntry.getKey()
                        + " Controls: " + family.getControlList().size()
                        + " Cases: " + family.getCaseList().size());
            }
        }
    }

    public static boolean initFamily(ArrayList<Sample> sampleList) {
        Family family = new Family();

        for (Sample sample : sampleList) {
            family.add(sample);
        }

        if (family.isValid()) {
            familyList.add(family);
            return true;
        } else {
            LogManager.writeAndPrint("Invalid Family: " + sampleList.get(0).getFamilyId() + " Controls: "
                    + family.getControlList().size() + " Cases: "
                    + family.getCaseList().size());
            return false;
        }
    }

    public static ArrayList<Family> getFamilyList() {
        return familyList;
    }

    // List family can be used with --sample or --family-id
    // If already initialized with --family-id then familyList > 0
    public static boolean isInit() {
        return !familyList.isEmpty();
    }

}
