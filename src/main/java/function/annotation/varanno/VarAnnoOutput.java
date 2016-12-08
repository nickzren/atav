package function.annotation.varanno;

import function.annotation.base.AnnotatedVariant;
import function.variant.base.Output;

/**
 *
 * @author nick
 */
public class VarAnnoOutput {

    AnnotatedVariant annotatedVar;

    public static String getTitle() {
        return Output.getVariantDataTitle()
                + Output.getAnnotationDataTitle()
                + Output.getExternalDataTitle();
    }

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        annotatedVar.getVariantData(sb);
        annotatedVar.getAnnotationData(sb);
        annotatedVar.getExternalData(sb);

        return sb.toString();
    }
}
