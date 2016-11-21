package function.genotype.collapsing;

import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.TranscriptManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
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
                + TrapManager.getTitle()
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
                + "LOO MAF,"
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
                + "Transcript Stable Id,"
                + "Is CCDS Transcript,"
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

    HashSet<String> regionBoundaryNameSet; // for --region-boundary only

    public CollapsingOutput(CalledVariant c) {
        super(c);

        geneName = c.getGeneName();
    }

    public void initRegionBoundaryNameSet() {
        regionBoundaryNameSet = RegionBoundaryManager.getNameSet(
                calledVar.getChrStr(),
                calledVar.getStartPosition());
    }

    public void calculateLooFreq(Sample sample) {
        if (sample.getId() != Data.NA) {
            int geno = calledVar.getGenotype(sample.getIndex());
            int pheno = (int) sample.getPheno();
            int type = getGenoType(geno, sample);

            deleteSampleGeno(type, pheno);

            calculateLooMaf();

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

    public boolean isMaxLooMafValid() {
        return CollapsingCommand.isMaxLooMafValid(looMAF);
    }

    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    @Override
    public boolean isQualifiedGeno(int geno) {
        if (CollapsingCommand.isRecessive && geno == 1) {
            return false;
        }

        return super.isQualifiedGeno(geno);
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        Carrier carrier = calledVar.getCarrier(sample.getId());
        int readsAlt = carrier != null ? carrier.getReadsAlt() : Data.NA;;
        int readsRef = carrier != null ? carrier.getReadsRef() : Data.NA;

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(isMinorRef).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
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
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoverage(sample.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getGatkFilteredCoverage() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(readsAlt)).append(",");
        sb.append(FormatManager.getInteger(readsRef)).append(",");
        sb.append(FormatManager.getPercAltRead(readsAlt, carrier != null ? carrier.getGatkFilteredCoverage() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getVqslod() : Data.NA)).append(",");
        sb.append(carrier != null ? carrier.getPassFailStatus() : "NA").append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getGenotypeQualGQ() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getStrandBiasFS() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getHaplotypeScore() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getRmsMapQualMQ() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getQualByDepthQD() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getQual() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getReadPosRankSum() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getMapQualRankSum() : Data.NA)).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(geneName).append("'").append(",");
        sb.append(calledVar.getStableId()).append(",");
        sb.append(TranscriptManager.isCCDSTranscript((calledVar.getStableId()))).append(",");
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
