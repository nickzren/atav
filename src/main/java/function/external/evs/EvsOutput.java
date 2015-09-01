package function.external.evs;

/**
 *
 * @author nick
 */
public class EvsOutput {

    Evs evs;

    public static final String title
            = "Variant ID,"
            + EvsManager.getTitle();

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
