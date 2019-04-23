package function.external.primateai;

import java.sql.ResultSet;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class PrimateAIOutput {
    
    PrimateAI primateAI;
    
    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add(PrimateAIManager.getTitle());

        return sj.toString();
    }
    
    public PrimateAIOutput(ResultSet rs) {
        primateAI = new PrimateAI(rs);
    }

    public boolean isValid() {
        return primateAI.isValid();
    }

    @Override
    public String toString() {
        return primateAI.toString();
    }
}
