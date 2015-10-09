package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    String variantIdStr;
    Clinvar clinvar;
    HGMD hgmd;
    OMIM omim;

    public static final String title
            = "Variant ID,"
            + KnownVarManager.getTitle();

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        variantIdStr = annotatedVar.variantIdStr;
        clinvar = new Clinvar(variantIdStr);
        hgmd = new HGMD(variantIdStr);
        omim = new OMIM(annotatedVar.getGeneName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinvar.toString()).append(",");
        sb.append(hgmd.toString()).append(",");
        sb.append(omim.toString());

        return sb.toString();
    }
}
