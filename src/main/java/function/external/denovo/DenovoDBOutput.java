package function.external.denovo;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class DenovoDBOutput {

    DenovoDB denovoDB;

    public static String getHeader() {
        return "Variant ID,"
                + DenovoDBManager.getHeader();
    }

    public DenovoDBOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        denovoDB = new DenovoDB(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public DenovoDBOutput(ResultSet rs) {
        denovoDB = new DenovoDB(rs);
    }
    
    @Override
    public String toString() {
        return denovoDB.toString();
    }
}