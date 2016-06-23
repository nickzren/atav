package function.variant.base;

import function.genotype.base.CalledVariant;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import function.genotype.base.SampleManager;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Output implements Cloneable {

    protected CalledVariant calledVar;

    protected boolean isMinorRef = false; // reference allele is minor or major

    protected int[][] genoCount = new int[6][3];
    protected int[] minorHomCount = new int[2];
    protected int[] majorHomCount = new int[2];
    protected double[] hetFreq = new double[2];
    protected double[] minorAlleleFreq = new double[2];
    protected double[] minorHomFreq = new double[2];

    public Output(CalledVariant c) {
        calledVar = c;
    }

    public CalledVariant getCalledVariant() {
        return calledVar;
    }

    public void countSampleGeno() {
        int geno;

        for (Sample sample : SampleManager.getList()) {
            geno = calledVar.getGenotype(sample.getIndex());
            geno = getGenoType(geno, sample);

            addSampleGeno(geno, sample.getPheno());
        }
    }

    public void addSampleGeno(int geno, int pheno) {
        if (geno == Data.NA) {
            geno = Index.MISSING;
        }

        genoCount[geno][Index.ALL]++;
        genoCount[geno][pheno]++;
    }

    public void deleteSampleGeno(int geno, int pheno) {
        if (geno == Data.NA) {
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

        countMajorMinorHomHet();
    }

    private void calculateAlleleFreq() {
        int caseAC = 2 * genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE];
        int caseTotalAC = caseAC + genoCount[Index.HET][Index.CASE]
                + 2 * genoCount[Index.REF][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        double caseAF = MathManager.devide(caseAC, caseTotalAC); // (2*hom + het + homMale) / (2*hom + homMale + 2*het + 2*ref + refMale)

        minorAlleleFreq[Index.CASE] = caseAF;
        if (caseAF > 0.5) {
            minorAlleleFreq[Index.CASE] = 1.0 - caseAF;
        }

        int ctrlAC = 2 * genoCount[Index.HOM][Index.CTRL]
                + genoCount[Index.HET][Index.CTRL]
                + genoCount[Index.HOM_MALE][Index.CTRL];
        int ctrlTotalAC = ctrlAC + genoCount[Index.HET][Index.CTRL]
                + 2 * genoCount[Index.REF][Index.CTRL]
                + genoCount[Index.REF_MALE][Index.CTRL];

        double ctrlAF = MathManager.devide(ctrlAC, ctrlTotalAC);

        minorAlleleFreq[Index.CTRL] = ctrlAF;
        if (ctrlAF > 0.5) {
            isMinorRef = true;
            minorAlleleFreq[Index.CTRL] = 1.0 - ctrlAF;
        } else {
            isMinorRef = false;
        }
    }

    private void calculateGenotypeFreq() {
        double totalCaseGenotypeCount
                = genoCount[Index.HOM][Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + genoCount[Index.REF][Index.CASE]
                + genoCount[Index.HOM_MALE][Index.CASE]
                + genoCount[Index.REF_MALE][Index.CASE];

        double totalCtrlGenotypeCount
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

    public int getGenoType(int geno, Sample sample) {
        if (sample.isMale()
                && !calledVar.getRegion().isInsideAutosomalOrPseudoautosomalRegions()) {

            if (geno == Index.HOM) {
                return Index.HOM_MALE;
            } else if (geno == Index.REF) {
                return Index.REF_MALE;
            }
        }

        return geno;
    }

    public String getGenoStr(int geno) {
        switch (geno) {
            case 2:
                return "hom";
            case 1:
                return "het";
            case 0:
                return "hom ref";
            case Data.NA:
                return "NA";
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
        return minorHomCount[Index.CASE]
                + genoCount[Index.HET][Index.CASE]
                + minorHomCount[Index.CTRL]
                + genoCount[Index.HET][Index.CTRL];
    }

    private int getCaseCarrier() {
        return minorHomCount[Index.CASE]
                + genoCount[Index.HET][Index.CASE];
    }

    /*
     * if ref is minor then only het & ref are qualified samples. If ref is
     * major then only hom & het are qualified samples.
     */
    public boolean isQualifiedGeno(int geno) {
        if (GenotypeLevelFilterCommand.isAllGeno) {
            return true;
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

    public boolean isMinorRef() {
        return isMinorRef;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Output output = (Output) super.clone();

        output.genoCount = FormatManager.deepCopyIntArray(genoCount);
        output.minorHomCount = FormatManager.deepCopyIntArray(minorHomCount);
        output.majorHomCount = FormatManager.deepCopyIntArray(majorHomCount);
        output.hetFreq = FormatManager.deepCopyDoubleArray(hetFreq);
        output.minorAlleleFreq = FormatManager.deepCopyDoubleArray(minorAlleleFreq);
        output.minorHomFreq = FormatManager.deepCopyDoubleArray(minorHomFreq);

        return output;
    }
}
