package function.external.base;

import function.external.bis.BisManager;
import function.external.denovo.DenovoDBManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class DataManager {

    public static String getVersion() {
        StringBuilder sb = new StringBuilder();

        sb.append(EvsManager.getVersion());
        sb.append(ExacManager.getVersion());
        sb.append(GnomADManager.getExomeVersion());
        sb.append(GnomADManager.getGenomeVersion());
        sb.append(KnownVarManager.getVersion());
        sb.append(KaviarManager.getVersion());
        sb.append(GenomesManager.getVersion());
        sb.append(RvisManager.getVersion());
        sb.append(SubRvisManager.getVersion());
        sb.append(BisManager.getVersion());
        sb.append(GerpManager.getVersion());
        sb.append(TrapManager.getVersion());
        sb.append(MgiManager.getVersion());
        sb.append(DenovoDBManager.getVersion());

        return sb.toString();
    }

    public static String getVersion(String table) {
        try {
            String sql = "SELECT version_number "
                    + "From external_table_meta "
                    + "Where data_source ='" + table + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getString("version_number");

            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.STRING_NA;
    }
}
