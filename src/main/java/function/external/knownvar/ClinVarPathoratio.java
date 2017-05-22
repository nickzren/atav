package function.external.knownvar;

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
    private int lastPathoLoc;
    
    public ClinVarPathoratio(int indelCount, int copyCount, int snvSpliceCount, 
            int snvNonsenseCount, int snvMissenseCount, int lastPathoLoc){
        this.indelCount = indelCount;
        this.copyCount = copyCount;
        this.snvSpliceCount = snvSpliceCount;
        this.snvNonsenseCount = snvNonsenseCount;
        this.snvMissenseCount = snvMissenseCount;
        this.lastPathoLoc = lastPathoLoc;
    }
    
     @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getInteger(indelCount)).append(",");
        sb.append(FormatManager.getInteger(copyCount)).append(",");
        sb.append(FormatManager.getInteger(snvSpliceCount)).append(",");
        sb.append(FormatManager.getInteger(snvNonsenseCount)).append(",");
        sb.append(FormatManager.getInteger(snvMissenseCount)).append(",");
        sb.append(FormatManager.getInteger(lastPathoLoc)).append(",");

        return sb.toString();
    }
}
