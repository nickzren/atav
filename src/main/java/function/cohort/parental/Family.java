package function.cohort.parental;

import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Data;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Family {

    private String familyId;
    private ArrayList<Sample> parentList = new ArrayList<Sample>();
    private ArrayList<Sample> childList = new ArrayList<Sample>();

    public Family(Sample child) {
        familyId = child.getFamilyId();

        addParentByName(child.getPaternalId());

        addParentByName(child.getMaternalId());

        childList.add(child);
    }

    private void addParentByName(String name) {
        int id = SampleManager.getIdByName(name);

        if (id != Data.INTEGER_NA) {
            Sample parent = SampleManager.getMap().get(id);

            parentList.add(parent);
        }
    }

    public void addChild(Sample child) {
        if (child.getFamilyId().equals(familyId)
                && hasParent(child)) {
            childList.add(child);
        }
    }

    private boolean hasParent(Sample child) {
        for (Sample parent : parentList) {
            if (parent.getName().equals(child.getPaternalId())
                    || parent.getName().equals(child.getMaternalId())) {
                return true;
            }
        }

        return false;
    }

    public String getFamilyId() {
        return familyId;
    }
    
    public ArrayList<Sample> getParentList() {
        return parentList;
    }

    public ArrayList<Sample> getChildList() {
        return childList;
    }
}
