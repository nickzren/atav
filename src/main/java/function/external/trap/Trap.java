package function.external.trap;

/**
 *
 * @author nick
 */
public class Trap {

    private String gene;
    private float score;

    public Trap(String gene, float score) {
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
