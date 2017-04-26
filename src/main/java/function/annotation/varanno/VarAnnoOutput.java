package function.annotation.varanno;

import function.annotation.base.AnnotatedVariant;
import function.annotation.base.TranscriptManager;
import function.external.denovo.DenovoDBManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.gnomad.GnomADManager;
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
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + TrapManager.getTitle()
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + "Transcript Stable Id,"
            + "Has CCDS Transcript,"
            + "Codon Change,"
            + "Amino Acid Change,"
            + "Coding Sequence Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle()
            + GnomADManager.getTitle()
            + KaviarManager.getTitle()
            + KnownVarManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle()
            + GenomesManager.getTitle()
            + MgiManager.getTitle()
            + DenovoDBManager.getTitle();

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
        sb.append(annotatedVar.getGerpScore());
        sb.append(annotatedVar.getTrapScore());
        sb.append(annotatedVar.getEvsStr());
        sb.append(annotatedVar.getPolyphenHumdivScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarScore()).append(",");
        sb.append(annotatedVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(annotatedVar.getFunction()).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(annotatedVar.getStableId()).append(",");
        sb.append(annotatedVar.hasCCDS()).append(",");
        sb.append(annotatedVar.getCodonChange()).append(",");
        sb.append(annotatedVar.getAminoAcidChange()).append(",");
        sb.append(annotatedVar.getCodingSequenceChange()).append(",");
        sb.append(annotatedVar.getTranscriptSet()).append(",");
        sb.append(annotatedVar.getExacStr());
        sb.append(annotatedVar.getGnomADStr());
        sb.append(annotatedVar.getKaviarStr());
        sb.append(annotatedVar.getKnownVarStr());
        sb.append(annotatedVar.getRvis());
        sb.append(annotatedVar.getSubRvis());
        sb.append(annotatedVar.get1000Genomes());
        sb.append(annotatedVar.getMgi());
        sb.append(annotatedVar.getDenovoDB());

        return sb.toString();
    }
}
