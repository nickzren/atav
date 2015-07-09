package function.genotype.collapsing;

import function.genotype.base.QualityManager;
import function.external.evs.EvsManager;
import function.annotation.base.IntolerantScoreManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingOutput extends Output implements Comparable {

    String geneName = "";
    String sampleName;
    String sampleType;
    String genoType;
    double varAllFreq = 0;
    double looMaf = 0;
    double looMhgf = 0;
    public static String title
            = "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Is Minor Ref,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "C Score Phred,"
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

    public CollapsingOutput(CalledVariant c) {
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

    public void calculateLooFreq(Sample sample) {
        if (sample.getId() != Data.NA) {
            int geno = calledVar.getGenotype(sample.getIndex());
            int pheno = (int) sample.getPheno();
            int type = getGenoType(geno, sample);

            deleteSampleGeno(type, pheno);

            calculateLooMaf();

            calculateLooMhgf();

            addSampleGeno(type, pheno);
        }
    }

    private void calculateLooMaf() {
        int totalVar = 2 * sampleCount[Index.HOM][Index.ALL]
                + sampleCount[Index.HET][Index.ALL]
                + sampleCount[Index.HOM_MALE][Index.ALL];
        int totalNum = totalVar + sampleCount[Index.HET][Index.ALL]
                + 2 * sampleCount[Index.REF][Index.ALL]
                + sampleCount[Index.REF_MALE][Index.ALL];

        varAllFreq = FormatManager.devide(totalVar, totalNum);
        looMaf = varAllFreq;

        if (varAllFreq > 0.5) {
            isMinorRef = true;

            looMaf = 1.0 - varAllFreq;
        } else {
            isMinorRef = false;
        }
    }

    private void calculateLooMhgf() {
        int allSample = sampleCount[Index.HOM][Index.ALL]
                + sampleCount[Index.HET][Index.ALL]
                + sampleCount[Index.REF][Index.ALL]
                + sampleCount[Index.HOM_MALE][Index.ALL]
                + sampleCount[Index.REF_MALE][Index.ALL];

        looMhgf = FormatManager.devide(sampleCount[Index.HOM][Index.ALL]
                + sampleCount[Index.HOM_MALE][Index.ALL], allSample); // hom / (hom + het + ref)

        if (isMinorRef) {
            looMhgf = FormatManager.devide(sampleCount[Index.REF][Index.ALL]
                    + sampleCount[Index.REF_MALE][Index.ALL], allSample); // ref / (hom + het + ref)
        }
    }

    public boolean isLooFreqValid() {
        boolean isRecessive = false;

        if (CollapsingCommand.isRecessive) {
            isRecessive = isRecessive();

            if (!isRecessive) {
                return false;
            }
        }

        if (isLooMafValid(isRecessive)) {
            if (isRecessive) {
                if (isLooMhgf4RecessiveValid()
                        && calledVar.isEvsMhgfValid()
                        && QualityManager.isMinHomCaseRecValid(minorHomCase)) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    @Override
    public boolean isQualifiedGeno(int geno) {
        if (CollapsingCommand.isRecessive && geno == 1) { // just for collapsing function now
            return false;
        }

        if (isMinorRef && !CommonCommand.isAllNonRef) {
            if (geno == 0 || geno == 1) {
                return true;
            }
        } else {
            if (geno == 2 || geno == 1) {
                return true;
            }
        }

        return false;
    }

    public boolean isLooMafValid(boolean isRecessive) {
        if (isRecessive) {
            return QualityManager.isMaf4RecessiveValid(looMaf);
        } else {
            return QualityManager.isMafValid(looMaf);
        }
    }

    public boolean isLooMhgf4RecessiveValid() {
        if (QualityManager.isMhgf4RecessiveValid(looMhgf)) {
            return true;
        }

        return false;
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(isMinorRef).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(genoType).append(",");
        sb.append(sampleName).append(",");
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

        if (CommonCommand.isOldEvsUsed) {
            sb.append(calledVar.getEvsCoverageStr()).append(",");
            sb.append(calledVar.getEvsMafStr()).append(",");
            sb.append(calledVar.getEvsFilterStatus()).append(",");
        }

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(geneName).append("'").append(",");
        sb.append(IntolerantScoreManager.getValues(geneName)).append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(geneName))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        return sb.toString();
    }

    public int compareTo(Object another) throws ClassCastException {
        CollapsingOutput that = (CollapsingOutput) another;
        return this.geneName.compareTo(that.geneName); //small -> large
    }
}
