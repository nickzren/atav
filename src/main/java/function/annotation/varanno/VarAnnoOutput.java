package function.annotation.varanno;

import function.annotation.base.AnnotatedVariant;
import function.variant.base.Output;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class VarAnnoOutput {

    AnnotatedVariant annotatedVar;

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(Output.getVariantDataTitle());
        sj.add(Output.getAnnotationDataTitle());
        sj.add(Output.getExternalDataTitle());

        return sj.toString();
    }

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        annotatedVar.getVariantData(sj);
        annotatedVar.getAnnotationData(sj);
        annotatedVar.getExternalData(sj);

        return sj.toString();
    }
}
