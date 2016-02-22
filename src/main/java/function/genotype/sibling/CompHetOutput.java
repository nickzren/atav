package function.genotype.sibling;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.IntolerantScoreManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends Output implements Comparable {

    String geneName = "";

    public static final String title
            = "Family ID,"
            + "Mother,"
            + "Father,"
            + "Flag,"
            + "Child1,"
            + "Child1 Trio Comp Het Flag,"
            + "Child2,"
            + "Child2 Trio Comp Het Flag,"
            + "Gene Name,"
            + IntolerantScoreManager.getTitle()
            + "Artifacts in Gene,"
            + initVarTitleStr("1")
            + initVarTitleStr("2");

    private static String initVarTitleStr(String var) {
        String varTitle = "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + "Is Minor Ref,"
                + "Child1 Genotype,"
                + "Child1 Samtools Raw Coverage,"
                + "Child2 Genotype,"
                + "Child2 Samtools Raw Coverage,"
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
                + ExacManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle();

        String[] list = varTitle.split(",");

        varTitle = "";

        for (String s : list) {
            varTitle += s + " (#" + var + "),";
        }

        return varTitle;
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample child1, Sample child2) {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");

        sb.append(isMinorRef).append(",");

        sb.append(getGenoStr(calledVar.getGenotype(child1.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoverage(child1.getIndex()))).append(",");
        sb.append(getGenoStr(calledVar.getGenotype(child2.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoverage(child2.getIndex()))).append(",");

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

        sb.append(calledVar.getEvsStr());

        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");

        sb.append(calledVar.getFunction()).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");

        sb.append(calledVar.getExacStr());

        sb.append(calledVar.getKaviarStr());

        sb.append(calledVar.getKnownVarStr());

        return sb.toString();
    }

    public int compareTo(Object another) throws ClassCastException {
        CompHetOutput that = (CompHetOutput) another;
        return this.geneName.compareTo(that.geneName); //small -> large
    }
}
