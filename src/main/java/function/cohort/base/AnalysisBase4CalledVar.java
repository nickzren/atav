package function.cohort.base;

import function.annotation.base.GeneManager;
import function.variant.base.AnalysisBase4Variant;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;

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

            // when --case-only used and case num > 0
            VariantManager.initCaseVariantTable(region.getChrStr());

            // when --gene or --gene-boundary used 
            if (GeneManager.isUsed()) {
                while (!GeneManager.isEmpty(region.getChrStr())) {
                    GeneManager.resetTempTable(region.getChrStr());

                    iterateVariantAnnotation();
                }
            } else {
                iterateVariantAnnotation();
            }

            // clear temp case variant able
            VariantManager.dropCaseVariantTable(region.getChrStr());
        }
    }

    private void iterateVariantAnnotation() throws Exception {
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

        // only for the last qualified variant
        processVariant();

        rset.close();

        // only comphet function support it
        doOutput();
    }

    private void processVariant() {
        if (calledVar != null
                && calledVar.isValid()) {
            calledVar.initExternalData();

            processVariant(calledVar);
        }
    }
}
