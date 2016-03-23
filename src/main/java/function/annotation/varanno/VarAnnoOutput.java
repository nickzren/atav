package function.annotation.varanno;

import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.subrvis.SubRvisOutput;
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
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
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
            + KnownVarManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle();

    private String rvisStr = "";

    private SubRvisOutput subRvisOutput;

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;

        initRvis();

        initSubRvis();
    }

    private void initRvis() {
        if (RvisCommand.isIncludeRvis && rvisStr.isEmpty()) {
            rvisStr = RvisManager.getLine(annotatedVar.getGeneName());
        }
    }

    private void initSubRvis() {
        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(annotatedVar.getGeneName(),
                    annotatedVar.getRegion().getChrStr(),
                    annotatedVar.getRegion().getStartPosition());
        }
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
        sb.append(annotatedVar.getGerpScore()).append(",");

        sb.append(annotatedVar.getEvsStr());

        sb.append(annotatedVar.getPolyphenHumdivScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(annotatedVar.getFunction()).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
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

        sb.append(rvisStr);

        sb.append(subRvisOutput.toString());

        return sb.toString();
    }
}
