package function.genotype.base;

import global.Data;
import global.Index;
import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class Sample {

    // database info
    private int id;
    // sample info
    private String familyId; // sample or family name
    private String name; // sample name
    private String paternalId;
    private String maternalId;
    private byte sex;
    private byte pheno; // ctrl 0 , case 1
    private float quantitativeTrait;
    private String type;
    private String captureKit;

    // covariate
    private ArrayList<Double> covariateList = new ArrayList<>();
    // sample file order
    private int index;

    public Sample(int sampled_id, String family_id, String child_id,
            String paternal_id, String maternal_id, byte _sex, byte _pheno,
            String sample_type, String _captureKit) {
        id = sampled_id;
        type = sample_type;
        captureKit = _captureKit;

        familyId = family_id;
        name = child_id;
        paternalId = paternal_id;
        maternalId = maternal_id;
        sex = _sex;
        pheno = (byte) (_pheno - 1);
        quantitativeTrait = Data.FLOAT_NA;
    }

    public int getId() {
        return id;
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

    public void setPheno(byte value) {
        pheno = value;
    }

    public byte getPheno() {
        return pheno;
    }

    public void setQuantitativeTrait(float value) {
        quantitativeTrait = value;
    } 

    public float getQuantitativeTrait() {
        return quantitativeTrait;
    }

    public boolean isMale() {
        return sex == 1;
    }

    public boolean isFemale() {
        return !isMale();
    }

    public boolean isCase() {
        return pheno == Index.CASE;
    }

    public boolean isFamily() {
        return !name.equals(familyId);
    }

    public void initCovariate(String[] values) {
        int i = 0;

        for (String value : values) {
            if (i == 0 || i == 1) { // ignore FID and IID columns
                i++;
                continue;
            }

            covariateList.add(Double.valueOf(value));
        }
    }

    public ArrayList<Double> getCovariateList() {
        return covariateList;
    }
}
