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

    public Family(Sample child) {
        familyId = child.getFamilyId();

        String fatherName = child.getPaternalId();
        int fatherId = SampleManager.getIdByName(fatherName);
        father = SampleManager.getTable().get(fatherId);

        String motherName = child.getMaternalId();
        int motherId = SampleManager.getIdByName(motherName);
        mother = SampleManager.getTable().get(motherId);

        childList.add(child);
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
}
