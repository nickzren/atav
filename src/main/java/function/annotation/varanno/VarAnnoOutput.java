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

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.merge(Output.getVariantDataHeader());
        sj.merge(Output.getAnnotationDataHeader());
        sj.merge(Output.getExternalDataHeader());

        return sj.toString();
    }

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        annotatedVar.getVariantData(sj);
        annotatedVar.getAnnotationData(sj);
        annotatedVar.getExternalData(sj);

        return sj;
    }
    
    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
