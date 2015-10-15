package function.annotation.varanno;

import function.annotation.base.IntolerantScoreManager;
import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class VarAnnoOutput {

    AnnotatedVariant annotatedVar;

    public static final String annotationFileTitle
            = "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "C Score Phred,"
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + "Transcript Stable Id,"
            + "NM #,"
            + "NP #,"
            + "Codon Change,"
            + "Amino Acid Change,"
            + "Coding Sequence Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle() 
            + KaviarManager.getTitle()
            + KnownVarManager.getTitle();

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(annotatedVar.getVariantIdStr()).append(",");
        sb.append(annotatedVar.getType()).append(",");
        sb.append(annotatedVar.getRsNumber()).append(",");
        sb.append(annotatedVar.getRefAllele()).append(",");
        sb.append(annotatedVar.getAllele()).append(",");

        sb.append(FormatManager.getDouble(annotatedVar.getCscore())).append(",");

        sb.append(annotatedVar.getEvsStr());

        sb.append(annotatedVar.getPolyphenHumdivScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(annotatedVar.getFunction()).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(IntolerantScoreManager.getValues(annotatedVar.getGeneName())).append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(annotatedVar.getGeneName()))).append(",");

        sb.append(annotatedVar.getStableId()).append(",");
        sb.append(GeneManager.getNmNpValuesByStableId(annotatedVar.getStableId())).append(",");
        sb.append(annotatedVar.getCodonChange()).append(",");
        sb.append(annotatedVar.getAminoAcidChange()).append(",");
        sb.append(annotatedVar.getCodingSequenceChange()).append(",");
        sb.append(annotatedVar.getTranscriptSet()).append(",");

        sb.append(annotatedVar.getExacStr());
        
        sb.append(annotatedVar.getKaviarStr());
        
        sb.append(annotatedVar.getKnownVarStr());

        return sb.toString();
    }
}
