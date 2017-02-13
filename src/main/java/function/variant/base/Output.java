package function.variant.base;

import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
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
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import function.genotype.base.SampleManager;
import function.genotype.statistics.HWEExact;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Output implements Cloneable {

    public static String getVariantDataTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "Rs Number,";
    }

    public static String getAnnotationDataTitle() {
        return "Transcript Stable Id,"
                + "Is CCDS Transcript,"
                + "Effect,"
                + "HGVS_c,"
                + "HGVS_p,"
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Gene Name,"
                + "All Effect Gene Transcript HGVS_p,";
    }

    public static String getExternalDataTitle() {
        return EvsManager.getTitle()
                + ExacManager.getTitle()
                + KnownVarManager.getTitle()
                + KaviarManager.getTitle()
                + GenomesManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GerpManager.getTitle()
                + TrapManager.getTitle()
                + MgiManager.getTitle();
    }

    public static String getGenoStatDataTitle() {
        return "Hom Case,"
                + "Het Case,"
                + "Hom Ref Case,"
                + "Hom Case Freq,"
                + "Het Case Freq,"
                + "Hom Ctrl,"
                + "Het Ctrl,"
                + "Hom Ref Ctrl,"
                + "Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "QC Fail Case,"
                + "QC Fail Ctrl,"
                + "Case Maf,"
                + "Ctrl Maf,"
                + "Case HWE_P,"
                + "Ctrl HWE_P,";
    }

    public static String getCarrierDataTitle() {
        return "Sample Name,"
                + "Sample Type,"
                + "GT,"
                + "DP,"
                + "DP Bin,"
                + "AD REF,"
                + "AD ALT,"
                + "Percent Alt Read,"
                + "Percent Alt Read Binomial P,"
                + "GQ,"
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,";
    }

    protected CalledVariant calledVar;
    
    protected int[][] genoCount = new int[3][2];
    protected float[] homFreq = new float[2];
    protected float[] hetFreq = new float[2];
    protected float[] minorAlleleFreq = new float[2];
    protected double[] hweP = new double[2];

    public Output(CalledVariant c) {
        calledVar = c;
    }

    public CalledVariant getCalledVariant() {
        return calledVar;
    }

    public void countSampleGeno() {
        for (Sample sample : SampleManager.getList()) {
            addSampleGeno(calledVar.getGT(sample.getIndex()), sample.getPheno());
        }
    }

    public void addSampleGeno(byte geno, int pheno) {
        if (geno != Data.BYTE_NA) {
            genoCount[geno][pheno]++;
        }
    }

    public void deleteSampleGeno(byte geno, int pheno) {
        if (geno != Data.INTEGER_NA) {
            genoCount[geno][pheno]--;
        }
    }

    public void calculate() {
        calculateAlleleFreq();

        calculateGenotypeFreq();

        calculateHweP();
    }

    private void calculateAlleleFreq() {
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE];

        float caseAF = MathManager.devide(caseAC, caseTotalAC); // (2*hom + het) / (2*hom + 2*het + 2*ref)

        minorAlleleFreq[Index.CASE] = caseAF;
        if (caseAF > 0.5) {
            minorAlleleFreq[Index.CASE] = 1.0f - caseAF;
        }

        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL];

        float ctrlAF = MathManager.devide(ctrlAC, ctrlTotalAC);

        minorAlleleFreq[Index.CTRL] = ctrlAF;
        if (ctrlAF > 0.5) {
            minorAlleleFreq[Index.CTRL] = 1.0f - ctrlAF;
        } 
    }

    private void calculateGenotypeFreq() {
        int totalCaseGenotypeCount
                = genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE];

        int totalCtrlGenotypeCount
                = genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.REF][Index.CTRL];

        // hom / (hom + het + ref)
        homFreq[Index.CASE] = MathManager.devide(genoCount[Index.HOM][Index.CASE], totalCaseGenotypeCount);
        homFreq[Index.CTRL] = MathManager.devide(genoCount[Index.HOM][Index.CTRL], totalCtrlGenotypeCount);

        hetFreq[Index.CASE] = MathManager.devide(genoCount[Index.HET][Index.CASE], totalCaseGenotypeCount);
        hetFreq[Index.CTRL] = MathManager.devide(genoCount[Index.HET][Index.CTRL], totalCtrlGenotypeCount);
    }

    public void calculateHweP() {
        hweP[Index.CASE] = HWEExact.getP(genoCount[Index.HOM][Index.CASE],
                genoCount[Index.HET][Index.CASE],
                genoCount[Index.REF][Index.CASE]);

        hweP[Index.CTRL] = HWEExact.getP(genoCount[Index.HOM][Index.CTRL],
                genoCount[Index.HET][Index.CTRL],
                genoCount[Index.REF][Index.CTRL]);
    }

    public String getGenoStr(byte geno) {
        switch (geno) {
            case Index.HOM:
                return "hom";
            case Index.HET:
                return "het";
            case Index.REF:
                return "hom ref";
            case Data.BYTE_NA:
                return Data.STRING_NA;
        }

        return "";
    }

    public boolean isValid() {
        return GenotypeLevelFilterCommand.isMinVarPresentValid(getVarPresent())
                && GenotypeLevelFilterCommand.isMinCaseCarrierValid(getCaseCarrier())
                && GenotypeLevelFilterCommand.isMaxCtrlMafValid(minorAlleleFreq[Index.CTRL])
                && GenotypeLevelFilterCommand.isMinCtrlMafValid(minorAlleleFreq[Index.CTRL]);
    }

    private int getVarPresent() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
    }

    private int getCaseCarrier() {
        return genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE];
    }

    public boolean isQualifiedGeno(byte geno) {
        return geno == Index.HOM || geno == Index.HET;
    }

    public void getGenoStatData(StringBuilder sb) {
        sb.append(genoCount[Index.HOM][Index.CASE]).append(",");
        sb.append(genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(genoCount[Index.REF][Index.CASE]).append(",");
        sb.append(FormatManager.getFloat(homFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(hetFreq[Index.CASE])).append(",");
        sb.append(genoCount[Index.HOM][Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(genoCount[Index.REF][Index.CTRL]).append(",");
        sb.append(FormatManager.getFloat(homFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getFloat(hetFreq[Index.CTRL])).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getFloat(minorAlleleFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
    }

    public void getCarrierData(StringBuilder sb, Carrier carrier, Sample sample) {
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(calledVar.getDPBin(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADRef() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADAlt() : Data.SHORT_NA)).append(",");
        sb.append(carrier != null ? carrier.getPercAltRead() : Data.STRING_NA).append(",");
        sb.append(carrier != null ? FormatManager.getDouble(carrier.getPercentAltReadBinomialP()) : Data.STRING_NA).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getMQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getQD() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : Data.STRING_NA).append(",");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Output output = (Output) super.clone();

        output.genoCount = FormatManager.deepCopyArray(genoCount);
        output.homFreq = FormatManager.deepCopyArray(homFreq);
        output.hetFreq = FormatManager.deepCopyArray(hetFreq);
        output.minorAlleleFreq = FormatManager.deepCopyArray(minorAlleleFreq);
        output.hweP = FormatManager.deepCopyArray(hweP);

        return output;
    }
}
