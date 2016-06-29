package function.external.genomes;

/**
 *
 * @author nick
 */
public class GenomesOutput {

    Genomes genomes;

    public static String getTitle() {
        return "Variant ID,"
                + GenomesManager.getTitle();
    }

    public GenomesOutput(String id) {
        String[] tmp = id.split("-"); // chr-pos-ref-alt
        genomes = new Genomes(tmp[0], Integer.parseInt(tmp[1]), tmp[2], tmp[3]);
    }

    public boolean isValid() {
        return genomes.isValid();
    }

    @Override
    public String toString() {
        return genomes.toString();
    }
}
