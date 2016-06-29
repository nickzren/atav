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
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        kaviar = new Kaviar(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public boolean isValid() {
        return kaviar.isValid();
    }

    @Override
    public String toString() {
        return kaviar.toString();
    }
}
