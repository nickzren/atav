package function.genotype.parental;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import utils.CommandValue;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class ParentalOutput extends Output {

    public static final String title
            = "Family Id,"
            + "Sample Name (child),"
            + "Genotype (child),"
            + "Sample Name (parent),"
            + "Genotype (parent),"
            + "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "C Score Phred,"
            + "Is Minor Ref,"
            + "Major Hom Case,"
            + "Het Case,"
            + "Minor Hom Case,"
            + "Minor Hom Case Freq,"
            + "Het Case Freq,"
            + "Major Hom Ctrl,"
            + "Het Ctrl,"
            + "Minor Hom Ctrl,"
            + "Minor Hom Ctrl Freq,"
            + "Het Ctrl Freq,"
            + "Missing Case,"
            + "QC Fail Case,"
            + "Missing Ctrl,"
            + "QC Fail Ctrl,"
            + "Case Maf,"
            + "Ctrl Maf,"
            + "Case HWE_P,"
            + "Ctrl HWE_P,"
            + "Samtools Raw Coverage,"
            + "Gatk Filtered Coverage,"
            + "Reads Alt,"
            + "Reads Ref,"
            + "Percent Alt Read,"
            + "Vqslod,"
            + "Pass Fail Status,"
            + "Genotype Qual GQ,"
            + "Strand Bias FS,"
            + "Haplotype Score,"
            + "Rms Map Qual MQ,"
            + "Qual By Depth QD,"
            + "Qual,"
            + "Read Pos Rank Sum,"
            + "Map Qual Rank Sum,"
            + EvsManager.getTitle()
            + "Polyphen Humdiv Score,"
            + "Polyphen Humdiv Prediction,"
            + "Polyphen Humvar Score,"
            + "Polyphen Humvar Prediction,"
            + "Function,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + "Codon Change,"
            + "Gene Transcript (AA Change),"
            + ExacManager.getTitle();

    public ParentalOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample child, int childGeno,
            Sample parent, int parentGeno) {
        StringBuilder sb = new StringBuilder();

        sb.append(child.getFamilyId()).append(",");
        sb.append(child.getName()).append(",");
        sb.append(getGenoStr(childGeno)).append(",");
        sb.append(parent.getName()).append(",");
        sb.append(getGenoStr(parentGeno)).append(",");

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCase).append(",");
        sb.append(sampleCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCase).append(",");
        sb.append(FormatManager.getDouble(caseMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CASE])).append(",");
        sb.append(majorHomCtrl).append(",");
        sb.append(sampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCtrl).append(",");
        sb.append(FormatManager.getDouble(ctrlMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(caseMaf)).append(",");
        sb.append(FormatManager.getDouble(ctrlMaf)).append(",");
        sb.append(FormatManager.getDouble(caseHweP)).append(",");
        sb.append(FormatManager.getDouble(ctrlHweP)).append(",");

        sb.append(FormatManager.getDouble(calledVar.getCoverage(child.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getGatkFilteredCoverage(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadsAlt(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadsRef(child.getId()))).append(",");
        sb.append(FormatManager.getPercAltRead(calledVar.getReadsAlt(child.getId()),
                calledVar.getGatkFilteredCoverage(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getVqslod(child.getId()))).append(",");
        sb.append(calledVar.getPassFailStatus(child.getId())).append(",");
        sb.append(FormatManager.getDouble(calledVar.getGenotypeQualGQ(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getStrandBiasFS(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getHaplotypeScore(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getRmsMapQualMQ(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQualByDepthQD(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQual(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadPosRankSum(child.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getMapQualRankSum(child.getId()))).append(",");

        if (CommandValue.isOldEvsUsed) {
            sb.append(calledVar.getEvsCoverageStr()).append(",");
            sb.append(calledVar.getEvsMafStr()).append(",");
            sb.append(calledVar.getEvsFilterStatus()).append(",");
        }

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(IntolerantScoreManager.getValues(calledVar.getGeneName())).append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        return sb.toString();
    }
}
