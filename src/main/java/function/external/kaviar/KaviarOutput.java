package function.external.kaviar;

/**
 *
 * @author nick
 */
public class KaviarOutput {

    Kaviar kaviar;

    public static String getTitle() {
        return "Variant ID,"
                + KaviarManager.getTitle();
    }

    public KaviarOutput(String id) {
        kaviar = new Kaviar(id);
    }

    public boolean isValid() {
        return kaviar.isValid();
    }

    @Override
    public String toString() {
        return kaviar.toString();
    }
}
