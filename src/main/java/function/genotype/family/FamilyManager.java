package function.genotype.family;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nick
 */
public class FamilyManager {

    static ConcurrentHashMap<String, Family> allFamilyMap = new ConcurrentHashMap<>();
    static HashSet<String> userFamilyIdSet = new HashSet<>();
    static HashMap<String, FamilySummary> summaryOnlySharedMap = new HashMap<>();
    static HashMap<String, FamilySummary> summaryAllSharedMap = new HashMap<>();
    static ArrayList<FamilySummary> summaryOnlySharedList = new ArrayList<>();
    static ArrayList<FamilySummary> summaryAllSharedList = new ArrayList<>();

    public static void init() {
        initAllFamilyMap();

        removeInvalidFamilies();

        printFamilyNum();
    }

    private static void initAllFamilyMap() {
        for (Sample sample : SampleManager.getList()) {
            if (sample.isFamily()) {
                addFamilyMember(sample);
            }
        }

    }

    private static void addFamilyMember(Sample sample) {
        Family family;

        if (isFamilyValid(sample.getFamilyId())) {
            family = allFamilyMap.get(sample.getFamilyId());
        } else {
            family = new Family(sample.getFamilyId());
            allFamilyMap.put(sample.getFamilyId(), family);
        }

        family.addMember(sample);
    }

    /*
     * any families that member number is less then 2 then the whole family will
     * be excluded from analysis
     */
    private static void removeInvalidFamilies() {
        Iterator<Family> it = allFamilyMap.values().iterator();

        while (it.hasNext()) {
            Family family = it.next();

            if (!family.isValid()) {
                allFamilyMap.remove(family.getId());
                userFamilyIdSet.remove(family.getId());
            }
        }
    }

    private static void printFamilyNum() {
        if (allFamilyMap.isEmpty()) {
            ErrorManager.print("Missing family in sample file");
        } else {
            LogManager.writeAndPrint("Total families: " + allFamilyMap.size());
        }
    }

    public static void initFamilyIdSet() {
        String str = FamilyCommand.familyId.replaceAll("( )+", "");

        String familyIds = "";

        File f = new File(str);
        if (!f.isFile()) {
            familyIds = str;
        } else {
            try {
                FileInputStream fstream = new FileInputStream(f);
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                while ((str = br.readLine()) != null) {
                    if (str.isEmpty()) {
                        continue;
                    }

                    familyIds += str + ",";
                }

                br.close();
                in.close();
                fstream.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }

        String[] tempArray = familyIds.split(",");

        for (String id : tempArray) {
            userFamilyIdSet.add(id);
        }
    }

    private static boolean isFamilyValid(String familyId) {
        return allFamilyMap.containsKey(familyId);
    }

    public static HashSet<String> getUserFamilyIdSet() {
        return userFamilyIdSet;
    }

    public static void updateFamilySummary(FamilyOutput output) {
        if (output.isShared()) {
            updateFamilySummaryMap(output, summaryOnlySharedMap);
        }

        if (output.isAllShared()) {
            updateFamilySummaryMap(output, summaryAllSharedMap);
        }
    }

    private static void updateFamilySummaryMap(FamilyOutput output,
            HashMap<String, FamilySummary> summaryMap) {
        String geneName = output.getCalledVariant().getGeneName();
        FamilySummary summary;

        if (!summaryMap.containsKey(geneName)) {
            summary = new FamilySummary(geneName);
        } else {
            summary = summaryMap.get(geneName);
        }

        summary.update(output);

        summaryMap.put(geneName, summary);
    }

    public static void initSummaryList() {
        initSummaryList(summaryOnlySharedMap, summaryOnlySharedList);

        initSummaryList(summaryAllSharedMap, summaryAllSharedList);
    }

    private static void initSummaryList(HashMap<String, FamilySummary> map,
            ArrayList<FamilySummary> list) {
        for (FamilySummary summary : map.values()) {
            if (summary.getTotalSharedFamilyNum() >= 2) {
                list.add(summary);
            }
        }

        map.clear();

        Collections.sort(list);
    }

    public static ArrayList<FamilySummary> getSummarySharedList() {
        return summaryOnlySharedList;
    }

    public static ArrayList<FamilySummary> getSummaryAllList() {
        return summaryAllSharedList;
    }
}
