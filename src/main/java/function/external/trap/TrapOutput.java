package function.external.trap;

import global.Data;
import java.util.ArrayList;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class TrapOutput {

    String variantId;
    ArrayList<Trap> trapList = new ArrayList<>();

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add("Gene");
        sj.add(TrapManager.getTitle());

        return sj.toString();
    }

    public TrapOutput(String id) throws Exception {
        variantId = id;

        String[] tmp = id.split("-"); // chr-pos-ref-alt

        if (tmp[2].length() == 1 && tmp[3].length() == 1) { // SNV only
            trapList = TrapManager.getTrapList(tmp[0], Integer.parseInt(tmp[1]), tmp[3]);
        }
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        for (Trap trap : trapList) {
            sj.add(variantId);
            sj.add(trap.getGene());
            sj.add(FormatManager.getFloat(trap.getScore()));
        }

        if (sj.length() == 0) {
            sj.add(variantId);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
        }

        return sj.toString();
    }
}
