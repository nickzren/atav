package function.genotype.trio;

import function.genotype.base.CalledVariant;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.IntolerantScoreManager;
import function.variant.base.VariantLevelFilterCommand;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends TrioOutput implements Comparable {

    public static final String title
            = "Family ID,"
            + "Child,"
            + "Sample Type (Child),"
            + "Mother,"
            + "Father,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + "Flag,"
            + "Multi qualified var combinations,"
            + "Var Case Freq #1 & #2 (co-occurance),"
            + "Var Ctrl Freq #1 & #2 (co-occurance),"
            + initVarTitleStr("1") + ","
            + initVarTitleStr("2");

    private static String initVarTitleStr(String var) {
        String varTitle = "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "C Score Phred,"
                + "Is Minor Ref,"
                + "Genotype (child),"
                + "Samtools Raw Coverage (child),"
                + "Gatk Filtered Coverage (child),"
                + "Reads Alt (child),"
                + "Reads Ref (child),"
                + "Percent Alt Read (child),"
                + "Genotype (mother),"
                + "Samtools Raw Coverage (mother),"
                + "Gatk Filtered Coverage (mother),"
                + "Reads Alt (mother),"
                + "Reads Ref (mother),"
                + "Percent Alt Read (mother),"
                + "Genotype (father),"
                + "Samtools Raw Coverage (father),"
                + "Gatk Filtered Coverage (father),"
                + "Reads Alt (father),"
                + "Reads Ref (father),"
                + "Percent Alt Read (father),"
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
                + "Case MAF,"
                + "Ctrl MAF,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle();

        String[] list = varTitle.split(",");

        varTitle = "";

        boolean isFirst = true;

        for (String s : list) {
            if (isFirst) {
                varTitle += s + " (#" + var + ")";
                isFirst = false;
            } else {
                varTitle += "," + s + " (#" + var + ")";
            }
        }

        return varTitle;
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
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

        sb.append(isMinorRef).append(",");

        sb.append(cGenotype).append(",");
        sb.append(FormatManager.getDouble(cSamtoolsRawCoverage)).append(",");
        sb.append(FormatManager.getDouble(cGatkFilteredCoverage)).append(",");
        sb.append(FormatManager.getDouble(cReadsAlt)).append(",");
        sb.append(FormatManager.getDouble(cReadsRef)).append(",");
        sb.append(FormatManager.getPercAltRead(cReadsAlt, cGatkFilteredCoverage)).append(",");

        sb.append(mGenotype).append(",");
        sb.append(FormatManager.getDouble(mSamtoolsRawCoverage)).append(",");
        sb.append(FormatManager.getDouble(mGatkFilteredCoverage)).append(",");
        sb.append(FormatManager.getDouble(mReadsAlt)).append(",");
        sb.append(FormatManager.getDouble(mReadsRef)).append(",");
        sb.append(FormatManager.getPercAltRead(mReadsAlt, mGatkFilteredCoverage)).append(",");

        sb.append(fGenotype).append(",");
        sb.append(FormatManager.getDouble(fSamtoolsRawCoverage)).append(",");
        sb.append(FormatManager.getDouble(fGatkFilteredCoverage)).append(",");
        sb.append(FormatManager.getDouble(fReadsAlt)).append(",");
        sb.append(FormatManager.getDouble(fReadsRef)).append(",");
        sb.append(FormatManager.getPercAltRead(fReadsAlt, fGatkFilteredCoverage)).append(",");

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

        if (VariantLevelFilterCommand.isOldEvsUsed) {
            sb.append(calledVar.getEvsCoverageStr()).append(",");
            sb.append(calledVar.getEvsMafStr()).append(",");
            sb.append(calledVar.getEvsFilterStatus()).append(",");
        }

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        return sb.toString();
    }

    public int compareTo(Object another) throws ClassCastException {
        CompHetOutput that = (CompHetOutput) another;
        return this.getCalledVariant().getGeneName().compareTo(
                that.getCalledVariant().getGeneName()); //small -> large
    }
}
