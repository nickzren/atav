package function.annotation.base;

import function.AnalysisBase;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase4AnnotatedGene extends AnalysisBase {

    public abstract void processGene(String gene);

    @Override
    public void processDatabaseData() throws Exception {
        for (String gene : GeneManager.getList()) {
            processGene(gene);
        }
    }
}
