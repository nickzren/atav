package function.external.pext;

import global.Data;
import java.util.ArrayList;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class PextOutput {

    String variantIdStr;
    ArrayList<Pext> pextList = new ArrayList<>();

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add("Gene");
        sj.add(PextManager.getTitle());

        return sj.toString();
    }

    public PextOutput(String id) throws Exception {
        variantIdStr = id;

        String[] tmp = id.split("-"); // chr-pos-ref-alt

        if (tmp[2].length() == 1 && tmp[3].length() == 1) { // SNV only
            pextList = PextManager.getPextList(tmp[0], Integer.parseInt(tmp[1]), tmp[3]);
        }
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        if (pextList.isEmpty()) {
            sj.add(variantIdStr);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
        } else {
            for (Pext pext : pextList) {
                sj.add(variantIdStr);
                sj.add(pext.getGene());
                sj.add(FormatManager.getFloat(pext.getScore()));
            }
        }

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
