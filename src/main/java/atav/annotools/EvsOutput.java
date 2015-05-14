package atav.annotools;

import atav.manager.data.EvsManager;
import atav.manager.utils.ErrorManager;

/**
 *
 * @author nick
 */
public class EvsOutput {

    String variantId;
    String chr;
    String pos;
    String ref;
    String alt;
    boolean isSnv;
    String evsCoverage;
    String evsMafStr;
    String evsFilterStatus;

    public static final String title
            = "Variant ID,"
            + EvsManager.getTitle();

    public EvsOutput(String id) {
        initBasic(id);

        initEVSInfo();
    }

    private void initBasic(String id) {
        variantId = id;

        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = tmp[1];
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }

    private void initEVSInfo() {
        try {
            evsCoverage = EvsManager.getCoverageInfo(chr, pos);

            evsMafStr = EvsManager.getMafInfo(isSnv, chr, pos, ref, alt);

            if (evsCoverage.equals("0,0,0,0,0,0")) {
                evsMafStr = evsMafStr.replaceAll("NAMAF", "NA");
            } else {
                evsMafStr = evsMafStr.replaceAll("NAMAF", "0");
            }

            evsFilterStatus = EvsManager.getFilterStatus();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(evsCoverage).append(",");
        sb.append(evsMafStr).append(",");
        sb.append(evsFilterStatus);

        return sb.toString();
    }
}
