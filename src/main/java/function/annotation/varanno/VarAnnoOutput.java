package function.annotation.varanno;

import function.annotation.base.GeneManager;
import function.annotation.base.AnnotatedVariant;
import function.annotation.base.TranscriptManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
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
            + "Ref Allele,"
            + "Alt Allele,"
            + "Rs Number,"
            + "Transcript Stable Id,"
            + "Is CCDS Transcript,"
            + "Effect,"
            + "HGVS_c,"
            + "HGVS_p,"
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Gene Name,"
            + "Artifacts in Gene,"
            + "All Effect Gene Transcript HGVS_p,"
            + EvsManager.getTitle()
            + ExacManager.getTitle()
            + KnownVarManager.getTitle()
            + KaviarManager.getTitle()
            + GenomesManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle()
            + GerpManager.getTitle()
            + TrapManager.getTitle()
            + MgiManager.getTitle();

    public VarAnnoOutput(AnnotatedVariant var) {
        annotatedVar = var;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(annotatedVar.getVariantIdStr()).append(",");
        sb.append(annotatedVar.getType()).append(",");
        sb.append(annotatedVar.getRefAllele()).append(",");
        sb.append(annotatedVar.getAllele()).append(",");
        sb.append(annotatedVar.getRsNumber()).append(",");
        sb.append(annotatedVar.getStableId()).append(",");
        sb.append(TranscriptManager.isCCDSTranscript((annotatedVar.getStableId()))).append(",");
        sb.append(annotatedVar.getEffect()).append(",");
        sb.append(annotatedVar.getHGVS_c()).append(",");
        sb.append(annotatedVar.getHGVS_p()).append(",");
        sb.append(annotatedVar.getPolyphenHumdivScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarPrediction()).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(annotatedVar.getGeneName()))).append(",");
        sb.append(annotatedVar.getAllGeneTranscript()).append(",");
        sb.append(annotatedVar.getEvsStr());
        sb.append(annotatedVar.getExacStr());
        sb.append(annotatedVar.getKnownVarStr());
        sb.append(annotatedVar.getKaviarStr());
        sb.append(annotatedVar.get1000Genomes());
        sb.append(annotatedVar.getRvis());
        sb.append(annotatedVar.getSubRvis());
        sb.append(annotatedVar.getGerpScore());
        sb.append(annotatedVar.getTrapScore());
        sb.append(annotatedVar.getMgi());

        return sb.toString();
    }
}
