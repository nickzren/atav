package function.genotype.parent;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import global.Index;
import java.util.ArrayList;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class FamilyManager {

    static ArrayList<Family> familyList = new ArrayList<>();

    public static final String[] COMP_HET_FLAG = {
        "COMPOUND HETEROZYGOTE", // 0
        "POSSIBLY COMPOUND HETEROZYGOTE", // 1
        "NO FLAG" //2
    };

    public static void init() {
        initList();
    }

    private static void initList() {
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()
                    && !sample.getPaternalId().equals("0")
                    && !sample.getMaternalId().equals("0")
                    && !sample.getPaternalId().equals(sample.getMaternalId())) {

                Family family = new Family(sample);
                
                if (family.isValid()) {
                    familyList.add(family);
                }
            }
        }

        if (familyList.isEmpty()) {
            ErrorManager.print("Missing family in sample file", ErrorManager.INPUT_PARSING);
        } else {
            LogManager.writeAndPrint("Total families: " + familyList.size());
        }
    }

    public static ArrayList<Family> getList() {
        return familyList;
    }

    public static String getParentCompHetFlag(
            byte childGeno1, byte parent1Geno1, byte parent2Geno1,
            byte childGeno2, byte parent1Geno2, byte parent2Geno2) {
        if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HET && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.HET && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HOM) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HOM
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HET) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HET
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[0];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Data.BYTE_NA
                && childGeno2 == Index.HET && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HET && parent1Geno1 == Index.HET && parent2Geno1 == Data.BYTE_NA
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Data.BYTE_NA
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HOM) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HOM
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Data.BYTE_NA
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HET) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HET
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Index.REF) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HET && parent1Geno2 == Index.HET && parent2Geno2 == Data.BYTE_NA) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HET && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Data.BYTE_NA) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HOM) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HOM
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Data.BYTE_NA) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.REF && parent1Geno1 == Index.HET && parent2Geno1 == Index.REF
                && childGeno2 == Index.HOM && parent1Geno2 == Index.HET && parent2Geno2 == Index.HET) {
            return COMP_HET_FLAG[1];
        } else if (childGeno1 == Index.HOM && parent1Geno1 == Index.HET && parent2Geno1 == Index.HET
                && childGeno2 == Index.REF && parent1Geno2 == Index.HET && parent2Geno2 == Data.BYTE_NA) {
            return COMP_HET_FLAG[1];
        }

        return COMP_HET_FLAG[2];
    }
}
