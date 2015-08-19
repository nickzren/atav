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

    private ArrayList<Child> childList = new ArrayList<Child>();

    public Family(Sample sample) {
        familyId = sample.getFamilyId();

        String fatherName = sample.getPaternalId();
        int fatherId = SampleManager.getIdByName(fatherName);
        father = SampleManager.getTable().get(fatherId);

        String motherName = sample.getMaternalId();
        int motherId = SampleManager.getIdByName(motherName);
        mother = SampleManager.getTable().get(motherId);

        childList.add(new Child(sample));
    }

    public void addChild(Sample sample) {
        if (sample.getFamilyId().equals(familyId)
                && sample.getPaternalId().equals(father.getName())
                && sample.getMaternalId().equals(mother.getName())) {
            childList.add(new Child(sample));
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

    public ArrayList<Child> getChildList() {
        return childList;
    }
}