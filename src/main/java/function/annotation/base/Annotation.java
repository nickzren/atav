package function.annotation.base;

import function.variant.base.Region;
import global.Data;
import function.variant.base.RegionManager;
import utils.FormatManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public double polyphenHumdiv;
    public double polyphenHumvar;
    public Region region = new Region("1", 0, 0);

    public void init(ResultSet rset, boolean isIndel) throws SQLException {
        function = FunctionManager.getFunction(rset, isIndel);

        if (isIndel) {
            polyphenHumdiv = Data.NA;
            polyphenHumvar = Data.NA;
        } else {
            polyphenHumdiv = FormatManager.devide(rset.getInt("polyphen_humdiv"), 1000);
            polyphenHumvar = FormatManager.devide(rset.getInt("polyphen_humvar"), 1000);
        }

        geneName = FormatManager.getString(rset.getString("gene_name"));
        codonChange = FormatManager.getString(rset.getString("codon_change"));
        aminoAcidChange = FormatManager.getString(rset.getString("amino_acid_change"));
        stableId = FormatManager.getString(rset.getString("transcript_stable_id"));

        int id = rset.getInt("seq_region_id");
        String chrStr = RegionManager.getChrById(id);
        int position = rset.getInt("seq_region_pos");
        int len = 1;

        if (isIndel) {
            len = rset.getInt("length");
            region.setLength(len);
            region.setEndPosition(region.getStartPosition() + len - 1);
        }

        region.init(chrStr, position, position + len - 1);
    }

    public boolean isValid() {
        if (PolyphenManager.isValid(polyphenHumdiv, function, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, function, AnnotationLevelFilterCommand.polyphenHumvar)
                && GeneManager.isValid(this)
                && TranscriptManager.isValid(stableId)
                && !function.isEmpty()) {
            return true;
        }

        return false;
    }
}
