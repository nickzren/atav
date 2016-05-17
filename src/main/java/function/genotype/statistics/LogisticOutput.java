package function.genotype.statistics;

import function.annotation.base.GeneManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Data;
import global.Index;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class LogisticOutput extends StatisticOutput {

    public static final String title
            = "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + "Is Minor Ref,"
            + "Major Hom Ctrl,"
            + "Het Ctrl,"
            + "Minor Hom Ctrl,"
            + "Minor Hom Ctrl Freq,"
            + "Het Ctrl Freq,"
            + "Missing Ctrl,"
            + "QC Fail Ctrl,"
            + "Ctrl Maf,"
            + "Ctrl HWE_P,"
            + "P value,"
            + "Avg Min Ctrl Cov,"
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + "Artifacts in Gene,"
            + "Codon Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle()
            + KaviarManager.getTitle()
            + KnownVarManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle()
            + GenomesManager.getTitle();

    public LogisticOutput(CalledVariant c) {
        super(c);
    }

    public boolean isValid(String model) {
        if (model.equals("recessive")) {
            if (!isRecessive()) {
                return false;
            }
        }

        if (isValid()) {
            return true;
        }

        return false;
    }

    public void doRegression(String model) {
        for (Sample sample : SampleManager.getList()) {
            sample.getCovariateList();
            
            // ......
            
            pValue = Data.NA;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCtrl).append(",");
        sb.append(sampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCtrl).append(",");
        sb.append(FormatManager.getDouble(ctrlMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(ctrlMaf)).append(",");
        sb.append(FormatManager.getDouble(ctrlHweP)).append(",");
        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(averageCov[Index.CTRL])).append(",");

        sb.append(calledVar.getEvsStr());

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        sb.append(calledVar.getKaviarStr());

        sb.append(calledVar.getKnownVarStr());

        sb.append(calledVar.getRvis());

        sb.append(calledVar.getSubRvis());

        sb.append(calledVar.get1000Genomes());

        return sb.toString();
    }
}
