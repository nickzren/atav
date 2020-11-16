package function.external.base;

import function.external.ccr.CCRCommand;
import function.external.ccr.CCRManager;
import function.external.denovo.DenovoDBCommand;
import function.external.limbr.LIMBRManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.gnomad.GnomADManager;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gevir.GeVIRCommand;
import function.external.gevir.GeVIRManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.igmaf.IGMAFCommand;
import function.external.igmaf.IGMAFManager;
import function.external.iranome.IranomeCommand;
import function.external.iranome.IranomeManager;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.limbr.LIMBRCommand;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCManager;
import function.external.mtr.MTRCommand;
import function.external.mtr.MTRManager;
import function.external.pext.PextCommand;
import function.external.pext.PextManager;
import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.synrvis.SynRvisCommand;
import function.external.synrvis.SynRvisManager;
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import global.Data;
import java.sql.PreparedStatement;
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

        if (CCRCommand.isInclude) {
            sb.append(CCRManager.getVersion());
        }

        if (EvsCommand.isInclude) {
            sb.append(EvsManager.getVersion());
        }

        if (ExACCommand.getInstance().isInclude) {
            sb.append(ExACManager.getVersion());
        }

        if (DenovoDBCommand.isInclude) {
            sb.append(DenovoDBManager.getVersion());
        }

        if (DiscovEHRCommand.isInclude) {
            sb.append(DiscovEHRManager.getVersion());
        }

        if (GenomeAsiaCommand.getInstance().isInclude) {
            sb.append(GenomeAsiaManager.getVersion());
        }

        if (GerpCommand.isInclude) {
            sb.append(GerpManager.getVersion());
        }

        if (GeVIRCommand.isInclude) {
            sb.append(GeVIRManager.getVersion());
        }

        if (GMECommand.getInstance().isInclude) {
            sb.append(GMEManager.getVersion());
        }

        if (GnomADExomeCommand.getInstance().isInclude) {
            sb.append(GnomADManager.getExomeVersion());
        }

        if (GnomADGenomeCommand.getInstance().isInclude) {
            sb.append(GnomADManager.getGenomeVersion());
        }

        if (GnomADCommand.isIncludeGeneMetrics) {
            sb.append(GnomADManager.getGeneMetricsVersion());
        }

        if (IGMAFCommand.getInstance().isInclude) {
            sb.append(IGMAFManager.getVersion());
        }
        
        if (IranomeCommand.getInstance().isInclude) {
            sb.append(IranomeManager.getVersion());
        }

        if (KnownVarCommand.isInclude) {
            sb.append(KnownVarManager.getVersion());
        }

        if (LIMBRCommand.isInclude) {
            sb.append(LIMBRManager.getVersion());
        }

        if (MgiCommand.isInclude) {
            sb.append(MgiManager.getVersion());
        }

        if (MPCCommand.isInclude) {
            sb.append(MPCManager.getVersion());
        }

        if (MTRCommand.isInclude) {
            sb.append(MTRManager.getVersion());
        }

        if (PextCommand.isInclude) {
            sb.append(PextManager.getVersion());
        }

        if (PrimateAICommand.isInclude) {
            sb.append(PrimateAIManager.getVersion());
        }

        if (RevelCommand.isInclude) {
            sb.append(RevelManager.getVersion());
        }

        if (RvisCommand.isInclude) {
            sb.append(RvisManager.getVersion());
        }

        if (SubRvisCommand.isInclude) {
            sb.append(SubRvisManager.getVersion());
        }

        if (SynRvisCommand.isInclude) {
            sb.append(SynRvisManager.getVersion());
        }

        if (TopMedCommand.getInstance().isInclude) {
            sb.append(TopMedManager.getVersion());
        }

        if (TrapCommand.isInclude) {
            sb.append(TrapManager.getVersion());
        }

        return sb.toString();
    }

    public static String getVersion(String table) {
        try {
            String sql = "SELECT version_number From external_table_meta Where data_source=?";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, table);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return rs.getString("version_number");
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return Data.STRING_NA;
    }
}
