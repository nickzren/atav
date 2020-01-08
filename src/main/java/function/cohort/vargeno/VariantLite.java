package function.cohort.vargeno;

import function.annotation.base.Annotation;
import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.exac.ExAC;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADGenome;
import function.external.primateai.PrimateAICommand;
import function.external.revel.RevelCommand;
import function.external.subrvis.SubRvisCommand;
import function.variant.base.VariantLevelFilterCommand;
import function.variant.base.VariantManager;
import global.Data;
import global.Index;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class VariantLite {

    private String variantID;
    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSNV;
    private ExAC exac;
    private GnomADExome gnomADExome;
    private GnomADGenome gnomADGenome;
    private StringJoiner allAnnotationSJ = new StringJoiner(",");
    private List<String> geneList = new ArrayList();
    private Annotation mostDamagingAnnotation = new Annotation();
    private float subRVISDomainScorePercentile;
    private float subRVISExonScorePercentile;
    private float revel;
    private float primateAI;
    private int[] qcFailSample = new int[2];
    private float looAF;
    private CSVRecord record;

    public VariantLite(CSVRecord record) {
        this.record = record;
        variantID = record.get(ListVarGenoLite.VARIANT_ID_HEADER);
        String[] tmp = variantID.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSNV = ref.length() == alt.length();

        exac = new ExAC(chr, pos, ref, alt, record);
        gnomADExome = new GnomADExome(chr, pos, ref, alt, record);
        gnomADGenome = new GnomADGenome(chr, pos, ref, alt, record);

        String allAnnotation = record.get(ListVarGenoLite.ALL_ANNOTATION_HEADER);
        processAnnotation(
                allAnnotation,
                chr,
                pos,
                geneList,
                mostDamagingAnnotation);
        
        subRVISDomainScorePercentile = FormatManager.getFloat(record.get(ListVarGenoLite.SUBRVIS_DOMAIN_SCORE_PERCENTILE_HEADER));
        subRVISExonScorePercentile = FormatManager.getFloat(record.get(ListVarGenoLite.SUBRVIS_EXON_SCORE_PERCENTILE_HEADER));
        revel = FormatManager.getFloat(record.get(ListVarGenoLite.REVEL_HEADER));
        primateAI = FormatManager.getFloat(record.get(ListVarGenoLite.PRIMATE_AI_HEADER));

        qcFailSample[Index.CASE] = FormatManager.getInteger(record.get(ListVarGenoLite.QC_FAIL_CASE_HEADER));
        qcFailSample[Index.CTRL] = FormatManager.getInteger(record.get(ListVarGenoLite.QC_FAIL_CTRL_HEADER));

        looAF = FormatManager.getFloat(record.get(ListVarGenoLite.LOO_AF_HEADER));
    }

    private void processAnnotation(
            String allAnnotation,
            String chr,
            int pos,
            List<String> geneList,
            Annotation mostDamagingAnnotation) {
        for (String annotation : allAnnotation.split(",")) {
            String[] values = annotation.split("\\|");
            String effect = values[0];
            String geneName = values[1];
            String stableIdStr = values[2];
            int stableId = getIntStableId(stableIdStr);
            String HGVS_c = values[3];
            String HGVS_p = values[4];
            float polyphenHumdiv = FormatManager.getFloat(values[5]);
            float polyphenHumvar = FormatManager.getFloat(values[6]);

            // --effect filter applied
            // --polyphen-humdiv filter applied
            // --gene or --gene-boundary filter applied
            if (EffectManager.isEffectContained(effect)
                    && PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                    && GeneManager.isValid(geneName, chr, pos)) {
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
                if (!geneList.contains(geneName)) {
                    geneList.add(geneName);
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

    private int getIntStableId(String value) {
        if (value.equals(Data.STRING_NA)) {
            return Data.INTEGER_NA;
        } else {
            return Integer.valueOf(value.substring(4)); // remove ENST
        }
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
        } else if (CHMCommand.isExcludeRepeatRegion
                && CHMManager.isRepeatRegion(chr, pos)) {
            return false;
        }

        return exac.isValid()
                && gnomADExome.isValid()
                && gnomADGenome.isValid()
                && !geneList.isEmpty()
                && SubRvisCommand.isSubRVISDomainScorePercentileValid(subRVISDomainScorePercentile)
                && SubRvisCommand.isSubRVISExonScorePercentileValid(subRVISExonScorePercentile)
                && RevelCommand.isMinRevelValid(revel)
                && PrimateAICommand.isMinPrimateAIValid(primateAI)
                && CohortLevelFilterCommand.isMaxLooAFValid(looAF)
                && isMaxQcFailSampleValid();
    }

    private boolean isMaxQcFailSampleValid() {
        int totalQCFailSample = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        return CohortLevelFilterCommand.isMaxQcFailSampleValid(totalQCFailSample);
    }

    public String getVariantID() {
        return variantID;
    }

    public CSVRecord getRecord() {
        return record;
    }

    public Annotation getMostDamagingAnnotation() {
        return mostDamagingAnnotation;
    }

    public String getAllAnnotation() {
        return FormatManager.appendDoubleQuote(allAnnotationSJ.toString());
    }

    public List<String> getGeneList() {
        return geneList;
    }

    public boolean isSNV() {
        return isSNV;
    }
}
