package function.annotation.base;

import function.variant.base.Region;
import global.Data;
import function.variant.base.RegionManager;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Annotation {

    public String function;
    public String geneName;
    public String stableId;
    public String codonChange;
    public String aminoAcidChange;
    public float polyphenHumdiv;
    public float polyphenHumdivCCDS;
    public float polyphenHumvar;
    public float polyphenHumvarCCDS;
    public boolean isCCDS;

    public Region region = new Region("1", 0, 0);

    public void init(ResultSet rset, boolean isIndel) throws SQLException {
        function = FunctionManager.getFunction(rset, isIndel);

        geneName = FormatManager.getString(rset.getString("gene_name"));
        codonChange = FormatManager.getString(rset.getString("codon_change"));
        aminoAcidChange = FormatManager.getString(rset.getString("amino_acid_change"));
        stableId = FormatManager.getString(rset.getString("transcript_stable_id"));

        polyphenHumdivCCDS = Data.NA;
        polyphenHumvarCCDS = Data.NA;
        polyphenHumdiv = Data.NA;
        polyphenHumvar = Data.NA;

        if (!isIndel) {
            polyphenHumdiv = MathManager.devide(rset.getInt("polyphen_humdiv"), 1000);
            polyphenHumvar = MathManager.devide(rset.getInt("polyphen_humvar"), 1000);

            isCCDS = TranscriptManager.isCCDSTranscript(stableId);
            if (isCCDS) {
                polyphenHumdivCCDS = polyphenHumdiv;
                polyphenHumvarCCDS = polyphenHumvar;
            }
        }

        int id = rset.getInt("seq_region_id");
        String chrStr = RegionManager.getChrById(id);
        int position = rset.getInt("seq_region_pos");
        int len = 1;

        if (isIndel) {
            len = rset.getInt("length");
            region.setLength(len);
            region.setEndPosition(region.getStartPosition() + len - 1);
        }

        region.initRegion(chrStr, position, position + len - 1);
    }

    public boolean isValid() {
        return PolyphenManager.isValid(polyphenHumdiv, function, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, function, AnnotationLevelFilterCommand.polyphenHumvar)
                && GeneManager.isValid(this)
                && TranscriptManager.isValid(stableId)
                && !function.isEmpty();
    }
}
