package function.external.evs;

/**
 *
 * @author nick
 */
public class EvsOutput {

    Evs evs;

    public static String getTitle() {
        return "Variant ID,"
                + EvsManager.getTitle();
    }

    public EvsOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        evs = new Evs(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public boolean isValid() {
        return evs.isValid();
    }

    @Override
    public String toString() {
        return evs.toString();
    }
}
