package function.genotype.family;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author nick
 */
public class FamilySummary implements Comparable {

    private String geneName;
    private HashSet<String> snvSet = new HashSet<String>();
    private HashSet<String> indelSet = new HashSet<String>();
    private HashMap<String, HashSet<String>> sharedFamilyVariantMap = new HashMap<String, HashSet<String>>();
    private HashSet<String> totalSharedHetFamilySet = new HashSet<String>();
    private HashSet<String> totalSharedHomFamilySet = new HashSet<String>();

    public static final String title
            = "Gene Name,"
            + "Artifacts in Gene,"
            + "Total Shared Variant Family,"
            + "Total Shared Het Family,"
            + "Total Shared Hom Family,"
            + "Total Shared Cpht Family,"
            + "Total SNV,"
            + "Total Indel,"
            + "Family IDs,"
            + "Variant IDs";

    public FamilySummary(String name) {
        geneName = name;
    }

    public void update(FamilyOutput output) {
        String variantId = output.getCalledVariant().getVariantIdStr();
        String familyId = output.getFamilyId();

        if (output.getCalledVariant().isSnv()) {
            snvSet.add(variantId);
        } else {
            indelSet.add(variantId);
        }

        if (!sharedFamilyVariantMap.containsKey(familyId)) {
            sharedFamilyVariantMap.put(familyId, new HashSet<String>());
        }

        sharedFamilyVariantMap.get(familyId).add(variantId);

        if (output.getHetFamily() > 0) {
            totalSharedHetFamilySet.add(familyId);
        }

        if (output.getHomFamily() > 0) {
            totalSharedHomFamilySet.add(familyId);
        }
    }

    public String getGeneName() {
        return geneName;
    }

    public String getFamilyIdSet() {
        if (sharedFamilyVariantMap.keySet().size() > 0) {
            List list = new ArrayList(sharedFamilyVariantMap.keySet());
            return list.toString().replaceAll(", ", " | ").replace("[", "").replace("]", "");
        }

        return "NA";
    }

    public String getVariantIdSet() {
        if (sharedFamilyVariantMap.values().size() > 0) {
            StringBuilder sb = new StringBuilder();

            boolean isFirst = true;

            for (HashSet<String> set : sharedFamilyVariantMap.values()) {
                List list = new ArrayList(set);
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" | ");
                }

                sb.append(list.toString().replaceAll(", ", " : ").replace("[", "").replace("]", ""));
            }

            return sb.toString();
        }

        return "NA";
    }

    public int getTotalSharedFamilyNum() {
        return sharedFamilyVariantMap.size();
    }

    public int getTotalSharedHetFamilyNum() {
        return totalSharedHetFamilySet.size();
    }

    public int getTotalSharedHomFamilyNum() {
        return totalSharedHomFamilySet.size();
    }

    public int getTotalSharedCphtFamilyNum() {
        int totalSharedCphtFamilyNum = 0;

        for (HashSet<String> set : sharedFamilyVariantMap.values()) {
            if (set.size() >= 2) {
                totalSharedCphtFamilyNum++;
            }
        }

        return totalSharedCphtFamilyNum;
    }

    public int getTotalSnv() {
        return snvSet.size();
    }

    public int getTotalIndel() {
        return indelSet.size();
    }

    public int compareTo(Object another) {
        FamilySummary that = (FamilySummary) another;

        return that.getTotalSharedFamilyNum() - this.getTotalSharedFamilyNum();
    }
}
