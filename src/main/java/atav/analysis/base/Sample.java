package atav.analysis.base;

import atav.global.Data;
import atav.manager.data.SampleManager;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Sample {

    // database info
    private int id;
    private int prepId;
    // sample info
    private String familyId; // sample or family name
    private String name; // sample name
    private String paternalId;
    private String maternalId;
    private int sex;
    private double pheno;
    private double quantitativeTrait;
    private String type;
    private String captureKit;
    private String finishTime; // in annoDB
    // covariate
    private ArrayList<String> covariateList = new ArrayList<String>();
    // sample file order
    private int index;

    public Sample(int sampled_id, String family_id, String child_id,
            String paternal_id, String maternal_id, int _sex, double _pheno,
            String sample_type, String _captureKit) {
        id = sampled_id;
        type = sample_type;
        captureKit = _captureKit;

        familyId = family_id;
        name = child_id;
        paternalId = paternal_id;
        maternalId = maternal_id;
        sex = _sex;
        pheno = _pheno - 1;

        prepId = SampleManager.getSamplePrepId(id);
        finishTime = SampleManager.getSampleFinishTime(id);

        quantitativeTrait = Data.NA;
    }

    public int getId() {
        return id;
    }

    public int getPrepId() {
        return prepId;
    }

    public void setIndex(int value) {
        index = value;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getCaptureKit() {
        return captureKit;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getName() {
        return name;
    }

    public String getPaternalId() {
        return paternalId;
    }

    public String getMaternalId() {
        return maternalId;
    }

    public int getSex() {
        return sex;
    }

    public void setPheno(int value) {
        pheno = value;
    }

    public double getPheno() {
        return pheno;
    }

    public String getPhenoType() {
        switch ((int) pheno) {
            case 0:
                return "ctrl";
            case 1:
                return "case";
        }

        return "NA";
    }

    public void setQuantitativeTrait(double value) {
        quantitativeTrait = value;
    }

    public double getQuantitativeTrait() {
        return quantitativeTrait;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public boolean isMale() {
        if (sex == 1) {
            return true;
        }

        return false;
    }

    public boolean isFemale() {
        return !isMale();
    }

    public boolean isCase() {
        if (pheno == 1) {
            return true;
        }

        return false;
    }

    public boolean isFamily() {
        if (name.equals(familyId)) {
            return false;
        }

        return true;
    }

    public void initCovariate(String[] values) {
        int i = 0;

        for (String value : values) {
            if (i == 0 || i == 1) { // ignore FID and IID columns
                i++;
                continue;
            }

            covariateList.add(value);
        }
    }

    public ArrayList<String> getCovariateList() {
        return covariateList;
    }
}
