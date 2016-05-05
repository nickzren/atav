package function.genotype.trio;

import function.external.evs.EvsManager;
import function.genotype.base.SampleManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class DenovoOutput extends TrioOutput {

    String flag = "";
    float avgCtrlCov = 0;
    public static final String title
            = "Family ID,"
            + "Child,"
            + "Mother,"
            + "Father,"
            + "Flag,"
            + "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + "Is Minor Ref,"
            + "Genotype (child),"
            + "Samtools Raw Coverage (child),"
            + "Gatk Filtered Coverage (child),"
            + "Reads Alt (child),"
            + "Reads Ref (child),"
            + "Percent Read Alt (child),"
            + "Pass Fail Status (child),"
            + "Genotype Qual GQ (child),"
            + "Qual By Depth QD (child),"
            + "Haplotype Score (child),"
            + "Rms Map Qual MQ (child),"
            + "Qual (child),"
            + "Genotype (mother),"
            + "Samtools Raw Coverage (mother),"
            + "Gatk Filtered Coverage (mother),"
            + "Reads Alt (mother),"
            + "Reads Ref (mother),"
            + "Percent Read Alt (mother),"
            + "Genotype (father),"
            + "Samtools Raw Coverage (father),"
            + "Gatk Filtered Coverage (father),"
            + "Reads Alt (father),"
            + "Reads Ref (father),"
            + "Percent Read Alt (father),"
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
            + "Average Ctrl Coverage,"
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

    public DenovoOutput(CalledVariant c) {
        super(c);
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getFlag() {
        return flag;
    }

    public void initFlag(int id) {
        convertParentGeno();

        flag = TrioManager.getStatus(calledVar.getRegion().getChrNum(),
                !isMinorRef, SampleManager.isMale(id),
                cGeno, cSamtoolsRawCoverage,
                mGeno, mSamtoolsRawCoverage,
                fGeno, fSamtoolsRawCoverage);
    }

    /*
     * convert all missing genotype to hom ref for parents
     */
    private void convertParentGeno() {
        if (mGeno == Data.NA) {
            mGeno = 0;
        }

        if (fGeno == Data.NA) {
            fGeno = 0;
        }
    }

    public void initAvgCov() {
        int sumCtrl = 0, numCtrl = 0, geno;
        for (Sample sample : SampleManager.getList()) {
            geno = calledVar.getGenotype(sample.getIndex());
            if (geno != Data.NA) {
                if (!sample.isCase()) { // control
                    sumCtrl += calledVar.getCoverage(sample.getIndex());
                    numCtrl++;
                }
            }
        }

        if (numCtrl > 0) {
            avgCtrlCov = (float) sumCtrl / numCtrl;
        }
    }

    public void initGenoZygo(int childIndex) {
        cGenotype = getGenoStr(calledVar.getGenotype(childIndex));
        mGenotype = getGenoStr(mGeno);
        fGenotype = getGenoStr(fGeno);
    }

    public String getString(Trio trio) {
        StringBuilder sb = new StringBuilder();

        sb.append(familyId).append(",");
        sb.append(childName).append(",");
        sb.append(motherName).append(",");
        sb.append(fatherName).append(",");
        sb.append(flag).append(",");
        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(isMinorRef).append(",");

        sb.append(cGenotype).append(",");
        sb.append(FormatManager.getDouble(cSamtoolsRawCoverage)).append(",");
        sb.append(FormatManager.getDouble(cGatkFilteredCoverage)).append(",");
        sb.append(FormatManager.getDouble(cReadsAlt)).append(",");
        sb.append(FormatManager.getDouble(cReadsRef)).append(",");
        sb.append(FormatManager.getPercAltRead(cReadsAlt, cGatkFilteredCoverage)).append(",");

        sb.append(calledVar.getPassFailStatus(trio.getChildId())).append(",");
        sb.append(FormatManager.getDouble(calledVar.getGenotypeQualGQ(trio.getChildId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQualByDepthQD(trio.getChildId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getHaplotypeScore(trio.getChildId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getRmsMapQualMQ(trio.getChildId()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getQual(trio.getChildId()))).append(",");

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
        sb.append(FormatManager.getDouble(avgCtrlCov)).append(",");

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
