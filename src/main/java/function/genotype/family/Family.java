package function.genotype.family;

import function.genotype.base.Sample;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Family {

    private String id;
    private ArrayList<Sample> memberList = new ArrayList<Sample>();

    public Family(String familyId) {
        id = familyId;
    }

    public void addMember(Sample member) {
        memberList.add(member);
    }

    public String getId() {
        return id;
    }

    public ArrayList<Sample> getMemberList() {
        return memberList;
    }

    public boolean isValid() {
        if (memberList.size() < 2) {
            return false;
        }

        return true;
    }
}
