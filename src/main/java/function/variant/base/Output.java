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

    public static String getGenotypeDataTitle() {
        return "Is Minor Ref,"
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
                + "Ctrl HWE_P,";
    }

    protected CalledVariant calledVar;

    protected boolean isMinorRef = false; // reference allele is minor or major

    protected int[][] genoCount = new int[6][3];
    protected int[] minorHomCount = new int[2];
    protected int[] majorHomCount = new int[2];
    protected float[] hetFreq = new float[2];
    protected float[] minorAlleleFreq = new float[2];
    protected float[] minorHomFreq = new float[2];
    protected double[] hweP = new double[2];

    public Output(CalledVariant c) {
        calledVar = c;
    }

    public CalledVariant getCalledVariant() {
        return calledVar;
    }

    public void countSampleGeno() {
        byte geno;

        for (Sample sample : SampleManager.getList()) {
            geno = calledVar.getGT(sample.getIndex());
            geno = getGenoType(geno, sample);

            addSampleGeno(geno, sample.getPheno());
        }
    }

    public void addSampleGeno(byte geno, int pheno) {
        if (geno == Data.BYTE_NA) {
            geno = Index.MISSING;
        }

        genoCount[geno][Index.ALL]++;
        genoCount[geno][pheno]++;
    }

    public void deleteSampleGeno(byte geno, int pheno) {
        if (geno == Data.INTEGER_NA) {
            geno = Index.MISSING;
        }

        genoCount[geno][Index.ALL]--;
        genoCount[geno][pheno]--;
    }

    public void countMissingSamples() {
        genoCount[Index.MISSING][Index.CASE] = SampleManager.getCaseNum();
        genoCount[Index.MISSING][Index.CTRL] = SampleManager.getCtrlNum();

        for (int i = 0; i < genoCount.length - 1; i++) {
            genoCount[Index.MISSING][Index.CASE] -= genoCount[i][Index.CASE];
            genoCount[Index.MISSING][Index.CTRL] -= genoCount[i][Index.CTRL];
        }

        genoCount[Index.MISSING][Index.ALL] = genoCount[Index.MISSING][Index.CTRL]
                + genoCount[Index.MISSING][Index.CASE];
    }

    public void calculate() {
        calculateAlleleFreq();

        calculateGenotypeFreq();

        calculateHweP();

        countMajorMinorHomHet();
    }

    private void calculateAlleleFreq() {
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        float caseAF = MathManager.devide(caseAC, caseTotalAC); // (2*hom + het + homMale) / (2*hom + homMale + 2*het + 2*ref + refMale)

        minorAlleleFreq[Index.CASE] = caseAF;
        if (caseAF > 0.5) {
            minorAlleleFreq[Index.CASE] = 1.0f - caseAF;
        }

        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.REF_MALE][Index.CTRL];

        float ctrlAF = MathManager.devide(ctrlAC, ctrlTotalAC);

        minorAlleleFreq[Index.CTRL] = ctrlAF;
        if (ctrlAF > 0.5) {
            isMinorRef = true;
            minorAlleleFreq[Index.CTRL] = 1.0f - ctrlAF;
        } else {
            isMinorRef = false;
        }
    }

    private void calculateGenotypeFreq() {
        int totalCaseGenotypeCount
                = genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        int totalCtrlGenotypeCount
                = genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL]
                + genoCount[Index.REF_MALE][Index.CTRL];

        // hom / (hom + het + ref)
        if (isMinorRef) {
            minorHomFreq[Index.CASE] = MathManager.devide(genoCount[Index.REF][Index.CASE]
                    + genoCount[Index.REF_MALE][Index.CASE], totalCaseGenotypeCount);

            minorHomFreq[Index.CTRL] = MathManager.devide(genoCount[Index.REF][Index.CTRL]
                    + genoCount[Index.REF_MALE][Index.CTRL], totalCtrlGenotypeCount);
        } else {
            minorHomFreq[Index.CASE] = MathManager.devide(genoCount[Index.HOM][Index.CASE]
                    + genoCount[Index.HOM_MALE][Index.CASE], totalCaseGenotypeCount);

            minorHomFreq[Index.CTRL] = MathManager.devide(genoCount[Index.HOM][Index.CTRL]
                    + genoCount[Index.HOM_MALE][Index.CTRL], totalCtrlGenotypeCount);
        }

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

    public void countMajorMinorHomHet() {
        int caseRef = genoCount[Index.REF][Index.CASE] + genoCount[Index.REF_MALE][Index.CASE];
        int caseHom = genoCount[Index.HOM][Index.CASE] + genoCount[Index.HOM_MALE][Index.CASE];
        int ctrlRef = genoCount[Index.REF][Index.CTRL] + genoCount[Index.REF_MALE][Index.CTRL];
        int ctrlHom = genoCount[Index.HOM][Index.CTRL] + genoCount[Index.HOM_MALE][Index.CTRL];

        if (isMinorRef) {
            minorHomCount[Index.CASE] = caseRef;
            minorHomCount[Index.CTRL] = ctrlRef;
            majorHomCount[Index.CASE] = caseHom;
            majorHomCount[Index.CTRL] = ctrlHom;
        } else {
            minorHomCount[Index.CASE] = caseHom;
            minorHomCount[Index.CTRL] = ctrlHom;
            majorHomCount[Index.CASE] = caseRef;
            majorHomCount[Index.CTRL] = ctrlRef;
        }
    }

    public byte getGenoType(byte geno, Sample sample) {
        if (sample.isMale()
                && !calledVar.isInsideAutosomalOrPseudoautosomalRegions()) {

            if (geno == Index.HOM) {
                return Index.HOM_MALE;
            } else if (geno == Index.REF) {
                return Index.REF_MALE;
            }
        }

        return geno;
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
        if (GenotypeLevelFilterCommand.isAllNonRef && isMinorRef) {
            return majorHomCount[Index.CASE]
                    + genoCount[Index.HET][Index.CASE]
                    + majorHomCount[Index.CTRL]
                    + genoCount[Index.HET][Index.CTRL];
        }

        return minorHomCount[Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + minorHomCount[Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
    }

    private int getCaseCarrier() {
        if (GenotypeLevelFilterCommand.isAllNonRef && isMinorRef) {
            return majorHomCount[Index.CASE]
                    + genoCount[Index.HET][Index.CASE];
        }

        return minorHomCount[Index.CASE]
                + genoCount[Index.HET][Index.CASE];
    }

    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    public boolean isQualifiedGeno(byte geno) {
        if (GenotypeLevelFilterCommand.isAllGeno) {
            return true;
        }

        if (GenotypeLevelFilterCommand.isAllNonRef) {
            if (geno == Index.HOM || geno == Index.HET) {
                return true;
            }
        }

        if (isMinorRef) {
            if (geno == Index.REF || geno == Index.HET) {
                return true;
            }
        } else if (geno == Index.HOM || geno == Index.HET) {
            return true;
        }

        return false;
    }

    public boolean isMinorRef() {
        return isMinorRef;
    }

    public void getGenotypeData(StringBuilder sb) {
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCount[Index.CASE]).append(",");
        sb.append(genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCount[Index.CASE]).append(",");
        sb.append(FormatManager.getFloat(minorHomFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(hetFreq[Index.CASE])).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(FormatManager.getFloat(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getFloat(hetFreq[Index.CTRL])).append(",");
        sb.append(genoCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getFloat(minorAlleleFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Output output = (Output) super.clone();

        output.genoCount = FormatManager.deepCopyArray(genoCount);
        output.minorHomCount = FormatManager.deepCopyArray(minorHomCount);
        output.majorHomCount = FormatManager.deepCopyArray(majorHomCount);
        output.hetFreq = FormatManager.deepCopyArray(hetFreq);
        output.minorAlleleFreq = FormatManager.deepCopyArray(minorAlleleFreq);
        output.minorHomFreq = FormatManager.deepCopyArray(minorHomFreq);
        output.hweP = FormatManager.deepCopyArray(hweP);

        return output;
    }
}
