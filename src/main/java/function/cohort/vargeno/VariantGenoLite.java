package function.cohort.vargeno;

import function.annotation.base.Annotation;
import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.PolyphenManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.collapsing.CollapsingCommand;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCROutput;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.dbnsfp.DBNSFP;
import function.external.dbnsfp.DBNSFPCommand;
import function.external.dbnsfp.DBNSFPManager;
import function.external.discovehr.DiscovEHR;
import function.external.discovehr.DiscovEHRCommand;
import function.external.exac.ExAC;
import function.external.exac.ExACCommand;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGenome;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.igmaf.IGMAFCommand;
import function.external.igmaf.IGMAFManager;
import function.external.iranome.IranomeCommand;
import function.external.iranome.IranomeManager;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarOutput;
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
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
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
import org.apache.commons.csv.CSVRecord;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class VariantGenoLite extends Variant {

    private int indelLength;
    private StringJoiner allAnnotationSJ = new StringJoiner(",");
    private List<String> geneList = new ArrayList();
    private HashSet<Integer> transcriptSet = new HashSet<>();
    private Annotation mostDamagingAnnotation = new Annotation();
    private ExAC exac;
    private GnomADExome gnomADExome;
    private GnomADGenome gnomADGenome;
    private SubRvisOutput subrvis;
    private LIMBROutput limbr;
    private CCROutput ccr;
    private KnownVarOutput knownVarOutput;
    private float trapScore;
    private DiscovEHR discovEHR;
    private Boolean isLOFTEEHCinCCDS;
    private MTR mtr;
    private Revel revel;
    private PrimateAI primateAI;
    private MPCOutput mpc;
    private PextOutput pext;
    private float gmeAF;
    private float topmedAF;
    private float genomeasiaAF;
    private float iranomeAF;
    private float igmAF;
    private DBNSFP dbNSFP;
    private int[] qcFailSample = new int[2];
    private float looAF;
    private CSVRecord record;

    public VariantGenoLite(CSVRecord record) throws Exception {
        variantIdStr = record.get(ListVarGenoLite.VARIANT_ID_HEADER);
        initVariant(variantIdStr.split("-"));
        
        this.record = record;
        indelLength = allele.length() - refAllele.length();

        // init to apply per annotation
        initREVEL(record);
        initPrimateAI(record);
        initDBNSFP(record);

        String allAnnotation = record.get(ListVarGenoLite.ALL_ANNOTATION_HEADER);
        processAnnotation(
                allAnnotation,
                record);

        if (mostDamagingAnnotation.isValid()) {
            initEXAC(record);
            initGnomADExome(record);
            initGnomADGenome(record);
            initSubRVIS(record);
            initLIMBR(record);
            initCCR(record);
            initTrap(record);
            initDiscovEHR(record);
            initLOFTEE(record);
            initMTR(record);
            initMPC(record);
            initPEXT(record);
            initGME(record);
            initTopMed(record);
            initGenomeAsia(record);
            initIranome(record);
            initIGMAF(record);
            initKnownVar();

            qcFailSample[Index.CASE] = FormatManager.getInteger(record.get(ListVarGenoLite.QC_FAIL_CASE_HEADER));
            qcFailSample[Index.CTRL] = FormatManager.getInteger(record.get(ListVarGenoLite.QC_FAIL_CTRL_HEADER));

            looAF = FormatManager.getFloat(record.get(ListVarGenoLite.LOO_AF_HEADER));
        }
    }

    private void processAnnotation(
            String allAnnotation,
            CSVRecord record) {
        // isValid to false means no annotations passed the filters
        mostDamagingAnnotation.setValid(false);

        for (String annotationStr : allAnnotation.split(",")) {
            String[] values = annotationStr.split("\\|");
            String effect = values[0];
            String geneName = values[1];

            // if --gene-column used, ATAV will perform collapsing by input gene column values
            if (!CollapsingCommand.geneColumn.isEmpty()) {
                geneName = record.get(CollapsingCommand.geneColumn);
            }

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
            annotation.revel = revel == null ? Data.FLOAT_NA : revel.getRevel();
            annotation.primateAI = primateAI == null ? Data.FLOAT_NA : primateAI.getPrimateDLScore();

            // --effect
            // --gene or --gene-boundary
            // --polyphen-humdiv
            // --ensemble-missense
            // --ensemble-missense-2
            // --ccds-only
            // --canonical-only
            // --filter-dbnsfp-all & --filter-dbnsfp-one
            if (EffectManager.isEffectContained(effect)
                    && GeneManager.isValid(geneName, chrStr, startPosition, indelLength)
                    && PolyphenManager.isValid(polyphenHumdiv, polyphenHumvar, effect)
                    && annotation.isEnsembleMissenseValid()
                    && TranscriptManager.isCCDSValid(chrStr, stableId)
                    && TranscriptManager.isCanonicalValid(chrStr, stableId)
                    && DBNSFPManager.isValid(dbNSFP, stableId, effect)
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
                    mostDamagingAnnotation.polyphenHumdiv = polyphenHumdiv;

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

    private void initEXAC(CSVRecord record) {
        if (ExACCommand.getInstance().isInclude) {
            exac = new ExAC(chrStr, startPosition, refAllele, allele, record);
        }
    }

    private void initGnomADExome(CSVRecord record) {
        if (GnomADExomeCommand.getInstance().isInclude) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele, record);
        }
    }

    private void initGnomADGenome(CSVRecord record) {
        if (GnomADGenomeCommand.getInstance().isInclude) {
            gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele, record);
        }
    }

    private void initSubRVIS(CSVRecord record) {
        if (SubRvisCommand.isInclude) {
            subrvis = new SubRvisOutput(record);
        }
    }

    private void initLIMBR(CSVRecord record) {
        if (LIMBRCommand.isInclude) {
            limbr = new LIMBROutput(record);
        }
    }

    private void initCCR(CSVRecord record) {
        if (CCRCommand.isInclude) {
            ccr = new CCROutput(record);
        }
    }

    private void initTrap(CSVRecord record) {
        trapScore = Data.FLOAT_NA;

        if (TrapCommand.isInclude) {
            if (TrapCommand.minTrapScore != Data.NO_FILTER) {
                trapScore = TrapManager.getScore(chrStr, startPosition, allele, isMNV(), mostDamagingAnnotation.geneName);
            } else {
                trapScore = TrapManager.getScore(record);
            }
        }
    }

    private void initDiscovEHR(CSVRecord record) {
        if (DiscovEHRCommand.isInclude) {
            discovEHR = new DiscovEHR(record);
        }
    }

    private void initLOFTEE(CSVRecord record) {
        if (VariantLevelFilterCommand.isIncludeLOFTEE) {
            isLOFTEEHCinCCDS = VariantManager.getLOFTEEHCinCCDS(record);
        }
    }

    private void initMTR(CSVRecord record) {
        if (MTRCommand.isInclude) {
            mtr = new MTR(chrStr, startPosition, record);
        }
    }

    private void initREVEL(CSVRecord record) {
        if (RevelCommand.isInclude) {
            revel = new Revel(record);
        }
    }

    private void initPrimateAI(CSVRecord record) {
        if (PrimateAICommand.isInclude) {
            primateAI = new PrimateAI(record);
        }
    }

    private void initMPC(CSVRecord record) {
        if (MPCCommand.isInclude) {
            mpc = new MPCOutput(record);
        }
    }

    private void initPEXT(CSVRecord record) {
        if (PextCommand.isInclude) {
            pext = new PextOutput(record);
        }
    }

    private void initGME(CSVRecord record) {
        if (GMECommand.getInstance().isInclude) {
            gmeAF = GMEManager.getAF(record);
        }
    }

    private void initTopMed(CSVRecord record) {
        if (TopMedCommand.getInstance().isInclude) {
            topmedAF = TopMedManager.getAF(record);
        }
    }

    private void initGenomeAsia(CSVRecord record) {
        if (GenomeAsiaCommand.getInstance().isInclude) {
            genomeasiaAF = GenomeAsiaManager.getAF(record);
        }
    }

    private void initIranome(CSVRecord record) {
        if (IranomeCommand.getInstance().isInclude) {
            iranomeAF = IranomeManager.getAF(record);
        }
    }

    private void initIGMAF(CSVRecord record) {
        if (IGMAFCommand.getInstance().isInclude) {
            igmAF = IGMAFManager.getAF(record);
        }
    }

    private void initDBNSFP(CSVRecord record) {
        if (DBNSFPCommand.isInclude) {
            dbNSFP = new DBNSFP(record);
        }
    }

    private void initKnownVar() {
        if (KnownVarCommand.isInclude) {
            knownVarOutput = new KnownVarOutput(this);
        }
    }

    public boolean isValid() throws SQLException {
        if (VariantLevelFilterCommand.isExcludeMultiallelicVariant
                && VariantManager.isMultiallelicVariant(chrStr, startPosition)) {
            // exclude Multiallelic site > 1 variant
            return false;
        } else if (VariantLevelFilterCommand.isExcludeMultiallelicVariant2
                && VariantManager.isMultiallelicVariant2(chrStr, startPosition)) {
            // exclude Multiallelic site > 2 variants
            return false;
        } else if (CHMCommand.isExclude
                && CHMManager.isRepeatRegion(chrStr, startPosition)) {
            return false;
        }

        return mostDamagingAnnotation.isValid()
                && VariantManager.isVariantIdIncluded(variantIdStr)
                && !VariantManager.isVariantIdExcluded(variantIdStr)
                && !geneList.isEmpty()
                && !transcriptSet.isEmpty()
                && isExacValid()
                && isGnomADExomeValid()
                && isGnomADGenomeValid()
                && isSubRVISValid()
                && isLIMBRValid()
                && isCCRValid()
                && isDiscovEHRValid()
                && isLOFTEEValid()
                && isMTRValid()
                && isRevelValid()
                && isPrimateAIValid()
                && TrapCommand.isValid(trapScore, mostDamagingAnnotation.effect)
                && isMPCValid()
                && isPextValid()
                && isGMEAFValid()
                && isTopMedAFValid()
                && isGenomeAsiaAFValid()
                && isIranomeAFValid()
                && isIGMAFValid()
                && CohortLevelFilterCommand.isLooAFValid(looAF)
                && isMaxQcFailSampleValid();
    }

    private boolean isMaxQcFailSampleValid() {
        int totalQCFailSample = qcFailSample[Index.CASE] + qcFailSample[Index.CTRL];

        return CohortLevelFilterCommand.isMaxQcFailSampleValid(totalQCFailSample);
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

    public HashSet<Integer> getTranscriptSet() {
        return transcriptSet;
    }

    private boolean isExacValid() {
        if (exac == null) {
            return true;
        }

        return exac.isValid(knownVarOutput.isKnownVariant());
    }

    private boolean isGnomADExomeValid() {
        if (gnomADExome == null) {
            return true;
        }

        return gnomADExome.isValid(knownVarOutput.isKnownVariant());
    }

    private boolean isGnomADGenomeValid() {
        if (gnomADGenome == null) {
            return true;
        }

        return gnomADGenome.isValid(knownVarOutput.isKnownVariant());
    }

    private boolean isSubRVISValid() {
        if (subrvis == null) {
            return true;
        }

        // sub rvis filters will only apply missense variants except gene boundary option at domain level used
        if (mostDamagingAnnotation.effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
            return subrvis.isValid();
        } else {
            return true;
        }
    }

    private boolean isLIMBRValid() {
        if (limbr == null) {
            return true;
        }

        // LIMBR filters will only apply missense variants except gene boundary option at domain level used
        if (mostDamagingAnnotation.effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
            return limbr.isValid();
        } else {
            return true;
        }
    }

    private boolean isCCRValid() {
        if (ccr == null) {
            return true;
        }

        // applied filter only to non-LOF variants
        if (!EffectManager.isLOF(mostDamagingAnnotation.effect)) {
            return ccr.isValid();
        }

        return true;
    }

    private boolean isDiscovEHRValid() {
        if (discovEHR == null) {
            return true;
        }

        return discovEHR.isValid();
    }

    private boolean isLOFTEEValid() {
        return VariantLevelFilterCommand.isLOFTEEValid(isLOFTEEHCinCCDS);
    }

    private boolean isMTRValid() {
        if (mtr == null) {
            return true;
        }

        // MTR filter will only apply missense variants
        if (mostDamagingAnnotation.effect.startsWith("missense_variant")) {
            return mtr.isValid();
        }

        return true;
    }

    private boolean isRevelValid() {
        if (revel == null) {
            return true;
        }

        return RevelCommand.isValid(revel.getRevel(), mostDamagingAnnotation.effect);
    }

    private boolean isPrimateAIValid() {
        if (primateAI == null) {
            return true;
        }

        return PrimateAICommand.isValid(primateAI.getPrimateDLScore(), mostDamagingAnnotation.effect);
    }

    private boolean isMPCValid() {
        if (mpc == null) {
            return true;
        }

        // MPC filter will only apply missense variants
        if (mostDamagingAnnotation.effect.startsWith("missense_variant")) {
            return mpc.isValid();
        } else {
            return true;
        }
    }

    private boolean isPextValid() {
        if (pext == null) {
            return true;
        }

        return pext.isValid();
    }

    private boolean isGMEAFValid() {
        return GMECommand.getInstance().isAFValid(gmeAF, knownVarOutput.isKnownVariant());
    }

    private boolean isTopMedAFValid() {
        return TopMedCommand.getInstance().isAFValid(topmedAF, knownVarOutput.isKnownVariant());
    }

    private boolean isGenomeAsiaAFValid() {
        return GenomeAsiaCommand.getInstance().isAFValid(genomeasiaAF, knownVarOutput.isKnownVariant());
    }

    private boolean isIranomeAFValid() {
        return IranomeCommand.getInstance().isAFValid(iranomeAF, knownVarOutput.isKnownVariant());
    }

    private boolean isIGMAFValid() {
        return IGMAFCommand.getInstance().isAFValid(igmAF, knownVarOutput.isKnownVariant());
    }

    public float getTrapScore() {
        return trapScore;
    }

    public String getATAVLink() {
        return "\"=HYPERLINK(\"\"http://atavdb.org/variant/" + variantIdStr + "\"\",\"\"ATAV\"\")\"";
    }
}
