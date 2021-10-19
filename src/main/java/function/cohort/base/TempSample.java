package function.cohort.base;

import global.Data;

/**
 *
 * @author nick
 */
public class TempSample {

    int sampleId;
    String sampleName;
    int experimentId;
    String sampleType;
    String captureKit;
    String seqGender;
    String selfDeclGender;
    String familyId;
    String familyRelationProband;
    String ancestry;
    String broadPhenotype;
    String paternalId;
    String maternalId;
    byte pheno;
    byte sex;

    public boolean isGenderMismatch() {
        if(selfDeclGender.equals("Unknown")) {
            return false;
        }
        
        return seqGender.equals("Ambiguous") || seqGender.equals(Data.STRING_NA)
                ||  selfDeclGender.equals(Data.STRING_NA) || !seqGender.equals(selfDeclGender);
    }
}