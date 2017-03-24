package function.annotation.base;

import function.variant.base.AnalysisBase4Variant;
import function.variant.base.RegionManager;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase4AnnotatedVar extends AnalysisBase4Variant {

    private AnnotatedVariant annotatedVar;

    public abstract void processVariant(AnnotatedVariant annotatedVar);

    @Override
    public void processDatabaseData() throws Exception {
        for (int r = 0; r < RegionManager.getRegionSize(); r++) {

            annotatedVar = null;

            analyzedRecords = 0;

            region = RegionManager.getRegion(r);

            rset = getAnnotationList(region);

            while (rset.next()) {
                annotation.init(rset, region.getChrStr());

                if (annotation.isValid()) {

                    nextVariantId = rset.getInt("variant_id");

                    if (annotatedVar == null
                            || nextVariantId != annotatedVar.getVariantId()) {
                        processVariant();

                        annotatedVar = new AnnotatedVariant(region.getChrStr(), nextVariantId, rset);
                    } // end of new one

                    annotatedVar.update(annotation);
                }
            }

            processVariant(); // only for the last qualified variant

            rset.close();
        }
    }

    private void processVariant() {
        if (annotatedVar != null
                && annotatedVar.isValid()) {
            annotatedVar.initExternalData();

            processVariant(annotatedVar);
        }

        countVariant();
    }
}
