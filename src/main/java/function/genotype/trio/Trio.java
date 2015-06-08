package function.genotype.trio;

import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;

/**
 *
 * @author nick
 */
public class Trio {

    private String familyId;
    private int childId;
    private int childIndex;
    private String childName;
    private int fatherId;
    private int fatherIndex;
    private String fatherName;
    private int motherId;
    private int motherIndex;
    private String motherName;

    public Trio(Sample sample) {
        familyId = sample.getFamilyId();

        childName = sample.getName();
        childId = sample.getId();
        childIndex = sample.getIndex();

        fatherName = sample.getPaternalId();
        fatherId = SampleManager.getIdByName(fatherName);
        fatherIndex = SampleManager.getIndexById(fatherId);

        motherName = sample.getMaternalId();
        motherId = SampleManager.getIdByName(motherName);
        motherIndex = SampleManager.getIndexById(motherId);
    }

    public boolean isValid() {
        if (fatherId != Data.NA
                && !SampleManager.getTable().get(fatherId).isCase()
                && motherId != Data.NA
                && !SampleManager.getTable().get(motherId).isCase()) {
            return true;
        }

        return false;
    }

    public String getFamilyId() {
        return familyId;
    }

    public int getChildId() {
        return childId;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public String getChildName() {
        return childName;
    }

    public int getFatherId() {
        return fatherId;
    }

    public int getFatherIndex() {
        return fatherIndex;
    }

    public String getFatherName() {
        return fatherName;
    }

    public int getMotherId() {
        return motherId;
    }

    public int getMotherIndex() {
        return motherIndex;
    }

    public String getMotherName() {
        return motherName;
    }
}
