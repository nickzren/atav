package function.external.knownvar;

import function.variant.base.Variant;
import global.Data;
import java.util.Collection;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class DBDSMOutput {

    private Variant var;

    private DBDSM dbDSM;

    public DBDSMOutput(Variant var, Collection<DBDSM> collection) {
        this.var = var;

        dbDSM = getDBDSM(collection);
    }

    /*
     1. get DBDSM by matching chr-pos-ref-alt
     2. or return site accumulated DBDSM
     */
    private DBDSM getDBDSM(Collection<DBDSM> collection) {
        DBDSM dbDSM = new DBDSM(
                var.getChrStr(),
                var.getStartPosition(),
                var.getRefAllele(),
                var.getAllele(),
                Data.STRING_NA,
                Data.STRING_NA,
                Data.STRING_NA);

        boolean isFirstSite = true;

        for (DBDSM tmpDBDSM : collection) {
            String idStr = var.getVariantIdStr();

            if (idStr.equals(tmpDBDSM.getVariantId())) {
                return tmpDBDSM;
            }

            if (var.isIndel()) { // site values only for SNVs
                continue;
            }

            if (isFirstSite) {
                isFirstSite = false;
                dbDSM.setDiseaseName("?Site - " + tmpDBDSM.getDiseaseName());
                dbDSM.setClassification(tmpDBDSM.getClassification());
                dbDSM.setPubmedID(tmpDBDSM.getPubmedID());
            } else {
                dbDSM.append(
                        tmpDBDSM.getDiseaseName(),
                        tmpDBDSM.getClassification(),
                        tmpDBDSM.getPubmedID());
            }
        }

        return dbDSM;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(dbDSM.getDiseaseName());
        sj.add(dbDSM.getClassification());
        sj.add(dbDSM.getPubmedID());

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
