package function.annotation.base;

import function.external.ccr.CCRCommand;
import function.external.ccr.CCROutput;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBROutput;
import function.external.denovo.DenovoDB;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHR;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.Evs;
import function.external.evs.EvsCommand;
import function.external.exac.ExAC;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADCommand;
import function.external.genomes.Genomes;
import function.external.genomes.GenomesCommand;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.gnomad.GnomADGenome;
import function.external.gnomad.GnomADManager;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import function.external.kaviar.Kaviar;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarOutput;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCManager;
import function.external.mtr.MTR;
import function.external.mtr.MTRCommand;
import function.external.pext.PextCommand;
import function.external.pext.PextManager;
import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisOutput;
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import function.variant.base.VariantLevelFilterCommand;
import global.Data;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class AnnotatedVariant extends Variant {

    // annotations / most damaging effect annotations
    private int stableId;
    private String effect = "";
    private int effectID;
    private String HGVS_c = "";
    private String HGVS_p = "";
    private float polyphenHumdiv = Data.FLOAT_NA;
    private float polyphenHumdivCCDS = Data.FLOAT_NA;
    private float polyphenHumvar = Data.FLOAT_NA;
    private float polyphenHumvarCCDS = Data.FLOAT_NA;
    private boolean hasCCDS = false;
    private String geneName = "";

    private List<String> geneList = new ArrayList<>();
    private StringJoiner allAnnotationSJ = new StringJoiner(",");

    // external db annotations
    private ExAC exac;
    private String exacGeneVariantCountStr;
    private GnomADExome gnomADExome;
    private GnomADGenome gnomADGenome;
    private Kaviar kaviar;
    private Evs evs;
    private float gerpScore;
    private float trapScore;
    private float pextRatio;
    private KnownVarOutput knownVarOutput;
    private SubRvisOutput subRvisOutput;
    private LIMBROutput limbrOutput;
    private Genomes genomes;
    private String mgiStr;
    private DenovoDB denovoDB;
    private DiscovEHR discovEHR;
    private MTR mtr;
    private float revel;
    private float primateAI;
    private CCROutput ccrOutput;
    private Boolean isLOFTEEHCinCCDS;
    private float mpc;
    private Boolean isRepeatRegion;
    private float gmeAF;
    private float topmedAF;
    private float genomeasiaAF;

    public boolean isValid = true;

    public AnnotatedVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        checkValid();
    }

    private void checkValid() throws Exception {
        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid && CHMCommand.isExcludeRepeatRegion) {
            isRepeatRegion = CHMManager.isRepeatRegion(chrStr, startPosition);
            isValid = isRepeatRegion;
        }

        if (isValid && GMECommand.isIncludeGME) {
            gmeAF = GMEManager.getAF(variantIdStr);

            isValid = GMECommand.isMaxGMEAFValid(gmeAF);
        }

        if (isValid && TopMedCommand.isIncludeTopMed) {
            topmedAF = TopMedManager.getAF(variantIdStr);

            isValid = TopMedCommand.isMaxAFValid(topmedAF);
        }
        
        if (isValid && GenomeAsiaCommand.isInclude) {
            genomeasiaAF = GenomeAsiaManager.getAF(variantIdStr);

            isValid = GenomeAsiaCommand.isMaxAFValid(genomeasiaAF);
        }

        if (isValid && GnomADCommand.isIncludeGnomADExome) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADExome.isValid();
        }

        if (isValid && GnomADCommand.isIncludeGnomADGenome) {
            gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADGenome.isValid();
        }

        if (isValid && ExACCommand.isIncludeExac) {
            exac = new ExAC(chrStr, startPosition, refAllele, allele);

            isValid = exac.isValid();
        }

        if (isValid && EvsCommand.isIncludeEvs) {
            evs = new Evs(chrStr, startPosition, refAllele, allele);

            isValid = evs.isValid();
        }

        if (isValid && GerpCommand.isIncludeGerp) {
            gerpScore = GerpManager.getScore(chrStr, startPosition, refAllele, allele);

            isValid = GerpCommand.isGerpScoreValid(gerpScore);
        }

        if (isValid && KaviarCommand.isIncludeKaviar) {
            kaviar = new Kaviar(chrStr, startPosition, refAllele, allele);

            isValid = kaviar.isValid();
        }

        if (isValid && GenomesCommand.isInclude1000Genomes) {
            genomes = new Genomes(chrStr, startPosition, refAllele, allele);

            isValid = genomes.isValid();
        }

        if (isValid && DiscovEHRCommand.isIncludeDiscovEHR) {
            discovEHR = new DiscovEHR(chrStr, startPosition, refAllele, allele);

            isValid = discovEHR.isValid();
        }

        if (isValid && VariantLevelFilterCommand.isIncludeLOFTEE) {
            isLOFTEEHCinCCDS = VariantManager.getLOFTEEHCinCCDS(chrStr, startPosition, refAllele, allele);

            isValid = VariantLevelFilterCommand.isLOFTEEValid(isLOFTEEHCinCCDS);
        }
    }

    public void update(Annotation annotation) {
        if (isValid) {
            if (effect.isEmpty()) { // init most damaging effect annotations
                stableId = annotation.stableId;
                effect = annotation.effect;
                effectID = annotation.effectID;
                HGVS_c = annotation.HGVS_c;
                HGVS_p = annotation.HGVS_p;
                geneName = annotation.geneName;
            }

            StringJoiner annotationSJ = new StringJoiner("|");
            annotationSJ.add(annotation.effect);
            annotationSJ.add(annotation.geneName);
            annotationSJ.add(getStableId(annotation.stableId));
            annotationSJ.add(annotation.HGVS_c);
            annotationSJ.add(annotation.HGVS_p);
            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumdiv));
            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumvar));

            allAnnotationSJ.add(annotationSJ.toString());

            polyphenHumdiv = MathManager.max(polyphenHumdiv, annotation.polyphenHumdiv);
            polyphenHumvar = MathManager.max(polyphenHumvar, annotation.polyphenHumvar);

            if (annotation.isCCDS) {
                polyphenHumdivCCDS = MathManager.max(polyphenHumdivCCDS, annotation.polyphenHumdivCCDS);
                polyphenHumvarCCDS = MathManager.max(polyphenHumvarCCDS, annotation.polyphenHumvarCCDS);

                hasCCDS = true;
            }

            if (!geneList.contains(annotation.geneName)) {
                geneList.add(annotation.geneName);
            }
        }
    }

    public String getAllAnnotation() {
        return allAnnotationSJ.toString();
    }

    public void initExternalData() {
        if (KnownVarCommand.isIncludeKnownVar) {
            knownVarOutput = new KnownVarOutput(this);
        }

        if (MgiCommand.isIncludeMgi) {
            mgiStr = MgiManager.getLine(getGeneName());
        }

        if (DenovoDBCommand.isIncludeDenovoDB) {
            denovoDB = new DenovoDB(chrStr, startPosition, refAllele, allele);
        }

        if (ExACCommand.isIncludeExacGeneVariantCount) {
            exacGeneVariantCountStr = ExACManager.getLine(getGeneName());
        }

        if (TrapCommand.isIncludeTrap) {
            trapScore = isIndel() ? Data.FLOAT_NA
                    : TrapManager.getScore(chrStr, getStartPosition(), allele, isMNV(), geneName);
        }
    }

    public boolean isValid() {
        return isValid
                && isSubRVISValid()
                && isLIMBRValid()
                && isCCRValid()
                && isMTRValid()
                && isPextValid()
                && isRevelValid()
                && isPrimateAIValid()
                && isMPCValid();
    }

    // init sub rvis score base on most damaging gene and applied filter
    private boolean isSubRVISValid() {
        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(getGeneName(), getChrStr(), getStartPosition());

            // sub rvis filters will only apply missense variants except gene boundary option at domain level used
            if (effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
                return subRvisOutput.isValid();
            } else {
                return true;
            }
        }

        return true;
    }

    // init LIMBR score base on most damaging gene and applied filter
    private boolean isLIMBRValid() {
        if (LIMBRCommand.isIncludeLIMBR) {
            limbrOutput = new LIMBROutput(getGeneName(), getChrStr(), getStartPosition());

            // LIMBR filters will only apply missense variants except gene boundary option at domain level used
            if (effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
                return limbrOutput.isValid();
            } else {
                return true;
            }
        }

        return true;
    }

    // init CCR score and applied filter only to non-LOF variants
    private boolean isCCRValid() {
        if (CCRCommand.isIncludeCCR) {
            ccrOutput = new CCROutput(geneList, getChrStr(), getStartPosition());

            if (!EffectManager.isLOF(effectID)) {
                return ccrOutput.isValid();
            }
        }

        return true;
    }

    // init MTR score based on most damaging transcript and applied filter
    private boolean isMTRValid() {
        if (MTRCommand.isIncludeMTR) {
            // MTR filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                mtr = new MTR(chrStr, startPosition);

                return mtr.isValid();
            }
        }

        return true;
    }

    // init PEXT score and applied filter 
    private boolean isPextValid() {
        if (PextCommand.isIncludePext) {
            pextRatio = PextManager.getRatio(chrStr, getStartPosition());

            return PextCommand.isPextRatioValid(pextRatio);
        }

        return true;
    }

    // init REVEL score base on most damaging gene and applied filter
    private boolean isRevelValid() {
        if (RevelCommand.isIncludeRevel) {
            revel = RevelManager.getRevel(chrStr, startPosition, refAllele, allele, isMNV());

            // REVEL filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return RevelCommand.isMinRevelValid(revel);
            } else {
                return true;
            }
        }

        return true;
    }

    // init PrimateAI score base on most damaging gene and applied filter
    private boolean isPrimateAIValid() {
        if (PrimateAICommand.isIncludePrimateAI) {
            primateAI = PrimateAIManager.getPrimateAI(chrStr, startPosition, refAllele, allele, isMNV());

            // PrimateAI filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return PrimateAICommand.isMinPrimateAIValid(primateAI);
            } else {
                return true;
            }
        }

        return true;
    }

    // init MPC score base on most damaging gene and applied filter
    private boolean isMPCValid() {
        if (MPCCommand.isIncludeMPC) {
            mpc = MPCManager.getScore(chrStr, getStartPosition(), refAllele, allele);

            // MPC filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return MPCCommand.isMPCValid(mpc);
            } else {
                return true;
            }
        }

        return true;
    }

    public void getAnnotationData(StringJoiner sj) {
        sj.add(getStableId(stableId));
        sj.add(Boolean.toString(hasCCDS));
        sj.add(effect);
        sj.add(HGVS_c);
        sj.add(HGVS_p);
        sj.add(FormatManager.getFloat(polyphenHumdiv));
        sj.add(PolyphenManager.getPrediction(polyphenHumdiv, effect));
        sj.add(FormatManager.getFloat(polyphenHumdivCCDS));
        sj.add(PolyphenManager.getPrediction(polyphenHumdivCCDS, effect));
        sj.add(FormatManager.getFloat(polyphenHumvar));
        sj.add(PolyphenManager.getPrediction(polyphenHumvar, effect));
        sj.add(FormatManager.getFloat(polyphenHumvarCCDS));
        sj.add(PolyphenManager.getPrediction(polyphenHumvarCCDS, effect));
        sj.add("'" + geneName + "'");
        sj.add("'" + GeneManager.getUpToDateGene(geneName) + "'");
        sj.add(FormatManager.appendDoubleQuote(getAllAnnotation()));
    }

    private String getStableId(int stableId) {
        if (stableId == Data.INTEGER_NA) {
            return Data.STRING_NA;
        }

        StringBuilder idSB = new StringBuilder(String.valueOf(stableId));

        int zeroStringLength = TranscriptManager.TRANSCRIPT_LENGTH - idSB.length() - 4;

        for (int i = 0; i < zeroStringLength; i++) {
            idSB.insert(0, 0);
        }

        idSB.insert(0, "ENST");

        return idSB.toString();
    }

    public boolean hasCCDS() {
        return hasCCDS;
    }

    public String getEffect() {
        return effect;
    }

    public String getHGVS_c() {
        return HGVS_c;
    }

    public String getHGVS_p() {
        return HGVS_p;
    }

    public String getGeneName() {
        return geneName;
    }

    public List<String> getGeneList() {
        return geneList;
    }

    public void getExternalData(StringJoiner sj) {
        if (EvsCommand.isIncludeEvs) {
            sj.merge(getEvsStringJoiner());
        }

        if (ExACCommand.isIncludeExac) {
            sj.merge(getExacStringJoiner());
        }

        if (ExACCommand.isIncludeExacGeneVariantCount) {
            sj.add(getExacGeneVariantCount());
        }

        if (GnomADCommand.isIncludeGnomADExome) {
            sj.merge(getGnomADExomeStringJoiner());
        }

        if (GnomADCommand.isIncludeGnomADGenome) {
            sj.merge(getGnomADGenomeStringJoiner());
        }

        if (GnomADCommand.isIncludeGnomADGeneMetrics) {
            sj.add(getGnomADGeneMetrics());
        }

        if (KnownVarCommand.isIncludeKnownVar) {
            sj.merge(getKnownVarStringJoiner());
        }

        if (KaviarCommand.isIncludeKaviar) {
            sj.merge(getKaviarStringJoiner());
        }

        if (GenomesCommand.isInclude1000Genomes) {
            sj.merge(get1000GenomesStringJoiner());
        }

        if (RvisCommand.isIncludeRvis) {
            sj.add(getRvis());
        }

        if (SubRvisCommand.isIncludeSubRvis) {
            sj.merge(getSubRvisStringJoiner());
        }

        if (LIMBRCommand.isIncludeLIMBR) {
            sj.merge(getLIMBRStringJoiner());
        }

        if (CCRCommand.isIncludeCCR) {
            sj.merge(getCCRStringJoiner());
        }

        if (GerpCommand.isIncludeGerp) {
            sj.add(getGerpScore());
        }

        if (TrapCommand.isIncludeTrap) {
            sj.add(getTrapScore());
        }

        if (MgiCommand.isIncludeMgi) {
            sj.add(getMgi());
        }

        if (DenovoDBCommand.isIncludeDenovoDB) {
            sj.merge(getDenovoDBStringJoiner());
        }

        if (DiscovEHRCommand.isIncludeDiscovEHR) {
            sj.add(getDiscovEHR());
        }

        if (MTRCommand.isIncludeMTR) {
            sj.add(getMTR());
        }

        if (RevelCommand.isIncludeRevel) {
            sj.add(FormatManager.getFloat(revel));
        }

        if (PrimateAICommand.isIncludePrimateAI) {
            sj.add(FormatManager.getFloat(primateAI));
        }

        if (VariantLevelFilterCommand.isIncludeLOFTEE) {
            sj.add(FormatManager.getBoolean(isLOFTEEHCinCCDS));
        }

        if (MPCCommand.isIncludeMPC) {
            sj.add(getMPC());
        }

        if (PextCommand.isIncludePext) {
            sj.add(getPextRatio());
        }

        if (CHMCommand.isFlagRepeatRegion) {
            if (isRepeatRegion == null) {
                isRepeatRegion = CHMManager.isRepeatRegion(chrStr, startPosition);
            }

            sj.add(FormatManager.getBoolean(isRepeatRegion));
        }

        if (GMECommand.isIncludeGME) {
            sj.add(getGME());
        }
        
        if (TopMedCommand.isIncludeTopMed) {
            sj.add(getTopMed());
        }
        
        if (GenomeAsiaCommand.isInclude) {
            sj.add(getGenomeAsia());
        }
    }

    public StringJoiner getEvsStringJoiner() {
        return evs.getStringJoiner();
    }

    public StringJoiner getExacStringJoiner() {
        return exac.getStringJoiner();
    }

    public String getExacGeneVariantCount() {
        return exacGeneVariantCountStr;
    }

    public StringJoiner getGnomADExomeStringJoiner() {
        return gnomADExome.getStringJoiner();
    }

    public StringJoiner getGnomADGenomeStringJoiner() {
        return gnomADGenome.getStringJoiner();
    }

    public String getGnomADGeneMetrics() {
        return GnomADManager.getGeneMetricsLine(getGeneName());
    }

    public StringJoiner getKnownVarStringJoiner() {
        return knownVarOutput.getStringJoiner();
    }

    public StringJoiner getKaviarStringJoiner() {
        return kaviar.getStringJoiner();
    }

    public StringJoiner get1000GenomesStringJoiner() {
        return genomes.getStringJoiner();
    }

    public String getRvis() {
        return RvisManager.getLine(getGeneName());
    }

    public StringJoiner getSubRvisStringJoiner() {
        return subRvisOutput.getStringJoiner();
    }

    public StringJoiner getLIMBRStringJoiner() {
        return limbrOutput.getStringJoiner();
    }

    public StringJoiner getCCRStringJoiner() {
        return ccrOutput.getStringJoiner();
    }

    public String getGerpScore() {
        return FormatManager.getFloat(gerpScore);
    }

    public String getTrapScore() {
        return FormatManager.getFloat(trapScore);
    }

    public String getPextRatio() {
        return FormatManager.getFloat(pextRatio);
    }

    public String getMgi() {
        return mgiStr;
    }

    public StringJoiner getDenovoDBStringJoiner() {
        return denovoDB.getStringJoiner();
    }

    public String getDiscovEHR() {
        return discovEHR.toString();
    }

    public String getMTR() {
        if (mtr != null) {
            return mtr.toString();
        } else {
            return "NA,NA,NA";
        }
    }

    public String getMPC() {
        return FormatManager.getFloat(mpc);
    }

    public String getGME() {
        return FormatManager.getFloat(gmeAF);
    }
    
    public String getTopMed() {
        return FormatManager.getFloat(topmedAF);
    }
    
    public String getGenomeAsia() {
        return FormatManager.getFloat(genomeasiaAF);
    }
}
