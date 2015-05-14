package atav.annotools;

import atav.analysis.base.Exac;
import atav.manager.data.ExacManager;

/**
 *
 * @author nick
 */
public class ExacOutput {

    String variantId;
    String chr;
    int pos;
    String ref;
    String alt;
    boolean isSnv;
    Exac exac;

    public static final String title
            = "Variant ID,"
            + ExacManager.getTitle();

    public ExacOutput(String id) {
        initBasic(id);

        exac = ExacManager.getExac(isSnv, chr, pos, ref, alt);
    }

    private void initBasic(String id) {
        variantId = id;

        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }
    
    @Override
    public String toString() {
        return exac.toString();
    }
}
