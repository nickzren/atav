package function.genotype.family;

import function.external.bis.BisManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.genotype.base.SampleManager;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.mtr.MTRManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class FamilyOutput extends Output {

    private final String[] FLAG = {"Shared", "Different zygosity", "Possibly shared",
        "Not shared", "Nnknown", "Partially shared"};
    String familyId = "";
    String flag = "";
    public int[][] familyGenoCount = new int[6][3];
    public double[][] familySampleFreq = new double[4][3];

    public static String getTitle() {
        return "Family ID,"
                + "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + GerpManager.getTitle()
                + TrapManager.getTitle()
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
                + "Major Hom Ctrl,"
                + "Het Ctrl,"
                + "Minor Hom Ctrl,"
                + "Missing Ctrl,"
                + "Minor Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "Ctrl Maf,"
                + "Ctrl HWE_P,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Gene Name,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + GnomADManager.getExomeTitle()
                + GnomADManager.getGenomeTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + BisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle()
                + DenovoDBManager.getTitle()
                + DiscovEHRManager.getTitle()
                + MTRManager.getTitle();
    }

    public FamilyOutput(CalledVariant c) {
        super(c);
    }

    public String getFamilyId() {
        return familyId;
    }

    public int getHomFamily() {
        if (isMinorRef) {
            return familyGenoCount[Index.REF][Index.CASE]
                    + familyGenoCount[Index.REF_MALE][Index.CASE]
                    + familyGenoCount[Index.REF][Index.CTRL]
                    + familyGenoCount[Index.REF_MALE][Index.CTRL];
        } else {
            return familyGenoCount[Index.HOM][Index.CASE]
                    + familyGenoCount[Index.HOM_MALE][Index.CASE]
                    + familyGenoCount[Index.HOM][Index.CTRL]
                    + familyGenoCount[Index.HOM_MALE][Index.CTRL];
        }
    }

    public int getHetFamily() {
        return familyGenoCount[Index.HET][Index.CASE]
                + familyGenoCount[Index.HET][Index.CTRL];
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
        familyGenoCount = new int[6][3];
        familySampleFreq = new double[4][3];
    }

    public void calculateFamilyNum(String familyId) {
        int geno, pheno, type;

        for (Sample sample : SampleManager.getList()) {
            if (familyId.equals(sample.getFamilyId())) {
                geno = calledVar.getGenotype(sample.getIndex());
                pheno = (int) sample.getPheno();
                type = getGenoType(geno, sample);

                countFamilySample(type, pheno);
            }
        }
    }

    public void countFamilySample(int genotype, int pheno) {
        if (genotype == Data.NA) {
            genotype = Index.MISSING;
        }

        familyGenoCount[genotype][pheno]++;
    }

    public void calculateFamilyFreq() {
        int totalFamilyCase = familyGenoCount[Index.HOM][Index.CASE]
                + familyGenoCount[Index.HET][Index.CASE]
                + familyGenoCount[Index.REF][Index.CASE]
                + familyGenoCount[Index.HOM_MALE][Index.CASE]
                + familyGenoCount[Index.REF_MALE][Index.CASE];
        familySampleFreq[Index.HOM][Index.CASE] = MathManager.devide(familyGenoCount[Index.HOM][Index.CASE]
                + familyGenoCount[Index.HOM_MALE][Index.CASE], totalFamilyCase);
        familySampleFreq[Index.HET][Index.CASE] = MathManager.devide(familyGenoCount[Index.HET][Index.CASE], totalFamilyCase);

        int totalFamilyCtrl = familyGenoCount[Index.HOM][Index.CTRL]
                + familyGenoCount[Index.HET][Index.CTRL]
                + familyGenoCount[Index.REF][Index.CTRL]
                + familyGenoCount[Index.HOM_MALE][Index.CTRL]
                + familyGenoCount[Index.REF_MALE][Index.CTRL];
        familySampleFreq[Index.HOM][Index.CTRL] = MathManager.devide(familyGenoCount[Index.HOM][Index.CTRL]
                + familyGenoCount[Index.HOM_MALE][Index.CTRL], totalFamilyCtrl);
        familySampleFreq[Index.HET][Index.CTRL] = MathManager.devide(familyGenoCount[Index.HET][Index.CTRL], totalFamilyCtrl);
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
        int hom = familyGenoCount[Index.HOM][Index.CASE] + familyGenoCount[Index.HOM_MALE][Index.CASE];
        int het = familyGenoCount[Index.HET][Index.CASE];
        int ref = familyGenoCount[Index.REF][Index.CASE] + familyGenoCount[Index.REF_MALE][Index.CASE];
        int missing = familyGenoCount[Index.MISSING][Index.CASE];
        int totalFamilyCase = hom + het + ref + missing;

        if (isMinorRef) {
            hom = familyGenoCount[Index.REF][Index.CASE] + familyGenoCount[Index.REF_MALE][Index.CASE];
            ref = familyGenoCount[Index.HOM][Index.CASE] + familyGenoCount[Index.HOM_MALE][Index.CASE];
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
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
        sb.append(isMinorRef).append(",");
        sb.append(flag).append(",");
        sb.append(familyGenoCount[Index.HOM][Index.CASE]
                + familyGenoCount[Index.HOM_MALE][Index.CASE]).append(",");
        sb.append(familyGenoCount[Index.HET][Index.CASE]).append(",");
        sb.append(familyGenoCount[Index.REF][Index.CASE]
                + familyGenoCount[Index.REF_MALE][Index.CASE]).append(",");
        sb.append(familyGenoCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HOM][Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HET][Index.CASE])).append(",");
        sb.append(familyGenoCount[Index.HOM][Index.CTRL]
                + familyGenoCount[Index.HOM_MALE][Index.CTRL]).append(",");
        sb.append(familyGenoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(familyGenoCount[Index.REF][Index.CTRL]
                + familyGenoCount[Index.REF_MALE][Index.CTRL]).append(",");
        sb.append(familyGenoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HOM][Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(familySampleFreq[Index.HET][Index.CTRL])).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getGnomADExomeStr());
        sb.append(calledVar.getGnomADGenomeStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.getBis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());
        sb.append(calledVar.getDenovoDB());
        sb.append(calledVar.getDiscovEHR());
        sb.append(calledVar.getMTR());

        return sb.toString();
    }
}
