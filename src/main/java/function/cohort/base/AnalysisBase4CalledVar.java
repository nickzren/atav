package function.cohort.base;

import function.variant.base.AnalysisBase4Variant;
import function.variant.base.RegionManager;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase4CalledVar extends AnalysisBase4Variant {

    private CalledVariant calledVar;

    public abstract void processVariant(CalledVariant calledVar);

    // only comphet function support it
    public abstract void doOutput();

    @Override
    public void processDatabaseData() throws Exception {
        for (int r = 0; r < RegionManager.getRegionSize(); r++) {

            calledVar = null;

            region = RegionManager.getRegion(r);

            rset = getAnnotationList(region);

            while (rset.next()) {
                annotation.init(rset, region.getChrStr());

                if (annotation.isValid()) {

                    nextVariantId = rset.getInt("variant_id");

                    if (calledVar == null
                            || nextVariantId != calledVar.getVariantId()) {
                        processVariant();

                        calledVar = new CalledVariant(region.getChrStr(), nextVariantId, rset);
                    } // end of new one

                    calledVar.update(annotation);
                }
            }

            processVariant(); // only for the last qualified variant

            rset.close();

            doOutput(); // only comphet function support it
        }
    }

    private void processVariant() {
        if (calledVar != null
                && calledVar.isValid()) {
            calledVar.initExternalData();

            processVariant(calledVar);
        }
    }
}
