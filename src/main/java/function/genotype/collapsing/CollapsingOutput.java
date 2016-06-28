package function.genotype.collapsing;

import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import java.util.HashSet;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CollapsingOutput extends Output {

    public static String getTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Is Minor Ref,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + GerpManager.getTitle()
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
                + "Loo Maf,"
                + "Loo Minor Hom Freq,"
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
                + "Artifacts in Gene,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle();
    }

    String geneName = "";
    double looMAF = 0;
    double looMhgf = 0;

    HashSet<String> regionBoundaryNameSet; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);

        geneName = c.getGeneName();
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameSet = RegionBoundaryManager.getNameSet(
                calledVar.getRegion().getChrStr(),
                calledVar.getRegion().getStartPosition());
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
        int alleleCount = 2 * genoCount[Index.HOM][Index.ALL]
                + genoCount[Index.HET][Index.ALL]
                + genoCount[Index.HOM_MALE][Index.ALL];
        int totalCount = alleleCount + genoCount[Index.HET][Index.ALL]
                + 2 * genoCount[Index.REF][Index.ALL]
                + genoCount[Index.REF_MALE][Index.ALL];

        double allAF = MathManager.devide(alleleCount, totalCount);
        looMAF = allAF;

        if (allAF > 0.5) {
            isMinorRef = true;

            looMAF = 1.0 - allAF;
        } else {
            isMinorRef = false;
        }
    }

    private void calculateLooMhgf() {
        int allSample = genoCount[Index.HOM][Index.ALL]
                + genoCount[Index.HET][Index.ALL]
                + genoCount[Index.REF][Index.ALL]
                + genoCount[Index.HOM_MALE][Index.ALL]
                + genoCount[Index.REF_MALE][Index.ALL];

        looMhgf = MathManager.devide(genoCount[Index.HOM][Index.ALL]
                + genoCount[Index.HOM_MALE][Index.ALL], allSample); // hom / (hom + het + ref)

        if (isMinorRef) {
            looMhgf = MathManager.devide(genoCount[Index.REF][Index.ALL]
                    + genoCount[Index.REF_MALE][Index.ALL], allSample); // ref / (hom + het + ref)
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

        if (isMaxLooMafValid(isRecessive)) {
            return true;
        }

        return false;
    }

    public boolean isRecessive() {
        if (isMinorRef) {
            if (genoCount[Index.REF][Index.ALL]
                    + genoCount[Index.REF_MALE][Index.ALL] > 0) {
                return true;
            }
        } else if (genoCount[Index.HOM][Index.ALL]
                + genoCount[Index.HOM_MALE][Index.ALL] > 0) {
            return true;
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

        if (GenotypeLevelFilterCommand.isAllNonRef) {
            if (geno == 2 || geno == 1) {
                return true;
            }
        }

        if (isMinorRef) {
            if (geno == 0 || geno == 1) {
                return true;
            }
        } else if (geno == 2 || geno == 1) {
            return true;
        }

        return false;
    }

    public boolean isMaxLooMafValid(boolean isRecessive) {
        if (isRecessive) {
            return CollapsingCommand.isMaxLooMafRecValid(looMAF);
        } else {
            return CollapsingCommand.isMaxLooMafValid(looMAF);
        }
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
        sb.append(calledVar.getGerpScore());
        sb.append(getGenoStr(calledVar.getGenotype(sample.getIndex()))).append(",");
        sb.append(sample.getName()).append(",");
        sb.append(sample.getPhenotype()).append(",");
        sb.append(majorHomCount[Index.CASE]).append(",");
        sb.append(genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCount[Index.CASE]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CASE])).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CTRL])).append(",");
        sb.append(genoCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(looMAF)).append(",");
        sb.append(FormatManager.getDouble(looMhgf)).append(",");
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
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(geneName).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(geneName))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());

        return sb.toString();
    }
}
