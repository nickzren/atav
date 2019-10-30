package function.external.discovehr;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class DiscovEHROutput {

    DiscovEHR discovEHR;

    public static String getHeader() {
        return "Variant ID,"
                + DiscovEHRManager.getHeader();
    }

    public DiscovEHROutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        discovEHR = new DiscovEHR(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public DiscovEHROutput(ResultSet rs) {
        discovEHR = new DiscovEHR(rs);
    }

    @Override
    public String toString() {
        return discovEHR.toString();
    }
}
