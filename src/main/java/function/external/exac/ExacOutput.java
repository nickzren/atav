package function.external.exac;

/**
 *
 * @author nick
 */
public class ExacOutput {

    Exac exac;

    public static final String title
            = "Variant ID,"
            + ExacManager.getTitle();

    public ExacOutput(String id) {
        exac = new Exac(id);
    }

    public boolean isValid() {
        return exac.isValid();
    }

    @Override
    public String toString() {
        return exac.toString();
    }
}
