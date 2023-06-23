package function.cohort.family;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.variant.base.Output;
import function.cohort.base.Sample;
import global.Data;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author jaimee
 */
public class FamilyOutput extends Output {
    Sample caseSample;
    Carrier cCarrier;
    private String inheritanceModel;
    
    // ACMG
    private boolean isACMGPLP = false;
    private String acmgClassification;
    private String acmgPathogenicCriteria;
    private String acmgBenignCriteria;
    private int acmgPSCount;
    private int acmgPMCount;
    private int acmgPPCount;
    private int acmgBSCount;
    private int acmgBPCount;
    boolean isPM3 = false;
    boolean isBP2 = false;
    
    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("Family ID");
        sj.add("Model - TBD");
        sj.add("ATAV ACMG Classification");
        sj.add("ATAV ACMG Pathogenic Criteria");
        sj.add("ATAV ACMG Benign Criteria");
        sj.merge(getVariantDataHeader());
        sj.merge(getAnnotationDataHeader());
        sj.merge(getCarrierDataHeader());
        sj.merge(getCohortLevelHeader());
        sj.merge(getExternalDataHeader());

        return sj.toString();
    }

    public FamilyOutput(CalledVariant c) {
        super(c);
    }

    public void initCarrierData(Sample sample){
        cCarrier = calledVar.getCarrier(sample.getId());
    }
    
    public String getString(Sample sample) {
        StringJoiner sj = new StringJoiner(",");
        sj.add(sample.getFamilyId());
        sj.add(FormatManager.getString(inheritanceModel));
        sj.add(acmgClassification);
        sj.add(acmgPathogenicCriteria);
        sj.add(acmgBenignCriteria);
        calledVar.getVariantData(sj);
        calledVar.getAnnotationData(sj);
        getCarrierData(sj, calledVar.getCarrier(sample.getId()), sample);
        getGenoStatData(sj);
        calledVar.getExternalData(sj);

        return sj.toString();
    }

    /*
        dominate model: all cases are HET calls, all controls are HOM REF
        recessive model: all cases are HOM calls, controls are either HET or REF
     */
    public void calculateInheritanceModel(Family family) {
        boolean case_dom = true; // All cases are HET
        boolean case_rec = true; // All cases are HOM
        for (Sample sample : family.getCaseList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());

            if (geno != Index.HET) {
                case_dom = false;
            }

            if (geno != Index.HOM) {
                case_rec = false;
            }
        }

        boolean control_dom = true; // All controls are REF
        boolean control_rec = true; // All controls are either REF or HET
        for (Sample sample : family.getControlList()) {
            byte geno = getCalledVariant().getGT(sample.getIndex());

            if (geno != Index.REF) {
                control_dom = false;
            }

            if (geno != Index.REF && geno != Index.HET) {
                control_rec = false;
            }
        }

        inheritanceModel = Data.STRING_NA;
        if (control_dom && case_dom) {
            inheritanceModel = "DOMINANT";
        } else if (control_rec && case_rec) {
            inheritanceModel = "RECESSIVE";
        }
    }

    public String getInheritanceModel() {
        return inheritanceModel;
    }
    
    public void initACMGClassification() {
        boolean isPathogenic = false;
        boolean isLikelyPathogenic = false;
        boolean isBenign = false;
        boolean isLikeBenign = false;

        boolean isPVS1 = calledVar.isPVS1(cCarrier);

        if (isPVS1
                && (acmgPSCount >= 1
                || acmgPMCount >= 2
                || (acmgPMCount == 1 && acmgPPCount == 1)
                || acmgPPCount >= 2)) {
            isPathogenic = true;
        }

        if (acmgPSCount >= 2) {
            isPathogenic = true;
        }

        if (acmgPSCount == 1
                && (acmgPMCount >= 3
                || (acmgPMCount == 2 && acmgPPCount >= 2)
                || (acmgPMCount == 1 && acmgPPCount >= 4))) {
            isPathogenic = true;
        }

        if ((isPVS1 && acmgPMCount == 1)
                || (acmgPSCount == 1 && acmgPMCount >= 1)
                || (acmgPSCount == 1 && acmgPPCount >= 2)
                || acmgPMCount >= 3
                || (acmgPMCount == 2 && acmgPPCount >= 2)
                || (acmgPMCount == 1 && acmgPPCount >= 4)) {
            isLikelyPathogenic = true;
        }

        if (calledVar.isBA1() || acmgBSCount >= 2) {
            isBenign = true;
        }

        if ((acmgBSCount == 1 && acmgBPCount == 1)
                || acmgBPCount >= 2) {
            isLikeBenign = true;
        }

        if ((!isPathogenic && !isLikelyPathogenic && !isBenign && !isLikeBenign) // Other criteria shown above are not met
                || ((isPathogenic || isLikelyPathogenic)) && (isBenign || isLikeBenign) // the criteria for benign and pathogenic are contradictory
                ) {
            acmgClassification = "Uncertain significance";
        }

        if (isPathogenic) {
            acmgClassification = "Pathogenic";
            isACMGPLP = true;
        } else if (isLikelyPathogenic) {
            acmgClassification = "Likely pathogenic";
            isACMGPLP = true;
        } else if (isBenign) {
            acmgClassification = "Benign";
        } else if (isLikeBenign) {
            acmgClassification = "Like benign";
        } else {
            acmgClassification = "Uncertain significance";
        }
    }

    public void initACMG() {
        acmgPSCount = 0;
        acmgPMCount = 0;
        acmgPPCount = 0;
        acmgBSCount = 0;
        acmgBPCount = 0;

        initACMGPathogenicCriteria();
        initACMGBenignCriteria();
        initACMGClassification();
    }

    private void initACMGPathogenicCriteria() {
        StringJoiner sj = new StringJoiner("|");

        if (calledVar.isPVS1(cCarrier)) {
            sj.add("PVS1");
        }

        // PS1 not clear
        // Same amino acid change as a previously established pathogenic variant regardless of nucleotide change
        //
        // PS2 not support
        // Denovo (TRIO)
        //
        // PS3 not clear
        // Well-established in vitro or in vivo functional studies supportive of a damaging effect on the gene or gene product
        //
        // PS4 not clear
        // The prevalence of the variant in affected individuals is significantly increased compared to the prevalence in controls
        //
        if (calledVar.isPM1()) {
            sj.add("PM1");
            acmgPMCount++;
        }

        if (calledVar.isPM2(cCarrier)) {
            sj.add("PM2");
            acmgPMCount++;
        }

        if (calledVar.isPM4()) {
            sj.add("PM4");
            acmgPMCount++;
        }

        if (calledVar.isPM5()) {
            sj.add("PM5");
            acmgPMCount++;
        }

        // PM6 not support
        // Denovo (DUO)
        //
        // PP1 not clear
        // Co-segregation with disease in multiple affected family members in a gene definitively known to cause the disease
        //
        if (calledVar.isPP2()) {
            sj.add("PP2");
            acmgPPCount++;
        }

        if (calledVar.isPP3()) {
            sj.add("PP3");
            acmgPPCount++;
        }

        // PP4 not clear
        // Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology
        if (calledVar.isPP5()) {
            sj.add("PP5");
            acmgPPCount++;
        }

        acmgPathogenicCriteria = sj.toString();

        if (acmgPathogenicCriteria.isEmpty()) {
            acmgPathogenicCriteria = Data.STRING_NA;
        }
    }

    private void initACMGBenignCriteria() {
        StringJoiner sj = new StringJoiner("|");

        if (calledVar.isBA1()) {
            sj.add("BA1");
        }

        if (calledVar.isBS1()) {
            sj.add("BS1");
            acmgBSCount++;
        }

        if (calledVar.isBS2()) {
            sj.add("BS2");
            acmgBSCount++;
        }

        // BS3 not clear
        // Well-established in vitro or in vivo functional studies shows no damaging effect on protein function or splicing
        //
        // BS4 not clear
        // Lack of segregation in affected members of a family
        //
        if (calledVar.isBP1()) {
            sj.add("BP1");
            acmgBPCount++;
        }

        // BP2 not clear
        // Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder; or observed in cis with a pathogenic variant in any inheritance pattern
        //
        if (calledVar.isBP3()) {
            sj.add("BP3");
            acmgBPCount++;
        }

        if (calledVar.isBP4()) {
            sj.add("BP4");
            acmgBPCount++;
        }

        // BP5 not clear
        // Variant found in a case with an alternate molecular basis for disease
        //
        if (calledVar.isBP6()) {
            sj.add("BP6");
            acmgBPCount++;
        }

        if (calledVar.isBP7()) {
            sj.add("BP7");
            acmgBPCount++;
        }

        acmgBenignCriteria = sj.toString();

        if (acmgBenignCriteria.isEmpty()) {
            acmgBenignCriteria = Data.STRING_NA;
        }
    }
}
