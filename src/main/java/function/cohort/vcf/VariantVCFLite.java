package function.cohort.vcf;

import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.SampleManager;
import function.external.exac.ExAC;
import function.external.exac.ExACCommand;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGenome;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarOutput;
import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBROutput;
import function.external.mtr.MTR;
import function.external.mtr.MTRCommand;
import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.variant.base.Variant;
import function.variant.base.VariantLevelFilterCommand;
import function.variant.base.VariantManager;
import global.Data;
import global.Index;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class VariantVCFLite extends Variant {

    private String rsNumber;
    //Indel attributes
    private int indelLength;

    // annotations
    private StringJoiner allAnnotationSJ = new StringJoiner(",");
    private List<String> geneList = new ArrayList();
    private HashSet<Integer> transcriptSet = new HashSet<>();
    private Annotation mostDamagingAnnotation = new Annotation();

    // cohort level data
    public int[] genoCount = new int[3];
    private float af;
    // The value will be dynamically updated per sample
    private float looAF = 0;

    private byte[] gtArr = new byte[SampleManager.getTotalSampleNum()];
    private short[] dpArr = new short[SampleManager.getTotalSampleNum()];
    private byte[] gqArr = new byte[SampleManager.getTotalSampleNum()];
    private List<Byte> carrierGTList = new ArrayList<>();

    // external
    public float revel = Data.FLOAT_NA;
    public float primateAI = Data.FLOAT_NA;

    public KnownVarOutput knownVarOutput;

    private boolean isValid = true;

    public VariantVCFLite(String values[]) throws Exception {
        rsNumber = values[2];
        String v_id_str = values[0] + "-" + values[1] + "-" + values[2] + "-" + values[3];
        
        initByVariantIDStr(v_id_str);
        
        indelLength = allele.length() - refAllele.length();

        initAllAnnotation(values[7]); // info: NS=X;AF=X;ANN=X

        if (mostDamagingAnnotation.isValid()) {
            initAllGenotype(values);

            initAF();
        }
    }

    private void initAllAnnotation(String info) {
        String allAnnotation = info.substring(info.indexOf("ANN=") + 4, info.length());

        // isValid to false means no annotations passed the filters
        mostDamagingAnnotation.setValid(false);

        if (KnownVarCommand.isInclude) {
            initKnownVar();
        }

        // init revel and primateai once and potentially used for ensemble filters
        initRevel();
        initPrimateAI();

        for (String annotationStr : allAnnotation.split(",")) {
            String[] values = annotationStr.split("\\|");
            String effect = values[0];
            String geneName = values[1];

            String stableIdStr = values[2];
            int stableId = TranscriptManager.getIntStableId(stableIdStr);
            String HGVS_c = values[3];
            String HGVS_p = values[4];
            float polyphenHumdiv = FormatManager.getFloat(values[5]);
            float polyphenHumvar = FormatManager.getFloat(values[6]);

            // init annotation in order to apply --ensemble-missense filter
            Annotation annotation = new Annotation();
            annotation.effect = effect;
            annotation.polyphenHumdiv = polyphenHumdiv;
            annotation.revel = revel;
            annotation.primateAI = primateAI;

            // --effect
            // --gene or --gene-boundary
            // --polyphen-humdiv
            // --ensemble-missense
            // --ensemble-missense-2
            // --ccds-only
            // --canonical-only
            if (EffectManager.isEffectContained(effect)
                    && GeneManager.isValid(geneName, chrStr, startPosition, indelLength)
                    && PolyphenManager.isValid(polyphenHumdiv, polyphenHumvar, effect)
                    && annotation.isEnsembleMissenseValid()
                    && TranscriptManager.isCCDSValid(chrStr, stableId)
                    && TranscriptManager.isCanonicalValid(chrStr, stableId)
                    && TranscriptManager.isTranscriptBoundaryValid(stableId, startPosition, indelLength)) {

                // reset gene name to gene domain name so the downstream procedure could match correctly
                // only for gene boundary input
                geneName = GeneManager.getGeneDomainName(geneName, chrStr, startPosition);

                StringJoiner geneTranscriptSJ = new StringJoiner("|");
                geneTranscriptSJ.add(effect);
                geneTranscriptSJ.add(geneName);
                geneTranscriptSJ.add(stableIdStr);
                geneTranscriptSJ.add(HGVS_c);
                geneTranscriptSJ.add(HGVS_p);
                geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumdiv));
                geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumvar));

                allAnnotationSJ.add(geneTranscriptSJ.toString());
                if (!geneList.contains(geneName) && !geneName.equals(Data.STRING_NA)) {
                    geneList.add(geneName);
                }
                if (stableId != Data.INTEGER_NA) {
                    transcriptSet.add(stableId);
                }

                // init most damging annotation
                if (mostDamagingAnnotation.effect == null) {
                    mostDamagingAnnotation.effect = effect;
                    mostDamagingAnnotation.stableId = stableId;
                    mostDamagingAnnotation.HGVS_c = HGVS_c;
                    mostDamagingAnnotation.HGVS_p = HGVS_p;
                    mostDamagingAnnotation.geneName = geneName;
                    int effectId = EffectManager.getIdByEffect(effect);

                    byte ttnPSI = GeneManager.getTTNLowPSI(geneName, effectId, startPosition);
                    if (GeneManager.isTTNPSIValid(ttnPSI)) {
                        mostDamagingAnnotation.setValid(true);
                    }
                }

                mostDamagingAnnotation.polyphenHumdiv = MathManager.max(mostDamagingAnnotation.polyphenHumdiv, polyphenHumdiv);
                mostDamagingAnnotation.polyphenHumvar = MathManager.max(mostDamagingAnnotation.polyphenHumvar, polyphenHumvar);

                boolean isCCDS = TranscriptManager.isCCDSTranscript(chrStr, stableId);

                if (isCCDS) {
                    mostDamagingAnnotation.polyphenHumdivCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumdivCCDS, polyphenHumdiv);
                    mostDamagingAnnotation.polyphenHumvarCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumvarCCDS, polyphenHumvar);

                    mostDamagingAnnotation.hasCCDS = true;
                }
            }
        }
    }

    private void initAllGenotype(String values[]) {
        int sampleIndex;

        for (int vcfColumnIndex = 9; vcfColumnIndex < values.length; vcfColumnIndex++) {
            String[] tmp = values[vcfColumnIndex].split(":"); // GT:DP:GQ

            byte gt = VCFManager.getGT(tmp[0]);
            short dp = FormatManager.getShort(tmp[1]);
            byte gq = FormatManager.getByte(tmp[2]);

            if (gt != Data.BYTE_NA) {
//                // apply --min-coverage or --min-gq filter
//                if (!GenotypeLevelFilterCommand.isMinGqValid(gq)
//                        || !GenotypeLevelFilterCommand.isMinDpBinValid(dp)) {
//                    gt = Data.BYTE_NA;
//                } else {
                genoCount[gt]++;
//                }
            }

            sampleIndex = vcfColumnIndex - 9;
            gtArr[sampleIndex] = gt;
            dpArr[sampleIndex] = dp;
            gqArr[sampleIndex] = gq;

            if (GenotypeLevelFilterCommand.isQualifiedGeno(gt)) {
                carrierGTList.add(gt);
            }
        }

        for (byte gt : carrierGTList) {
            calculateLooAF(gt);

            // apply --max-loo-af and --max-loo-maf
            if (!CohortLevelFilterCommand.isLooAFValid(looAF)) {
                isValid = false;
                break;
            }
        }

        carrierGTList = null;
    }

    private void calculateLooAF(byte gt) {
        // delete current sample geno as 'leave one out' concept
        deleteSampleGeno(gt);

        int ac = 2 * genoCount[Index.HOM] + genoCount[Index.HET];
        int totalAC = ac + genoCount[Index.HET] + 2 * genoCount[Index.REF];

        looAF = MathManager.devide(ac, totalAC);
        // add deleted sample geno back
        addSampleGeno(gt);
    }

    private void addSampleGeno(byte gt) {
        if (gt != Data.BYTE_NA) {
            genoCount[gt]++;
        }
    }

    private void deleteSampleGeno(byte gt) {
        if (gt != Data.BYTE_NA) {
            genoCount[gt]--;
        }
    }

    // NS = Number of Samples With Data
    public int getNS() {
        return genoCount[Index.HOM]
                + genoCount[Index.HET]
                + genoCount[Index.REF];
    }

    // AF = Allele Frequency
    private void initAF() {
        int ac = 2 * genoCount[Index.HOM] + genoCount[Index.HET];
        int totalAC = ac + genoCount[Index.HET] + 2 * genoCount[Index.REF];

        af = MathManager.devide(ac, totalAC);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\t");

        sj.add(chrStr);
        sj.add(FormatManager.getInteger(startPosition));
        sj.add(rsNumber);
        sj.add(refAllele);
        sj.add(allele);
        sj.add(Data.VCF_NA);
        sj.add(Data.VCF_NA);

        StringJoiner infoSJ = new StringJoiner(";");
        infoSJ.add("NS=" + FormatManager.getInteger(getNS()));
        infoSJ.add("AF=" + FormatManager.getFloat(af));
        infoSJ.add("ANN=" + allAnnotationSJ.toString());
        sj.add(infoSJ.toString());

        StringJoiner formatSJ = new StringJoiner(":");
        formatSJ.add("GT");
        formatSJ.add("DP");
        formatSJ.add("GQ");
        sj.add(formatSJ.toString());

        for (int i = 0; i < SampleManager.getTotalSampleNum(); i++) {
            formatSJ = new StringJoiner(":");
            formatSJ.add(VCFManager.getGT(getGT(i)));
            formatSJ.add(FormatManager.getShort(dpArr[i]));
            formatSJ.add(FormatManager.getByte(gqArr[i]));
            sj.add(formatSJ.toString());
        }

        return sj.toString();
    }

    public byte getGT(int index) {
        if (index == Data.INTEGER_NA) {
            return Data.BYTE_NA;
        }

        return gtArr[index];
    }

    /*
        support filters:
        --exclude-multiallelic-variant
        --exclude-multiallelic-variant-2
    
        --variant
        --exclude-variant
        
        --max-af
        --min-af
        --max-maf
        --min-maf
    
        --max-limbr-domain-percentile
        --max-limbr-exon-percentile
        all ExAC filters
        all gnomAD Exome filters
        all gnomAD Genome filters
        --max-mtr
        --max-mtr-fdr
        --max-mtr-centile
        --min-revel-score
        --min-primate-ai
     */
    public boolean isValid() throws SQLException {
        if (VariantLevelFilterCommand.isExcludeMultiallelicVariant
                && VariantManager.isMultiallelicVariant(chrStr, startPosition)) {
            // exclude Multiallelic site > 1 variant
            return false;
        } else if (VariantLevelFilterCommand.isExcludeMultiallelicVariant2
                && VariantManager.isMultiallelicVariant2(chrStr, startPosition)) {
            // exclude Multiallelic site > 2 variants
            return false;
        }

        return isValid
                && mostDamagingAnnotation.isValid()
                && VariantManager.isVariantIdIncluded(variantIdStr)
                && !VariantManager.isVariantIdExcluded(variantIdStr)
                && !geneList.isEmpty()
                && !transcriptSet.isEmpty()
                && CohortLevelFilterCommand.isAFValid(af)
                && isLIMBRValid()
                && isExACValid()
                && isGnomADExomeValid()
                && isGnomADGenomeValid()
                && isMTRValid()
                && isRevelValid()
                && isPrimateAIValid();
    }

    // public String getVariantID() {
    //   return variantIdStr;
    // }
    public Annotation getMostDamagingAnnotation() {
        return mostDamagingAnnotation;
    }

    public String getAllAnnotation() {
        return FormatManager.appendDoubleQuote(allAnnotationSJ.toString());
    }

    public List<String> getGeneList() {
        return geneList;
    }

    public HashSet<Integer> getTranscriptSet() {
        return transcriptSet;
    }

    public byte[] getGTArr() {
        return gtArr;
    }

    // init LIMBR score base on most damaging gene and applied filter
    private boolean isLIMBRValid() {
        if (LIMBRCommand.isInclude) {
            LIMBROutput limbrOutput = new LIMBROutput(mostDamagingAnnotation.geneName, chrStr, startPosition);

            // LIMBR filters will only apply missense variants except gene boundary option at domain level used
            if (mostDamagingAnnotation.effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
                return limbrOutput.isValid();
            } else {
                return true;
            }
        }

        return true;
    }

    private boolean isExACValid() {
        if (ExACCommand.getInstance().isInclude) {
            ExAC exac = new ExAC(chrStr, startPosition, refAllele, allele);

            return exac.isValid(knownVarOutput.isKnownVariant());
        }

        return true;
    }

    private boolean isGnomADExomeValid() {
        if (GnomADExomeCommand.getInstance().isInclude) {
            GnomADExome gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            return gnomADExome.isValid(knownVarOutput.isKnownVariant());
        }

        return true;
    }

    private boolean isGnomADGenomeValid() {
        if (GnomADGenomeCommand.getInstance().isInclude) {
            GnomADGenome gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele);

            return gnomADGenome.isValid(knownVarOutput.isKnownVariant());
        }

        return true;
    }

    // init MTR score based on most damaging transcript and applied filter
    private boolean isMTRValid() {
        if (MTRCommand.isInclude) {
            // MTR filters will only apply missense variants
            if (mostDamagingAnnotation.effect.startsWith("missense_variant")) {
                MTR mtr = new MTR(chrStr, startPosition);

                return mtr.isValid();
            }
        }

        return true;
    }

    private void initKnownVar() {
        if (KnownVarCommand.isInclude) {
            knownVarOutput = new KnownVarOutput(this);
        }
    }

    private void initRevel() {
        if (RevelCommand.isInclude) {
            revel = RevelManager.getRevel(chrStr, startPosition, refAllele, allele, isMNV());
        }
    }

    private void initPrimateAI() {
        if (PrimateAICommand.isInclude) {
            primateAI = PrimateAIManager.getPrimateAI(chrStr, startPosition, refAllele, allele, isMNV());
        }
    }

    private boolean isRevelValid() {
        if (RevelCommand.isInclude) {
            return RevelCommand.isValid(revel, mostDamagingAnnotation.effect);
        }

        return true;
    }

    private boolean isPrimateAIValid() {
        if (PrimateAICommand.isInclude) {
            return PrimateAICommand.isValid(primateAI, mostDamagingAnnotation.effect);
        }

        return true;
    }
}
