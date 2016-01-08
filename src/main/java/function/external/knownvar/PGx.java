package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class PGx {
    String geneName;
    String pgx;

    public PGx(String geneName) {
        this.geneName = geneName;

        initPGx();
    }

    private void initPGx() {
        try {
            String sql = KnownVarManager.getSql4PGx(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                pgx = rs.getString("PGx");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return FormatManager.getString(pgx);
    }
}
