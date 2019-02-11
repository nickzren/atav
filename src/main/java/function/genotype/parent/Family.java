package function.genotype.parent;

import function.genotype.base.Sample;
import function.genotype.base.SampleManager;

/**
 *
 * @author nick
 */
public class Family {

    private Sample child;
    private Sample mother;
    private Sample father;

    public Family(Sample sample) {
        child = sample;
        mother = SampleManager.getSampleByName(child.getMaternalId());
        father = SampleManager.getSampleByName(child.getPaternalId());
    }

    public boolean isValid() {
        return father != null
                && !father.isCase()
                && mother != null
                && !mother.isCase();
    }

    public Sample getChild() {
        return child;
    }

    public Sample getMother() {
        return mother;
    }

    public Sample getFather() {
        return father;
    }
}
