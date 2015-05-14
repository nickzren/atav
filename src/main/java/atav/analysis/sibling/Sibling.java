package atav.analysis.sibling;

import atav.analysis.base.Sample;
import atav.global.Data;
import atav.manager.data.SampleManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Sibling {

    private String familyId;

    private ArrayList<Sample> childList = new ArrayList<Sample>();

    private Sample mother;
    private Sample father;

    public Sibling(Sample sample) {
        familyId = sample.getFamilyId();

        int childId = sample.getId();
        childList.add(SampleManager.getTable().get(childId));

        String fatherName = sample.getPaternalId();
        int fatherId = SampleManager.getIdByName(fatherName);
        father = SampleManager.getTable().get(fatherId);

        String motherName = sample.getMaternalId();
        int motherId = SampleManager.getIdByName(motherName);
        mother = SampleManager.getTable().get(motherId);
    }

    public void addChild(Sample sample) {
        if (sample.getFamilyId().equals(familyId)
                && sample.getPaternalId().equals(father.getName())
                && sample.getMaternalId().equals(mother.getName())) {
            childList.add(sample);
        }
    }

    public boolean isValid() {
        if (father.getId() != Data.NA
                && !father.isCase()
                && mother.getId() != Data.NA
                && !mother.isCase()
                && childList.size() > 1) {
            return true;
        }

        return false;
    }

    public String getFamilyId() {
        return familyId;
    }

    public ArrayList<Sample> getChildList() {
        return childList;
    }

    public Sample getMother() {
        return mother;
    }

    public Sample getFather() {
        return father;
    }
}
