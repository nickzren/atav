package function.external.base;

import function.external.denovo.DenovoDBCommand;
import function.external.limbr.LIMBRManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesCommand;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gnomad.GnomADCommand;
import function.external.kaviar.KaviarCommand;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.limbr.LIMBRCommand;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mtr.MTRCommand;
import function.external.mtr.MTRManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapCommand;
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

        if (EvsCommand.isIncludeEvs) {
            sb.append(EvsManager.getVersion());
        }

        if (ExacCommand.isIncludeExac) {
            sb.append(ExacManager.getVersion());
        }

        if (GnomADCommand.isIncludeGnomADExome) {
            sb.append(GnomADManager.getExomeVersion());
        }

        if (GnomADCommand.isIncludeGnomADGenome) {
            sb.append(GnomADManager.getGenomeVersion());
        }

        if (KnownVarCommand.isIncludeKnownVar) {
            sb.append(KnownVarManager.getVersion());
        }

        if (KaviarCommand.isIncludeKaviar) {
            sb.append(KaviarManager.getVersion());
        }

        if (GenomesCommand.isInclude1000Genomes) {
            sb.append(GenomesManager.getVersion());
        }

        if (RvisCommand.isIncludeRvis) {
            sb.append(RvisManager.getVersion());
        }

        if (SubRvisCommand.isIncludeSubRvis) {
            sb.append(SubRvisManager.getVersion());
        }

        if (LIMBRCommand.isIncludeLIMBR) {
            sb.append(LIMBRManager.getVersion());
        }

        if (GerpCommand.isIncludeGerp) {
            sb.append(GerpManager.getVersion());
        }

        if (TrapCommand.isIncludeTrap) {
            sb.append(TrapManager.getVersion());
        }

        if (MgiCommand.isIncludeMgi) {
            sb.append(MgiManager.getVersion());
        }

        if (DenovoDBCommand.isIncludeDenovoDB) {
            sb.append(DenovoDBManager.getVersion());
        }

        if (DiscovEHRCommand.isIncludeDiscovEHR) {
            sb.append(DiscovEHRManager.getVersion());
        }

        if (MTRCommand.isIncludeMTR) {
            sb.append(MTRManager.getVersion());
        }

        if (RevelCommand.isIncludeRevel) {
            sb.append(RevelManager.getVersion());
        }
        
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
