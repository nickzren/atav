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

    String variantIdStr;
    ArrayList<Trap> trapList = new ArrayList<>();

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add("Gene");
        sj.add(TrapManager.getTitle());

        return sj.toString();
    }

    public TrapOutput(String id) throws Exception {
        variantIdStr = id;

        String[] tmp = id.split("-"); // chr-pos-ref-alt

        if (tmp[2].length() == 1 && tmp[3].length() == 1) { // SNV only
            trapList = TrapManager.getTrapList(tmp[0], Integer.parseInt(tmp[1]), tmp[3]);
        }
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        if (trapList.isEmpty()) {
            sj.add(variantIdStr);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
        } else {
            for (Trap trap : trapList) {
                sj.add(variantIdStr);
                sj.add(trap.getGene());
                sj.add(FormatManager.getFloat(trap.getScore()));
            }
        }

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
