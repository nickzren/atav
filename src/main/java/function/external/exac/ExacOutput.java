package function.external.exac;

/**
 *
 * @author nick
 */
public class ExacOutput {

    Exac exac;

    public static String getTitle() {
        return "Variant ID,"
                + ExacManager.getTitle();
    }

    public ExacOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        exac = new Exac(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public boolean isValid() {
        return exac.isValid();
    }

    @Override
    public String toString() {
        return exac.toString();
    }
}
