package function.external.knownvar;

import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class AdultOnset {
    String geneName;
    String adultOnset;

    public AdultOnset(String geneName) {
        this.geneName = geneName;

        initAdultOnset();
    }

    private void initAdultOnset() {
        try {
            String sql = KnownVarManager.getSql4AdultOnset(geneName);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                adultOnset = rs.getString("AdultOnset");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return FormatManager.getString(adultOnset);
    }
}
