package function.cohort.trio;

import function.cohort.base.Sample;
import global.Data;
import function.cohort.base.SampleManager;

/**
 *
 * @author nick
 */
public class Trio {

    private Sample child;

    private int fatherId;
    private int fatherIndex;
    private String fatherName;
    private int motherId;
    private int motherIndex;
    private String motherName;

    public Trio(Sample sample) {
        child = sample;

        fatherName = child.getPaternalId();
        fatherId = SampleManager.getIdByName(fatherName);
        fatherIndex = SampleManager.getIndexById(fatherId);

        motherName = child.getMaternalId();
        motherId = SampleManager.getIdByName(motherName);
        motherIndex = SampleManager.getIndexById(motherId);
    }

    public boolean isValid() {
        if (TrioCommand.isExcludeDUO && this.isDUO()) {
            return false;
        }
        return (fatherId != Data.INTEGER_NA || motherId != Data.INTEGER_NA);
    }

    public Sample getChild() {
        return child;
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

    public boolean isDUO() {
        return fatherId == Data.INTEGER_NA
                || motherId == Data.INTEGER_NA;
    }
}
