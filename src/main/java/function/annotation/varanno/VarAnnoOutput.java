package function.annotation.varanno;

import function.annotation.base.IntolerantScoreManager;
import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.variant.base.VariantLevelFilterCommand;
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
            + ExacManager.getTitle();

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

        if (VariantLevelFilterCommand.isOldEvsUsed) {
            sb.append(annotatedVar.getEvsCoverageStr()).append(",");
            sb.append(annotatedVar.getEvsMafStr()).append(",");
            sb.append(annotatedVar.getEvsFilterStatus()).append(",");
        }

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

        return sb.toString();
    }
}
