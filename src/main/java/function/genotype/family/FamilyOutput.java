package function.genotype.family;

import function.external.evs.EvsManager;
import function.genotype.base.SampleManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class FamilyOutput extends Output {

    private final String[] FLAG = {"Shared", "Different zygosity", "Possibly shared",
        "Not shared", "Nnknown", "Partially shared"};
    String familyId = "";
    String flag = "";
    public int[][] familySampleCount = new int[6][3];
    public double[][] familySampleFreq = new double[4][3];
    int[] familyTotalCov = new int[2];
    double[] familyAverageCov = new double[2];
    public static final String title
            = "Family ID,"
            + "Variant ID,"
            + "Variant Type,"
            + "Rs Number,"
            + "Ref Allele,"
            + "Alt Allele,"
            + "CADD Score Phred,"
            + GerpManager.getTitle()
            + "Is Minor Ref,"
            + "Flag,"
            + "Hom Family Case,"
            + "Het Family Case,"
            + "Ref Family Case,"
            + "Missing Family Case,"
            + "Hom Family Case Freq,"
            + "Het Family Case Freq,"
            + "Hom Family Ctrl,"
            + "Het Family Ctrl,"
            + "Ref Family Ctrl,"
            + "Missing Family Ctrl,"
            + "Hom Family Ctrl Freq,"
            + "Het Family Ctrl Freq,"
            + "Major Hom Pop Ctrl,"
            + "Het Pop Ctrl,"
            + "Minor Hom Pop Ctrl,"
            + "Missing Pop Ctrl,"
            + "Minor Hom Pop Ctrl Freq,"
            + "Het Pop Ctrl Freq,"
            + "Pop Ctrl Maf,"
            + "Pop Ctrl HWEp,"
            + "Avg Min Case Cov,"
            + "Avg Min Ctrl Cov,"
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
            + KnownVarManager.getTitle();

    public FamilyOutput(CalledVariant c) {
        super(c);
    }

    public String getFamilyId() {
        return familyId;
    }

    public int getHomFamily() {
        if (isMinorRef) {
            return familySampleCount[Index.REF][Index.CASE]
                    + familySampleCount[Index.REF_MALE][Index.CASE]
                    + familySampleCount[Index.REF][Index.CTRL]
                    + familySampleCount[Index.REF_MALE][Index.CTRL];
        } else {
            return familySampleCount[Index.HOM][Index.CASE]
                    + familySampleCount[Index.HOM_MALE][Index.CASE]
                    + familySampleCount[Index.HOM][Index.CTRL]
                    + familySampleCount[Index.HOM_MALE][Index.CTRL];
        }
    }

    public int getHetFamily() {
        return familySampleCount[Index.HET][Index.CASE]
                + familySampleCount[Index.HET][Index.CTRL];
    }

    public boolean isAllShared() {
        if (!flag.equals(FLAG[3])
                && !flag.equals(FLAG[4])) {
            return true;
        }

        return false;
    }

    public boolean isShared() {
        if (flag.equals(FLAG[0])) {
            return true;
        }

        return false;
    }

    public void resetFamilyData() {
        familySampleCount = new int[6][3];
        familySampleFreq = new double[4][3];
        familyTotalCov = new int[2];
        familyAverageCov = new double[2];
    }

    public void calculateFamilyNum(String familyId) {
        int cov, geno, pheno, type;

        for (Sample sample : SampleManager.getList()) {
            if (familyId.equals(sample.getFamilyId())) {
                cov = calledVar.getCoverage(sample.getIndex());
                geno = calledVar.getGenotype(sample.getIndex());
                pheno = (int) sample.getPheno();
                type = getGenoType(geno, sample);

                countFamilySample(type, pheno);
                countFamilyCoverage(cov, pheno);
            }
        }
    }

    public void countFamilySample(int genotype, int pheno) {
        if (genotype == Data.NA) {
            genotype = Index.MISSING;
        }

        familySampleCount[genotype][pheno]++;
    }

    private void countFamilyCoverage(int cov, int phone) {
        if (cov != Data.NA) {
            familyTotalCov[phone] += cov;
        }
    }

    public void calculateFamilyFreq() {
        int totalFamilyCase = familySampleCount[Index.HOM][Index.CASE]
                + familySampleCount[Index.HET][Index.CASE]
                + familySampleCount[Index.REF][Index.CASE]
                + familySampleCount[Index.HOM_MALE][Index.CASE]
                + familySampleCount[Index.REF_MALE][Index.CASE];
        familySampleFreq[Index.HOM][Index.CASE] = FormatManager.devide(familySampleCount[Index.HOM][Index.CASE]
                + familySampleCount[Index.HOM_MALE][Index.CASE], totalFamilyCase);
        familySampleFreq[Index.HET][Index.CASE] = FormatManager.devide(familySampleCount[Index.HET][Index.CASE], totalFamilyCase);

        int totalFamilyCtrl = familySampleCount[Index.HOM][Index.CTRL]
                + familySampleCount[Index.HET][Index.CTRL]
                + familySampleCount[Index.REF][Index.CTRL]
                + familySampleCount[Index.HOM_MALE][Index.CTRL]
                + familySampleCount[Index.REF_MALE][Index.CTRL];
        familySampleFreq[Index.HOM][Index.CTRL] = FormatManager.devide(familySampleCount[Index.HOM][Index.CTRL]
                + familySampleCount[Index.HOM_MALE][Index.CTRL], totalFamilyCtrl);
        familySampleFreq[Index.HET][Index.CTRL] = FormatManager.devide(familySampleCount[Index.HET][Index.CTRL], totalFamilyCtrl);

        familyAverageCov[Index.CASE] = FormatManager.devide(familyTotalCov[Index.CASE], totalFamilyCase);
        familyAverageCov[Index.CTRL] = FormatManager.devide(familyTotalCov[Index.CTRL], totalFamilyCtrl);
    }

    @Override
    public void countSampleGenoCov() {
        HashSet<String> familyIdSet = new HashSet<String>();

        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()) {
                addSampleGenoCov(sample);
            } else { // control                  
                boolean isFamilyValid = FamilyManager.isFamilyValid(sample.getFamilyId());

                boolean isFamilyContainedCase = false;
                if (isFamilyValid) {
                    isFamilyContainedCase = FamilyManager.isFamilyContainedCase(sample.getFamilyId());
                }

                if (!isFamilyValid || !isFamilyContainedCase) {
                    if (!familyIdSet.contains(sample.getFamilyId())) {
                        addSampleGenoCov(sample);

                        familyIdSet.add(sample.getFamilyId());
                    }
                }
            }
        }
    }

    private void addSampleGenoCov(Sample sample) {
        int cov = calledVar.getCoverage(sample.getIndex());
        int geno = calledVar.getGenotype(sample.getIndex());
        int pheno = (int) sample.getPheno();
        int type = getGenoType(geno, sample);

        addSampleGeno(type, pheno);
        addSampleCov(cov, pheno);
    }

    @Override
    public boolean isRecessive() {
        if (isMinorRef) {
            if (familySampleCount[Index.REF][Index.CASE]
                    + familySampleCount[Index.REF_MALE][Index.CASE] > 0
                    && familySampleCount[Index.HET][Index.CASE] == 0
                    && familySampleCount[Index.HOM][Index.CASE]
                    + familySampleCount[Index.HOM_MALE][Index.CASE] == 0) {
                return true;
            }
        } else {
            if (familySampleCount[Index.HOM][Index.CASE]
                    + familySampleCount[Index.HOM_MALE][Index.CASE] > 0
                    && familySampleCount[Index.HET][Index.CASE] == 0
                    && familySampleCount[Index.REF][Index.CASE]
                    + familySampleCount[Index.REF_MALE][Index.CASE] == 0) {
                return true;
            }
        }

        return false;
    }

    /*
     * Shared: if all of the cases are hom or all the cases are het. Different
     * zygosity:if all the cases are either hom or het (but not all hom and not
     * all het). Possibly shared: if all of the cases are either hom, het, or
     * missing Not shared: if only some cases are hom or het, and the others are
     * not missing. Variants where the ref is the minor allele and all cases are
     * either hom or missing, and also variants where the ref is the major
     * allele and the cases are all either ref or missing, shouldn't be output
     * at all.
     */
    public void initFlag() {
        int hom = familySampleCount[Index.HOM][Index.CASE] + familySampleCount[Index.HOM_MALE][Index.CASE];
        int het = familySampleCount[Index.HET][Index.CASE];
        int ref = familySampleCount[Index.REF][Index.CASE] + familySampleCount[Index.REF_MALE][Index.CASE];
        int missing = familySampleCount[Index.MISSING][Index.CASE];
        int totalFamilyCase = hom + het + ref + missing;

        if (isMinorRef) {
            hom = familySampleCount[Index.REF][Index.CASE] + familySampleCount[Index.REF_MALE][Index.CASE];
            ref = familySampleCount[Index.HOM][Index.CASE] + familySampleCount[Index.HOM_MALE][Index.CASE];
        }

        if (hom == totalFamilyCase || het == totalFamilyCase) {
            flag = FLAG[0]; // Shared
        } else if (hom < totalFamilyCase && het < totalFamilyCase && ref == 0 && missing == 0) {
            flag = FLAG[1]; // Different zygosity
        } else if ((het + hom) > 1 && (het + hom) < totalFamilyCase) {
            flag = FLAG[5]; // Partially shared
        } else if (missing > 0 && (het > 0 || hom > 0)) {
            flag = FLAG[2]; // Possibly shared
        } else if ((hom > 0 || het > 0) && ref > 0 && missing == 0) {
            flag = FLAG[3]; // Not shared
        } else if (ref == totalFamilyCase || missing == totalFamilyCase) {
            flag = FLAG[4]; // "Nnknown"
        } else {
            flag = FLAG[4]; // "Nnknown"
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(familyId).append(",");
        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(FormatManager.getFloat(calledVar.getGerpScore())).append(",");
        sb.append(isMinorRef).append(",");
        sb.append(flag).append(",");
        sb.append(familySampleCount[Index.HOM][Index.CASE]
                + familySampleCount[Index.HOM_MALE][Index.CASE]).append(",");
        sb.append(familySampleCount[Index.HET][Index.CASE]).append(",");
        sb.append(familySampleCount[Index.REF][Index.CASE]
                + familySampleCount[Index.REF_MALE][Index.CASE]).append(",");
        sb.append(familySampleCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HOM][Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HET][Index.CASE])).append(",");
        sb.append(familySampleCount[Index.HOM][Index.CTRL]
                + familySampleCount[Index.HOM_MALE][Index.CTRL]).append(",");
        sb.append(familySampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(familySampleCount[Index.REF][Index.CTRL]
                + familySampleCount[Index.REF_MALE][Index.CTRL]).append(",");
        sb.append(familySampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HOM][Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(majorHomCtrl).append(",");
        sb.append(sampleCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCtrl).append(",");
        sb.append(sampleCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(ctrlMhgf)).append(",");
        sb.append(FormatManager.getDouble(sampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(ctrlMaf)).append(",");
        sb.append(FormatManager.getDouble(ctrlHweP)).append(",");
        sb.append(FormatManager.getDouble(familyAverageCov[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(familyAverageCov[Index.CTRL])).append(",");

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

        return sb.toString();
    }
}
