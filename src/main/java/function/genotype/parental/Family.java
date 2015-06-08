package function.genotype.parental;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Family {

    private String familyId;

    private Sample father;
    private Sample mother;

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

    public Sample getFather() {
        return father;
    }

    public Sample getMother() {
        return mother;
    }

    public ArrayList<Sample> getChildList() {
        return childList;
    }
}
