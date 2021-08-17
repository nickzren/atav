package function.cohort.singleton;

import function.cohort.base.Sample;

/**
 *
 * @author nick
 */
public class Singleton {

    private Sample child;

    public Singleton(Sample sample) {
        child = sample;
    }

    public Sample getChild() {
        return child;
    }
}
