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
            + "Clinvar Clinical Significance,"
            + "Clinvar Other Ids,"
            + "Clinvar Disease Name,"
            + "Clinvar Flanking Count,"
            + "HGMD Variant Class,"
            + "HGMD Pmid,"
            + "HGMD Disease Name,"
            + "HGMD Flanking Count,"
            + "OMIM Gene Name,"
            + "OMIM Disease Name";

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        variantIdStr = annotatedVar.variantIdStr;
        clinvar = new Clinvar(variantIdStr);
        hgmd = new HGMD(variantIdStr);
        omim = new OMIM(annotatedVar.getGeneName());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(variantIdStr).append(",");
        sb.append(clinvar.toString()).append(",");
        sb.append(hgmd.toString()).append(",");
        sb.append(omim.toString());
        
        return sb.toString();
    }
}
