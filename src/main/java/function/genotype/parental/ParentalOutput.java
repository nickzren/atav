package function.genotype.parental;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.gnomad.GnomADManager;
import function.external.denovo.DenovoDBManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.Carrier;
import global.Data;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
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

    public static String getTitle() {
        return "Family Id,"
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
                + "CADD Score Phred,"
                + GerpManager.getTitle()
                + TrapManager.getTitle()
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
                + "Transcript Stable Id,"
                + "Has CCDS Transcript,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + GnomADManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle()
                + DenovoDBManager.getTitle();
    }

    public ParentalOutput(CalledVariant c) {
        super(c);
    }

    public boolean isChildValid(Sample child) {
        this.child = child;
        Carrier carrier = calledVar.getCarrier(child.getId());

        return isChildGenoValid()
                && isChildQdValid(carrier)
                && isChildHetPercentAltReadValid(carrier)
                && isChildBinomialValid(carrier);
    }

    private boolean isChildGenoValid() {
        childGeno = calledVar.getGenotype(child.getIndex());

        return isQualifiedGeno(childGeno);
    }

    private boolean isChildQdValid(Carrier carrier) {
        float value = Data.NA;

        if (ParentalCommand.childQD != Data.NO_FILTER) {
            value = carrier != null ? carrier.getQualByDepthQD() : Data.NA;
        }

        return ParentalCommand.isChildQdValid(value);
    }

    private boolean isChildHetPercentAltReadValid(Carrier carrier) {
        double percAltRead = Data.NA;

        if (ParentalCommand.childHetPercentAltRead != null
                && childGeno == Index.HET) {
            int readsAlt = carrier != null ? carrier.getReadsAlt() : Data.NA;
            int gatkFilteredCoverage = carrier != null ? carrier.getGatkFilteredCoverage() : Data.NA;

            percAltRead = MathManager.devide(readsAlt, gatkFilteredCoverage);
        }

        return ParentalCommand.isChildHetPercentAltReadValid(percAltRead);
    }

    private boolean isChildBinomialValid(Carrier carrier) {
        int readsAlt = carrier != null ? carrier.getReadsAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getReadsRef() : Data.NA;

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            childBinomial = Data.NA;
        } else {
            childBinomial = MathManager.getBinomialLessThan(readsAlt + readsRef, readsAlt, 0.5f, AlternativeHypothesis.LESS_THAN);
        }

        return ParentalCommand.isChildBinomialValid(childBinomial);
    }

    public boolean isParentValid(Sample parent) {
        this.parent = parent;
        parentGeno = calledVar.getGenotype(parent.getIndex());

        return isParentBinomialValid();
    }

    private boolean isParentBinomialValid() {
        parentBinomial = Data.NA;

        Carrier carrier = calledVar.getCarrier(parent.getId());

        int readsAlt = carrier != null ? carrier.getReadsAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getReadsRef() : Data.NA;

        if (readsAlt == Data.NA || readsRef == Data.NA) {
            parentBinomial = Data.NA;
        } else {
            parentBinomial = MathManager.getBinomialLessThan(readsAlt + readsRef, readsAlt, 0.5f, AlternativeHypothesis.LESS_THAN);
        }

        return ParentalCommand.isParentBinomialValid(parentBinomial);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        Carrier carrier = calledVar.getCarrier(child.getId());

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
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
        sb.append(isMinorRef).append(",");
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
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoverage(child.getIndex()))).append(",");

        sb.append(FormatManager.getDouble(carrier != null ? carrier.getGatkFilteredCoverage() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getReadsAlt() : Data.NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getReadsRef() : Data.NA)).append(",");
        sb.append(carrier != null ? carrier.getPercAltRead() : "NA").append(",");
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
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(calledVar.getStableId()).append(",");
        sb.append(calledVar.hasCCDS()).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getGnomADStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());
        sb.append(calledVar.getDenovoDB());

        return sb.toString();
    }
}
