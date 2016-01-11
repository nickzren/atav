package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RecessiveCarrier {
    String geneName;
    int recessiveCarrier;

    public RecessiveCarrier(String geneName) {
        this.geneName = geneName;

        initRecessiveCarrier();
    }

    private void initRecessiveCarrier() {
        try {
            String sql = KnownVarManager.getSql4RecessiveCarrier(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                recessiveCarrier = 1;
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(recessiveCarrier);
    }
}
