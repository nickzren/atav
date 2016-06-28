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
        genomes = new Genomes(id);
    }

    public boolean isValid() {
        return genomes.isValid();
    }

    @Override
    public String toString() {
        return genomes.toString();
    }
}
