package function.external.kaviar;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class KaviarOutput {

    Kaviar kaviar;

    public static String getHeader() {
        return "Variant ID,"
                + KaviarManager.getHeader();
    }

    public KaviarOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        kaviar = new Kaviar(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public KaviarOutput(boolean isIndel, ResultSet rs) {
        kaviar = new Kaviar(isIndel, rs);
    }

    public boolean isValid() {
        return kaviar.isValid();
    }

    @Override
    public String toString() {
        return kaviar.toString();
    }
}
