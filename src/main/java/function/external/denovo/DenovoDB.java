package function.external.denovo;

import global.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringJoiner;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DenovoDB {

    private String chr;
    private int pos;
    private String ref;
    private String alt;

    private String phenotyp;
    private String pubmedID;

    public DenovoDB(String chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;

        try {
            PreparedStatement preparedStatement = DenovoDBManager.getPreparedStatement4Variant();
            preparedStatement.setString(1, chr);
            preparedStatement.setInt(2, pos);
            preparedStatement.setString(3, ref);
            preparedStatement.setString(4, alt);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                phenotyp = rs.getString("Phenotype");
                pubmedID = rs.getString("PubmedID");
            } else {
                phenotyp = Data.STRING_NA;
                pubmedID = Data.STRING_NA;
            }
            
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public DenovoDB(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref");
            alt = rs.getString("alt");

            phenotyp = rs.getString("Phenotype");
            pubmedID = rs.getString("PubmedID");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.getString(phenotyp));
        sj.add(FormatManager.getString(pubmedID));

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner() .toString();
    }
}
