package atav.analysis.vargeno;

import atav.analysis.base.CalledVariant;
import atav.analysis.base.Output;
import atav.analysis.base.Sample;
import atav.global.Index;
import atav.manager.data.EvsManager;
import atav.manager.data.ExacManager;
import atav.manager.data.GeneManager;
import atav.manager.data.IntolerantScoreManager;
import atav.manager.utils.CommandValue;
import atav.manager.utils.FormatManager;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    String sampleType;
    String genoType;
    public static final String title
            = "Variant ID,"
            + "New Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "C Score Phred,"
            + "Is Minor Ref,"
            + "Genotype,"
            + "Sample Name,"
            + "Sample Type,"
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

    public VarGenoOutput(CalledVariant c) {
        super(c);
    }

    public void initGenoType(int geno) {
        genoType = getGenoStr(geno);
    }

    public void initPhenoType(int pheno) {
        switch (pheno) {
            case 0:
                sampleType = "ctrl";
                break;
            case 1:
                sampleType = "case";
        }
    }

    @Override
    public boolean isRecessive() {
        if (isMinorRef) {
            if (sampleCount[Index.REF][Index.CASE]
                    + sampleCount[Index.REF_MALE][Index.CASE] > 0) {
                return true;
            }
        } else {
            if (sampleCount[Index.HOM][Index.CASE]
                    + sampleCount[Index.HOM_MALE][Index.CASE] > 0) {
                return true;
            }
        }

        return false;
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getNewVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(isMinorRef).append(",");
        sb.append(genoType).append(",");
        sb.append(sample.getName()).append(",");
        sb.append(sampleType).append(",");
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
        sb.append(FormatManager.getDouble(calledVar.getCoverage(sample.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getGatkFilteredCoverage(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadsAlt(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadsRef(sample.getId()))).append(",");
        sb.append(FormatManager.getPercAltRead(calledVar.getReadsAlt(sample.getId()),
                calledVar.getGatkFilteredCoverage(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getVqslod(sample.getId()))).append(",");
        sb.append(calledVar.getPassFailStatus(sample.getId())).append(",");
        sb.append(FormatManager.getDouble(calledVar.getGenotypeQualGQ(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getStrandBiasFS(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getHaplotypeScore(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getRmsMapQualMQ(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQualByDepthQD(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQual(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getReadPosRankSum(sample.getId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getMapQualRankSum(sample.getId()))).append(",");

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
