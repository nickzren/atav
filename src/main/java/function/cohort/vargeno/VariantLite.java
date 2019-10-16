package function.cohort.vargeno;

import function.annotation.base.Annotation;
import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import static function.cohort.vargeno.ListVarGenoLite.LOO_AF_HEADER;
import function.external.exac.ExAC;
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
    private StringJoiner allGeneTranscriptSJ = new StringJoiner(";");
    private List<String> geneList = new ArrayList();
    private Annotation mostDamagingAnnotation = new Annotation();
    private float looAF;
    private ExAC exac;
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
        looAF = FormatManager.getFloat(record.get(LOO_AF_HEADER));

        String allAnnotation = record.get(ListVarGenoLite.ALL_ANNOTATION_HEADER);
        processAnnotation(
                allAnnotation,
                chr,
                pos,
                allGeneTranscriptSJ,
                geneList,
                mostDamagingAnnotation);
    }

    private void processAnnotation(
            String allAnnotation,
            String chr,
            int pos,
            StringJoiner allGeneTranscriptSJ,
            List<String> geneList,
            Annotation mostDamagingAnnotation) {
        for (String annotation : allAnnotation.split(";")) {
            String[] values = annotation.split("\\|");
            String effect = values[0];
            String geneName = values[1];
            String stableId = values[2];
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
                    mostDamagingAnnotation.stableId = FormatManager.getInteger(stableId);
                    mostDamagingAnnotation.HGVS_c = HGVS_c;
                    mostDamagingAnnotation.HGVS_p = HGVS_p;
                    mostDamagingAnnotation.geneName = geneName;
                }

                StringJoiner geneTranscriptSJ = new StringJoiner("|");
                geneTranscriptSJ.add(effect);
                geneTranscriptSJ.add(geneName);
                geneTranscriptSJ.add(stableId);
                geneTranscriptSJ.add(HGVS_c);
                geneTranscriptSJ.add(HGVS_p);
                geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumdiv));
                geneTranscriptSJ.add(FormatManager.getFloat(polyphenHumvar));

                allGeneTranscriptSJ.add(geneTranscriptSJ.toString());
                if (!geneList.contains(geneName)) {
                    geneList.add(geneName);
                }

                mostDamagingAnnotation.polyphenHumdiv = MathManager.max(mostDamagingAnnotation.polyphenHumdiv, polyphenHumdiv);
                mostDamagingAnnotation.polyphenHumvar = MathManager.max(mostDamagingAnnotation.polyphenHumvar, polyphenHumvar);

                boolean isCCDS = TranscriptManager.isCCDSTranscript(chr, FormatManager.getInteger(stableId));

                if (isCCDS) {
                    mostDamagingAnnotation.polyphenHumdivCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumdivCCDS, polyphenHumdiv);
                    mostDamagingAnnotation.polyphenHumvarCCDS = MathManager.max(mostDamagingAnnotation.polyphenHumvarCCDS, polyphenHumvar);

                    mostDamagingAnnotation.hasCCDS = true;
                }
            }
        }
    }

    public boolean isValid() {
        return CohortLevelFilterCommand.isMaxLooAFValid(looAF)
                && exac.isValid()
                && !geneList.isEmpty();

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
        return allGeneTranscriptSJ.toString();
    }
    
    public List<String> getGeneList() {
        return geneList;
    }
    
    public boolean isSNV() {
        return isSNV;
    }
}
