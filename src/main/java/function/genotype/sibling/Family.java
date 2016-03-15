package function.genotype.sibling;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Family {

    private String familyId;

    private Sample mother;
    private Sample father;

    private ArrayList<Sample> childList = new ArrayList<Sample>();

    public Family(Sample sample) {
        familyId = sample.getFamilyId();

        String fatherName = sample.getPaternalId();
        int fatherId = SampleManager.getIdByName(fatherName);
        father = SampleManager.getMap().get(fatherId);

        String motherName = sample.getMaternalId();
        int motherId = SampleManager.getIdByName(motherName);
        mother = SampleManager.getMap().get(motherId);

        childList.add(sample);
    }

    public void addChild(Sample sample) {
        if (sample.getFamilyId().equals(familyId)
                && sample.getPaternalId().equals(father.getName())
                && sample.getMaternalId().equals(mother.getName())) {
            childList.add(sample);
        }
    }

    public String getFamilyId() {
        return familyId;
    }

    public Sample getMother() {
        return mother;
    }

    public Sample getFather() {
        return father;
    }

    public ArrayList<Sample> getChildList() {
        return childList;
    }

    public boolean isValid() {
        if (childList.size() > 1) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        int[] array = new int[]{1, 2, 3, 4, 5};

        for (int i = 0; i < array.length - 1; i++) {

            for (int j = i + 1; j < array.length; j++) {
                System.out.println(i + " " + j);
            }
        }
    }
}
