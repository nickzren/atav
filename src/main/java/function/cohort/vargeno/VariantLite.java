package function.cohort.vargeno;

import function.annotation.base.Annotation;
import function.annotation.base.AnnotationLevelFilterCommand;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCROutput;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.discovehr.DiscovEHR;
import function.external.discovehr.DiscovEHRCommand;
import function.external.exac.ExAC;
import function.external.exac.ExACCommand;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADGenome;
import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBROutput;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCOutput;
import function.external.mtr.MTR;
import function.external.mtr.MTRCommand;
import function.external.pext.PextCommand;
import function.external.pext.PextOutput;
import function.external.primateai.PrimateAI;
import function.external.primateai.PrimateAICommand;
import function.external.revel.Revel;
import function.external.revel.RevelCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisOutput;
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
    private SubRvisOutput subrvis;
    private LIMBROutput limbr;
    private CCROutput ccr;
    private DiscovEHR discovEHR;
    private MTR mtr;
    private Revel revel;
    private PrimateAI primateAI;
    private MPCOutput mpc;
    private PextOutput pext;
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

        String allAnnotation = record.get(ListVarGenoLite.ALL_ANNOTATION_HEADER);
        processAnnotation(
                allAnnotation,
                chr,
                pos,
                geneList,
                mostDamagingAnnotation);

        initEXAC(record);
        initGnomADExome(record);
        initGnomADGenome(record);
        initSubRVIS(record);
        initLIMBR(record);
        initCCR(record);
        initDiscovEHR(record);
        initMTR(record);
        initREVEL(record);
        initPrimateAI(record);
        initMPC(record);
        initPEXT(record);
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

    private void initEXAC(CSVRecord record) {
        if (ExACCommand.isIncludeExac) {
            exac = new ExAC(chr, pos, ref, alt, record);
        }
    }

    private void initGnomADExome(CSVRecord record) {
        if (GnomADCommand.isIncludeGnomADExome) {
            gnomADExome = new GnomADExome(chr, pos, ref, alt, record);
        }
    }

    private void initGnomADGenome(CSVRecord record) {
        if (GnomADCommand.isIncludeGnomADGenome) {
            gnomADGenome = new GnomADGenome(chr, pos, ref, alt, record);
        }
    }

    private void initSubRVIS(CSVRecord record) {
        if (SubRvisCommand.isIncludeSubRvis) {
            subrvis = new SubRvisOutput(record);
        }
    }

    private void initLIMBR(CSVRecord record) {
        if (LIMBRCommand.isIncludeLIMBR) {
            limbr = new LIMBROutput(record);
        }
    }

    private void initCCR(CSVRecord record) {
        if (CCRCommand.isIncludeCCR) {
            ccr = new CCROutput(record);
        }
    }
    
    private void initDiscovEHR(CSVRecord record) {
        if (DiscovEHRCommand.isIncludeDiscovEHR) {
            discovEHR = new DiscovEHR(record);
        }
    }

    private void initMTR(CSVRecord record) {
        if (MTRCommand.isIncludeMTR) {
            mtr = new MTR(chr, pos, record);
        }
    }

    private void initREVEL(CSVRecord record) {
        if (RevelCommand.isIncludeRevel) {
            revel = new Revel(record);
        }
    }

    private void initPrimateAI(CSVRecord record) {
        if (PrimateAICommand.isIncludePrimateAI) {
            primateAI = new PrimateAI(record);
        }
    }

    private void initMPC(CSVRecord record) {
        if (MPCCommand.isIncludeMPC) {
            mpc = new MPCOutput(record);
        }
    }
    
    private void initPEXT(CSVRecord record) {
        if (PextCommand.isIncludePext) {
            pext = new PextOutput(record);
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

        return VariantManager.isVariantIdIncluded(variantID)
                && !geneList.isEmpty()
                && exac.isValid()
                && gnomADExome.isValid()
                && gnomADGenome.isValid()
                && isSubRVISValid()
                && isLIMBRValid()
                && isCCRValid()
                && discovEHR.isValid()
                && isMTRValid()
                && revel.isValid()
                && primateAI.isValid()
                && isMPCValid()
                && pext.isValid()
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

    private boolean isSubRVISValid() {
        // sub rvis filters will only apply missense variants except gene boundary option at domain level used
        if (mostDamagingAnnotation.effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
            return subrvis.isValid();
        } else {
            return true;
        }
    }

    private boolean isLIMBRValid() {
        // LIMBR filters will only apply missense variants except gene boundary option at domain level used
        if (mostDamagingAnnotation.effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
            return limbr.isValid();
        } else {
            return true;
        }
    }

    private boolean isCCRValid() {
        // applied filter only to non-LOF variants
        if (!EffectManager.isLOF(mostDamagingAnnotation.effect)) {
            return ccr.isValid();
        }

        return true;
    }

    private boolean isMTRValid() {
        // MTR filters will only apply missense variants
        if (mostDamagingAnnotation.effect.startsWith("missense_variant")) {
            return mtr.isValid();
        }

        return true;
    }

    private boolean isMPCValid() {
        // MPC filters will only apply missense variants
        if (mostDamagingAnnotation.effect.startsWith("missense_variant")) {
            return mpc.isValid();
        } else {
            return true;
        }
    }
}
