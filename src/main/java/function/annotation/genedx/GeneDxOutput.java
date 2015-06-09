package function.annotation.genedx;

import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class GeneDxOutput {

    AnnotatedVariant annotatedVar;

    public static final String geneDxFileTitle
            = "Variant ID,"
            + "New Variant ID,"
            + "Gene Name,"
            + "NM #,"
            + "NP #,"
            + "Coding Sequence Change,"
            //            + "Codon Change,"
            + "Amino Acid Change\n";

    public GeneDxOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    public String getGeneDxString() {
        StringBuilder sb = new StringBuilder();

        sb.append(annotatedVar.getVariantIdStr()).append(",");
        sb.append(annotatedVar.getNewVariantIdStr()).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(GeneManager.getNmNpValuesByStableId(annotatedVar.getStableId())).append(",");
        sb.append(annotatedVar.getCodingSequenceChange()).append(",");
//        sb.append(annotatedVar.getCodonChange()).append(",");
        sb.append(annotatedVar.getAminoAcidChange()).append("\n");

        return sb.toString();
    }
}
