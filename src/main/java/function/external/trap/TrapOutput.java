package function.external.trap;

import java.util.ArrayList;

/**
 *
 * @author nick
 */
public class TrapOutput {

    String variantId;
    ArrayList<Trap> trapList = new ArrayList<>();

    public static String getTitle() {
        return "Variant ID,"
                + "Gene,"
                + TrapManager.getTitle();
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
        StringBuilder sb = new StringBuilder();

        for (Trap trap : trapList) {
            sb.append(variantId).append(",");
            sb.append(trap.getGene()).append(",");
            sb.append(trap.getScore()).append("\n");
        }

        if (sb.length() == 0) {
            sb.append(variantId).append(",");
            sb.append("NA").append(",");
            sb.append("NA").append("\n");
        }

        return sb.toString();
    }
}
