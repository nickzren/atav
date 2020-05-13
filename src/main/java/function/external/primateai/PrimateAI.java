package function.external.primateai;

import global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class PrimateAI {
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private float score;
    
    public PrimateAI(ResultSet rs) {
        try {
            chr = rs.getString("chr");
            pos = rs.getInt("pos");
            ref = rs.getString("ref");
            alt = rs.getString("alt");
            score = rs.getFloat("score");
        } catch (SQLException e) {
            ErrorManager.send(e);
        }
    }
    
    public PrimateAI(CSVRecord record) {        
        score = FormatManager.getFloat(record, "PrimateAI");
    }
    
    public boolean isValid() {
        return PrimateAICommand.isMinPrimateAIValid(score);
    }

    public String getVariantId() {
        return chr + "-" + pos + "-" + ref + "-" + alt;
    }
    
    public float getPrimateDLScore() {
        return score;
    }
    
    public String getPrimateDLScoreStr() {
        return FormatManager.getFloat(score);
    }
    
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(getVariantId());
        sj.add(getPrimateDLScoreStr());

        return sj.toString();
    }
}
