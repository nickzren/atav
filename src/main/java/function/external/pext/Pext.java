package function.external.pext;

/**
 *
 * @author nick
 */
public class Pext {

    private String gene;
    private float score;

    public Pext(String gene, float score) {
        this.gene = gene;
        this.score = score;
    }

    public String getGene() {
        return gene;
    }

    public float getScore() {
        return score;
    }
}
