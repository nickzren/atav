package function.cohort.vcf;

import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.SampleManager;
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
public class VCFLite {

    private String variantID;
    private String rsNumber;
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private int indelLength;
    private boolean isSNV;

    private StringJoiner allAnnotationSJ = new StringJoiner(",");
    private List<String> geneList = new ArrayList();
    private HashSet<Integer> transcriptSet = new HashSet<>();
    private Annotation mostDamagingAnnotation = new Annotation();

    public int[] genoCount = new int[3];
    private float af;

    private byte[] gtArr = new byte[SampleManager.getTotalSampleNum()];
    private short[] dpArr = new short[SampleManager.getTotalSampleNum()];
    private byte[] gqArr = new byte[SampleManager.getTotalSampleNum()];

    public VCFLite(String values[]) {
        chr = values[0];
        pos = Integer.valueOf(values[1]);
        rsNumber = values[2];
        ref = values[3];
        alt = values[4];

        indelLength = ref.length() - alt.length();
        isSNV = ref.length() == alt.length();

        StringJoiner sj = new StringJoiner("-");
        sj.add(chr);
        sj.add(values[1]);
        sj.add(ref);
        sj.add(alt);
        variantID = sj.toString();

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

            // --effect
            // --gene or --gene-boundary
            // --polyphen-humdiv
            // --ccds-only
            // --canonical-only
            if (EffectManager.isEffectContained(effect)
                    && GeneManager.isValid(geneName, chr, pos, indelLength)
                    && PolyphenManager.isValid(polyphenHumdiv, polyphenHumvar, effect)
                    && TranscriptManager.isCCDSValid(chr, stableId)
                    && TranscriptManager.isCanonicalValid(chr, stableId)) {
                if (!mostDamagingAnnotation.isValid()) {
                    mostDamagingAnnotation.setValid(true);
                }

                // reset gene name to gene domain name so the downstream procedure could match correctly
                // only for gene boundary input
                geneName = GeneManager.getGeneDomainName(geneName, chr, pos);

                if (mostDamagingAnnotation.effect == null) {
                    mostDamagingAnnotation.effect = effect;
                    mostDamagingAnnotation.stableId = stableId;
                    mostDamagingAnnotation.HGVS_c = HGVS_c;
                    mostDamagingAnnotation.HGVS_p = HGVS_p;
                    mostDamagingAnnotation.geneName = geneName;
                }

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

                mostDamagingAnnotation.polyphenHumdiv = MathManager.max(mostDamagingAnnotation.polyphenHumdiv, polyphenHumdiv);
                mostDamagingAnnotation.polyphenHumvar = MathManager.max(mostDamagingAnnotation.polyphenHumvar, polyphenHumvar);

                boolean isCCDS = TranscriptManager.isCCDSTranscript(chr, stableId);

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
                // apply --min-coverage or --min-gq filter
                if (!GenotypeLevelFilterCommand.isMinGqValid(gq)
                        || !GenotypeLevelFilterCommand.isMinDpBinValid(dp)) {
                    gt = Data.BYTE_NA;
                } else {
                    genoCount[gt]++;
                }
            }

            sampleIndex = vcfColumnIndex - 9;
            gtArr[sampleIndex] = gt;
            dpArr[sampleIndex] = dp;
            gqArr[sampleIndex] = gq;
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

        sj.add(chr);
        sj.add(FormatManager.getInteger(pos));
        sj.add(rsNumber);
        sj.add(ref);
        sj.add(alt);
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

    public boolean isValid() throws SQLException {
        if (VariantLevelFilterCommand.isExcludeMultiallelicVariant
                && VariantManager.isMultiallelicVariant(chr, pos)) {
            // exclude Multiallelic site > 1 variant
            return false;
        } else if (VariantLevelFilterCommand.isExcludeMultiallelicVariant2
                && VariantManager.isMultiallelicVariant2(chr, pos)) {
            // exclude Multiallelic site > 2 variants
            return false;
        }

        return mostDamagingAnnotation.isValid()
                && VariantManager.isVariantIdIncluded(variantID)
                && !VariantManager.isVariantIdExcluded(variantID)
                && !geneList.isEmpty()
                && !transcriptSet.isEmpty()
                && CohortLevelFilterCommand.isAFValid(af);
    }
}
