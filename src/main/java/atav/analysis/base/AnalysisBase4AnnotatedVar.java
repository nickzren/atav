package atav.analysis.base;

import atav.global.Data;
import atav.manager.data.RegionManager;
import atav.manager.data.VariantManager;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase4AnnotatedVar extends AnalysisBase4Variant {

    private AnnotatedVariant annotatedVar;

    public abstract void processVariant(AnnotatedVariant annotatedVar);

    @Override
    public void processDatabaseData() throws Exception {
        totalNumOfRegionList = RegionManager.getRegionSize();

        for (int r = 0; r < totalNumOfRegionList; r++) {

            for (String varType : Data.VARIANT_TYPE) {

                if (VariantManager.isVariantTypeValid(r, varType)) {

                    isIndel = varType.equals("indel");

                    annotatedVar = null;

                    analyzedRecords = 0;

                    region = RegionManager.getRegion(r, varType);

                    rset = getAnnotationList(varType, region);

                    while (rset.next()) {
                        annotation.init(rset, isIndel);

                        if (annotation.isValid()) {

                            nextVariantId = rset.getInt(varType + "_id");

                            if (annotatedVar == null
                                    || nextVariantId != annotatedVar.getVariantId()) {
                                processVariant();

                                annotatedVar = new AnnotatedVariant(nextVariantId, isIndel, rset);
                            } // end of new one

                            annotatedVar.update(annotation);
                        }
                    }

                    processVariant(); // only for the last qualified variant

                    printTotalAnnotationCount(varType);

                    clearData();
                }
            }

            doOutput(); // only comphet function support it
        }
    }

    private void processVariant() {
        if (annotatedVar != null
                && annotatedVar.isValid()) {
            processVariant(annotatedVar);

            countVariant();
        }
    }
}
