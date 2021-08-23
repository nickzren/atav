package function.external.knownvar;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ClinVarPathoratio {

    private int indelCount;
    private int copyCount;
    private int snvSpliceCount;
    private int snvNonsenseCount;
    private int snvMissenseCount;
    private String lastPathoLoc;

    public ClinVarPathoratio(int indelCount, int copyCount, int snvSpliceCount,
            int snvNonsenseCount, int snvMissenseCount, String lastPathoLoc) {
        this.indelCount = indelCount;
        this.copyCount = copyCount;
        this.snvSpliceCount = snvSpliceCount;
        this.snvNonsenseCount = snvNonsenseCount;
        this.snvMissenseCount = snvMissenseCount;
        this.lastPathoLoc = lastPathoLoc;
    }
    
    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getInteger(indelCount));
        sj.add(FormatManager.getInteger(copyCount));
        sj.add(FormatManager.getInteger(snvSpliceCount));
        sj.add(FormatManager.getInteger(snvNonsenseCount));
        sj.add(FormatManager.getInteger(snvMissenseCount));
        sj.add(FormatManager.getString(lastPathoLoc));

        return sj;
    }
    
    // a gene that has pathogenic/likely pathogenic indel or CNV or spice/nonsense SNV
    public boolean isInClinVarPathoratio() {
        return indelCount > 0
                || copyCount > 0
                || snvSpliceCount > 0
                || snvNonsenseCount > 0;
    }

    public boolean isClinVarPathoratioMissenseSNVValid() {
        return snvMissenseCount > 0;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
