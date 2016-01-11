package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ACMG {

    String geneName;
    String acmg;

    public ACMG(String geneName) {
        this.geneName = geneName;

        initACMG();
    }

    private void initACMG() {
        try {
            String sql = KnownVarManager.getSql4ACMG(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                acmg = rs.getString("ACMG");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return FormatManager.getString(acmg);
    }
}
