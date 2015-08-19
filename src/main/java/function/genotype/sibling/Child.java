package function.genotype.sibling;

import function.genotype.base.Sample;

/**
 *
 * @author nick
 */
public class Child {

    private Sample sample;

    private int variantCount; // only count qualified variants within a gene

    public Child(Sample sample) {
        this.sample = sample;
        variantCount = 0;
    }

    public Sample getSample() {
        return sample;
    }

    public int getVariantCount() {
        return variantCount;
    }

    public void countVariant() {
        variantCount += 1;
    }

    public void resetVariantCount() {
        variantCount = 0;
    }
}
