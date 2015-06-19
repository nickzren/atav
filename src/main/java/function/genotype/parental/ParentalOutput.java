package function.genotype.parental;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import function.genotype.base.QualityManager;
import global.Data;
import utils.CommandValue;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class ParentalOutput extends Output {

    Sample child;
    int childGeno;
    double childBinomial;

    Sample parent;
    int parentGeno;
    double parentBinomial;

    public static final String title
            = "Family Id,"
            + "Sample Name (child),"
            + "Genotype (child),"
            + "Binomial (child),"
            + "Sample Name (parent),"
            + "Genotype (parent),"
            + "Binomial (parent),"
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
            + "Samtools Raw Coverage (child),"
            + "Gatk Filtered Coverage (child),"
            + "Reads Alt (child),"
            + "Reads Ref (child),"
            + "Percent Alt Read (child),"
            + "Vqslod (child),"
            + "Pass Fail Status (child),"
            + "Genotype Qual GQ (child),"
            + "Strand Bias FS (child),"
            + "Haplotype Score (child),"
            + "Rms Map Qual MQ (child),"
            + "Qual By Depth QD (child),"
            + "Qual (child),"
            + "Read Pos Rank Sum (child),"
            + "Map Qual Rank Sum (child),"
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

    public void setChild(Sample child) {
        this.child = child;
        childGeno = calledVar.getGenotype(child.getIndex());
    }

    public boolean isChildValid() {
        return isQualifiedGeno(childGeno)
                && isChildQdValid()
                && isChildHetPercentAltReadValid()
                && isChildBinomialValid();
    }

    private boolean isChildQdValid() {
        float value = Data.NA;

        if (CommandValue.childQD != Data.NO_FILTER) {
            value = calledVar.getQualByDepthQD(child.getId());
        }

        return QualityManager.isChildQdValid(value);
    }

    private boolean isChildHetPercentAltReadValid() {
        double percAltRead = Data.NA;

        if (CommandValue.childHetPercentAltRead != null
                && childGeno == 1) {
            int readsAlt = calledVar.getReadsAlt(child.getId());
            int gatkFilteredCoverage = calledVar.getGatkFilteredCoverage(child.getId());

            percAltRead = FormatManager.devide(readsAlt, gatkFilteredCoverage);
        }

        return QualityManager.isChildHetPercentAltReadValid(percAltRead);
    }

    private boolean isChildBinomialValid() {
        int readsAlt = calledVar.getReadsAlt(child.getId());
        int readsRef = calledVar.getReadsRef(child.getId());

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            childBinomial = Data.NA;
        } else {
            childBinomial = MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5);
        }

        return QualityManager.isChildBinomialValid(childBinomial);
    }

    public void setParent(Sample parent) {
        this.parent = parent;
    }

    public boolean isParentValid() {
        return isParentBinomialValid();
    }

    private boolean isParentBinomialValid() {
        parentBinomial = Data.NA;

        int readsAlt = calledVar.getReadsAlt(parent.getId());
        int readsRef = calledVar.getReadsRef(parent.getId());

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            parentBinomial = Data.NA;
        } else {
            parentBinomial = MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5);
        }

        return QualityManager.isParentBinomialValid(parentBinomial);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append(child.getFamilyId()).append(",");
        sb.append(child.getName()).append(",");
        sb.append(getGenoStr(childGeno)).append(",");
        sb.append(FormatManager.getDouble(childBinomial)).append(",");
        sb.append(parent.getName()).append(",");
        sb.append(getGenoStr(parentGeno)).append(",");
        sb.append(FormatManager.getDouble(parentBinomial)).append(",");

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
