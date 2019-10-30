package function.external.mtr;

import java.sql.ResultSet;

/**
 *
 * @author nick
 */
public class MTROutput {

    MTR mtr;

    public static String getHeader() {
        return "Variant ID,"
                + MTRManager.getHeader();
    }

//    public MTROutput(String id) {
//        String[] tmp = id.split("-"); // chr-pos-ref-alt
//        mtr = new MTR(tmp[0], Integer.parseInt(tmp[1]));
//    }

    public MTROutput(ResultSet rs) {
        mtr = new MTR(rs);
    }

    @Override
    public String toString() {
        return mtr.toString();
    }
}
