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
        evs = new Evs(id);
    }

    public boolean isValid() {
        return evs.isValid();
    }

    @Override
    public String toString() {
        return evs.toString();
    }
}
