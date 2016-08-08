package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;

/**
 *
 * @author nick
 */
public class DenovoOutput extends TrioOutput {

    String denovoFlag = "";

    public DenovoOutput(CalledVariant c) {
        super(c);
    }

    public void initDenovoFlag(Sample child) {
        convertParentGeno();

        denovoFlag = TrioManager.getStatus(calledVar.getChrNum(),
                !isMinorRef, child.isMale(),
                cGeno, cSamtoolsRawCoverage,
                mGeno, mSamtoolsRawCoverage,
                fGeno, fSamtoolsRawCoverage);
    }

    /*
     * convert all missing genotype to hom ref for parents
     */
    private void convertParentGeno() {
        if (mGeno == Data.NA) {
            mGeno = Index.REF;
        }

        if (fGeno == Data.NA) {
            fGeno = Index.REF;
        }
    }
}
