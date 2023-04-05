package function.annotation.base;

import function.cohort.base.Carrier;
import function.cohort.singleton.SingletonCommand;
import function.cohort.trio.TrioCommand;
import function.cohort.vcf.VCFCommand;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCROutput;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.dbnsfp.DBNSFP;
import function.external.dbnsfp.DBNSFPCommand;
import function.external.dbnsfp.DBNSFPManager;
import function.external.defaultcontrolaf.DefaultControl;
import function.external.defaultcontrolaf.DefaultControlCommand;
import function.external.defaultcontrolaf.DefaultControlManager;
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
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADCommand;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gevir.GeVIRCommand;
import function.external.gevir.GeVIRManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGene;
import function.external.gnomad.GnomADGenome;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.gnomad.GnomADManager;
import function.external.igmaf.IGMAF;
import function.external.igmaf.IGMAFCommand;
import function.external.igmaf.IGMAFManager;
import function.external.iranome.IranomeCommand;
import function.external.iranome.IranomeManager;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
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
import function.external.revel.RevelCommand;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisOutput;
import function.external.synrvis.SynRvisCommand;
import function.external.synrvis.SynRvisManager;
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import function.variant.base.VariantLevelFilterCommand;
import global.Data;
import global.Index;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class AnnotatedVariant extends Variant {

    // annotations / most damaging effect annotations
    private int stableId;
    private String impact = "";
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

    private List<Integer> canonicalEffectIdList = new ArrayList<>();

    private HashSet<Integer> transcriptSet = new HashSet<>();
    private Map<String, Integer> geneTranscriptCountMap = new LinkedHashMap<>();
    private StringJoiner allAnnotationSJ = new StringJoiner(",");

    // external db annotations
    private ExAC exac;
    private GnomADExome gnomADExome;
    private GnomADGenome gnomADGenome;
    private GnomADGene gnomADGene;
    private Evs evs;
    private float gerpScore;
    private float trapScore;
    private float pextRatio;
    public KnownVarOutput knownVarOutput;
    private SubRvisOutput subRvisOutput;
    private LIMBROutput limbrOutput;
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
    private float iranomeAF;
    private IGMAF igmAF;
    private DefaultControl defaultControl;
    private DBNSFP dbNSFP;
    private byte ttnLowPSI;

    public boolean isValid = true;

    public AnnotatedVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        checkValid();
    }

    private void checkValid() throws Exception {
        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid && KnownVarCommand.isInclude) {
            knownVarOutput = new KnownVarOutput(this);
            isValid = knownVarOutput.isExcludeClinVarBLB();
        }

        if (isValid && CHMCommand.isFlag) {
            isRepeatRegion = CHMManager.isRepeatRegion(chrStr, startPosition);

            if (CHMCommand.isExclude) {
                isValid = !isRepeatRegion; // invalid when variant's repeat region is true
            }
        }

        if (isValid && IGMAFCommand.getInstance().isInclude) {
            igmAF = IGMAFManager.getIGMAF(chrStr, variantId);

            isValid = IGMAFCommand.getInstance().isAFValid(igmAF.getAF(), getKnownVar());
        }

        if (isValid && DefaultControlCommand.getInstance().isInclude) {
            defaultControl = DefaultControlManager.getDefaultControlAF(chrStr, variantId);

            isValid = DefaultControlCommand.getInstance().isAFValid(defaultControl.getAF(), getKnownVar());
        }

        if (isValid && GMECommand.getInstance().isInclude) {
            gmeAF = GMEManager.getAF(variantIdStr);

            isValid = GMECommand.getInstance().isAFValid(gmeAF, getKnownVar());
        }

        if (isValid && IranomeCommand.getInstance().isInclude) {
            iranomeAF = IranomeManager.getAF(variantIdStr);

            isValid = IranomeCommand.getInstance().isAFValid(iranomeAF, getKnownVar());
        }

        if (isValid && TopMedCommand.getInstance().isInclude) {
            topmedAF = TopMedManager.getAF(variantIdStr);

            isValid = TopMedCommand.getInstance().isAFValid(topmedAF, getKnownVar());
        }

        if (isValid && GenomeAsiaCommand.getInstance().isInclude) {
            genomeasiaAF = GenomeAsiaManager.getAF(variantIdStr);

            isValid = GenomeAsiaCommand.getInstance().isAFValid(genomeasiaAF, getKnownVar());
        }

        if (isValid && GnomADExomeCommand.getInstance().isInclude) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADExome.isValid(getKnownVar());
        }

        if (isValid && GnomADGenomeCommand.getInstance().isInclude) {
            gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADGenome.isValid(getKnownVar());
        }

        if (isValid && ExACCommand.getInstance().isInclude) {
            exac = new ExAC(chrStr, startPosition, refAllele, allele);

            isValid = exac.isValid(getKnownVar());
        }

        if (isValid && EvsCommand.isInclude) {
            evs = new Evs(chrStr, startPosition, refAllele, allele);

            isValid = evs.isValid(getKnownVar());
        }

        if (isValid && GerpCommand.isInclude) {
            gerpScore = GerpManager.getScore(chrStr, startPosition, refAllele, allele);

            isValid = GerpCommand.isGerpScoreValid(gerpScore);
        }

        if (isValid && DiscovEHRCommand.isInclude) {
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
                impact = annotation.impact;
                effect = annotation.effect;
                effectID = annotation.effectID;
                HGVS_c = annotation.HGVS_c;
                HGVS_p = annotation.HGVS_p;
                geneName = annotation.geneName;

                // only need to init once per variant
                revel = annotation.revel;
                primateAI = annotation.primateAI;

                if (isValid && VariantLevelFilterCommand.isIncludeTTNLowPSI) {
                    ttnLowPSI = GeneManager.getTTNLowPSI(geneName, effectID, startPosition);

                    isValid = GeneManager.isTTNPSIValid(ttnLowPSI);
                }
            }

            StringJoiner annotationSJ = new StringJoiner("|");
            annotationSJ.add(annotation.effect);
            annotationSJ.add(annotation.geneName);
            annotationSJ.add(getStableId(annotation.stableId));
            annotationSJ.add(annotation.HGVS_c);
            annotationSJ.add(FormatManager.getString(annotation.HGVS_p));
            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumdiv));
            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumvar));

            int geneTranscriptCount = geneTranscriptCountMap.getOrDefault(annotation.geneName, 0);
            if (!transcriptSet.contains(annotation.stableId)) {
                transcriptSet.add(annotation.stableId);
                geneTranscriptCountMap.put(annotation.geneName, geneTranscriptCount + 1);
            }

            allAnnotationSJ.add(annotationSJ.toString());

            polyphenHumdiv = MathManager.max(polyphenHumdiv, annotation.polyphenHumdiv);
            polyphenHumvar = MathManager.max(polyphenHumvar, annotation.polyphenHumvar);

            if (annotation.isCCDS) {
                polyphenHumdivCCDS = MathManager.max(polyphenHumdivCCDS, annotation.polyphenHumdivCCDS);
                polyphenHumvarCCDS = MathManager.max(polyphenHumvarCCDS, annotation.polyphenHumvarCCDS);

                hasCCDS = true;
            }

            if (!canonicalEffectIdList.contains(annotation.effectID)
                    && TranscriptManager.isCanonicalTranscript(chrStr, annotation.stableId)) {
                canonicalEffectIdList.add(annotation.effectID);
            }
        }
    }

    public String getAllAnnotation() {
        return allAnnotationSJ.toString();
    }

    public void initExternalData() {
        if (KnownVarCommand.isInclude) {
            knownVarOutput.init(getGeneName());
        }

        if (MgiCommand.isInclude) {
            mgiStr = MgiManager.getLine(getGeneName());
        }

        if (DenovoDBCommand.isInclude) {
            denovoDB = new DenovoDB(chrStr, startPosition, refAllele, allele);
        }

        if (DBNSFPCommand.isInclude) {
            dbNSFP = DBNSFPManager.getDBNSFP(chrStr, startPosition, allele, isSNV(), transcriptSet);
        }

        if (GnomADCommand.isIncludeGeneMetrics) {
            gnomADGene = GnomADManager.getGnomADGene(geneName);
        }
    }

    public boolean isValid() {
        return isValid
                && isImpactValid4TrioOrSingleton()
                && isSubRVISValid()
                && isLIMBRValid()
                && isTrapValid()
                && isCCRValid()
                && isMTRValid()
                && isPextValid()
                && RevelCommand.isValid(revel, effect)
                && PrimateAICommand.isValid(primateAI, effect)
                && isMPCValid();
    }

    // Trio or Singleton analysis only
    // MODIFIER and Known variant site
    private boolean isImpactValid4TrioOrSingleton() {
        if ((SingletonCommand.isList || TrioCommand.isList)
                && knownVarOutput != null
                && AnnotationLevelFilterCommand.isModifierOnly) {
            return impact.equals("MODIFIER")
                    && knownVarOutput.isKnownVariant();
        }

        return true;
    }

    // init sub rvis score base on most damaging gene and applied filter
    private boolean isSubRVISValid() {
        if (SubRvisCommand.isInclude) {
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
        if (LIMBRCommand.isInclude) {
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
        if (CCRCommand.isInclude) {
            ccrOutput = new CCROutput(geneTranscriptCountMap.keySet(), getChrStr(), getStartPosition());

            if (!EffectManager.isLOF(effectID)) {
                return ccrOutput.isValid();
            }
        }

        return true;
    }

    // init MTR score based on most damaging transcript and applied filter
    private boolean isMTRValid() {
        if (MTRCommand.isInclude) {
            mtr = new MTR(chrStr, startPosition);

            // MTR filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                return mtr.isValid();
            }
        }

        return true;
    }

    // init Trap score and applied filter 
    private boolean isTrapValid() {
        if (TrapCommand.isInclude) {
            trapScore = isIndel() ? Data.FLOAT_NA : TrapManager.getScore(chrStr, startPosition, allele, isMNV(), geneName);

            return TrapCommand.isValid(trapScore, effect);
        }

        return true;
    }

    // init PEXT score and applied filter 
    private boolean isPextValid() {
        if (PextCommand.isInclude) {
            pextRatio = PextManager.getRatio(chrStr, getStartPosition());

            return PextCommand.isPextRatioValid(pextRatio);
        }

        return true;
    }

    // init MPC score base on most damaging gene and applied filter
    private boolean isMPCValid() {
        if (MPCCommand.isInclude) {
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
        if (!SingletonCommand.isList && !TrioCommand.isList) {
            sj.add(impact);
            sj.add(effect);
            sj.add(getCanonicalEffect());
            sj.add("'" + geneName + "'");
        }
        sj.add("'" + GeneManager.getUpToDateGene(geneName) + "'");
        sj.add(GeneManager.getAllGeneSymbol(geneTranscriptCountMap.keySet()));
        sj.add(GeneManager.getAllGeneTranscriptCount(geneTranscriptCountMap));
        sj.add(getStableId(stableId));
        sj.add(Boolean.toString(hasCCDS));
        sj.add(HGVS_c);
        sj.add(HGVS_p);
        sj.add(FormatManager.getFloat(polyphenHumdiv));
        sj.add(PolyphenManager.getPrediction(polyphenHumdiv, effect));
        sj.add(FormatManager.getFloat(polyphenHumvar));
        sj.add(PolyphenManager.getPrediction(polyphenHumvar, effect));
        sj.add(FormatManager.appendDoubleQuote(getAllAnnotation()));
    }

    public String getImpact() {
        return impact;
    }

    public String getStableId() {
        return getStableId(stableId);
    }

    public String getGeneLink() {
        // "=HYPERLINK(""url"",""name"")"
        if (knownVarOutput != null && knownVarOutput.isOMIMGene()) {
            return "\"=HYPERLINK(\"\"https://omim.org/search?search=" + geneName + "\"\",\"\"OMIM\"\")\"";
        } else {
            return "\"=HYPERLINK(\"\"https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + geneName + "\"\",\"\"GeneCards\"\")\"";
        }
    }

    public String getCanonicalEffect() {
        StringJoiner sj = new StringJoiner("|");
        for (int id : canonicalEffectIdList) {
            sj.add(EffectManager.getEffectById(id));
        }

        return sj.setEmptyValue(Data.STRING_NA).toString();
    }

    private String getStableId(int stableId) {
        if (stableId == Data.INTEGER_NA) {
            if (VCFCommand.isOutputVCF) {
                return Data.VCF_NA;
            }

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

    public Set<String> getGeneSet() {
        return geneTranscriptCountMap.keySet();
    }

    public HashSet<Integer> getTranscriptSet() {
        return transcriptSet;
    }

    public void getExternalData(StringJoiner sj) {
        if (KnownVarCommand.isInclude) {
            sj.merge(getKnownVarStringJoiner());
        }

        if (EvsCommand.isInclude) {
            sj.merge(getEvsStringJoiner());
        }

        if (ExACCommand.getInstance().isInclude) {
            sj.merge(getExacStringJoiner());
        }

        if (GnomADExomeCommand.getInstance().isInclude) {
            sj.merge(getGnomADExomeStringJoiner());
        }

        if (GnomADGenomeCommand.getInstance().isInclude) {
            sj.merge(getGnomADGenomeStringJoiner());
        }

        if (GnomADCommand.isIncludeGeneMetrics) {
            sj.merge(getGeneMetrics());
        }

        if (RvisCommand.isInclude) {
            sj.add(getRvis());
        }

        if (SubRvisCommand.isInclude) {
            sj.merge(getSubRvisStringJoiner());
        }

        if (GeVIRCommand.isInclude) {
            sj.add(getGeVIR());
        }

        if (SynRvisCommand.isInclude) {
            sj.add(getSynRvis());
        }

        if (LIMBRCommand.isInclude) {
            sj.merge(getLIMBRStringJoiner());
        }

        if (CCRCommand.isInclude) {
            sj.merge(getCCRStringJoiner());
        }

        if (GerpCommand.isInclude) {
            sj.add(getGerpScore());
        }

        if (TrapCommand.isInclude) {
            sj.add(getTrapScore());
        }

        if (MgiCommand.isInclude) {
            sj.add(getMgi());
        }

        if (DenovoDBCommand.isInclude) {
            sj.merge(getDenovoDBStringJoiner());
        }

        if (DiscovEHRCommand.isInclude) {
            sj.add(getDiscovEHR());
        }

        if (MTRCommand.isInclude) {
            sj.add(getMTRStr());
        }

        if (RevelCommand.isInclude) {
            sj.add(FormatManager.getFloat(revel));
        }

        if (PrimateAICommand.isInclude) {
            sj.add(FormatManager.getFloat(primateAI));
        }

        if (VariantLevelFilterCommand.isIncludeLOFTEE) {
            sj.add(FormatManager.getBoolean(isLOFTEEHCinCCDS));
        }

        if (MPCCommand.isInclude) {
            sj.add(getMPC());
        }

        if (PextCommand.isInclude) {
            sj.add(getPextRatio());
        }

        if (CHMCommand.isFlag) {
            sj.add(FormatManager.getBoolean(isRepeatRegion));
        }

        if (GMECommand.getInstance().isInclude) {
            sj.add(getGME());
        }

        if (TopMedCommand.getInstance().isInclude) {
            sj.add(getTopMed());
        }

        if (GenomeAsiaCommand.getInstance().isInclude) {
            sj.add(getGenomeAsia());
        }

        if (IranomeCommand.getInstance().isInclude) {
            sj.add(getIranome());
        }

        if (IGMAFCommand.getInstance().isInclude) {
            sj.add(getIGMAF());
        }

        if (DefaultControlCommand.getInstance().isInclude) {
            sj.merge(getDefaultControlStringJoiner());
        }

        if (DBNSFPCommand.isInclude) {
            sj.add(dbNSFP.toString());
        }

        if (VariantLevelFilterCommand.isIncludeTTNLowPSI) {
            sj.add(FormatManager.getByte(ttnLowPSI));
        }
    }

    public StringJoiner getEvsStringJoiner() {
        return evs.getStringJoiner();
    }

    public StringJoiner getExacStringJoiner() {
        return exac.getStringJoiner();
    }

    public StringJoiner getGnomADExomeStringJoiner() {
        return gnomADExome.getStringJoiner();
    }

    public StringJoiner getGnomADGenomeStringJoiner() {
        return gnomADGenome.getStringJoiner();
    }

    public StringJoiner getGeneMetrics() {
        if (gnomADGene == null) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            return sj;
        } else {
            return gnomADGene.getGeneMetricsSJ();
        }
    }

    public StringJoiner getKnownVarStringJoiner() {
        return knownVarOutput.getStringJoiner();
    }

    public String getRvis() {
        return RvisManager.getLine(getGeneName());
    }

    public StringJoiner getSubRvisStringJoiner() {
        return subRvisOutput.getStringJoiner();
    }

    public String getGeVIR() {
        return GeVIRManager.getLine(getGeneName());
    }

    public String getSynRvis() {
        return SynRvisManager.getLine(getGeneName());
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

    public String getMTRStr() {
        if (mtr != null) {
            return mtr.toString();
        } else {
            return "NA,NA,NA";
        }
    }

    public MTR getMTR() {
        return mtr;
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

    public String getIranome() {
        return FormatManager.getFloat(iranomeAF);
    }

    public String getIGMAF() {
        return igmAF.toString();
    }

    public StringJoiner getDefaultControlStringJoiner() {
        return defaultControl.getStringJoiner();
    }

    public DefaultControl getDefaultControl() {
        return defaultControl;
    }

    public GnomADExome getGnomADExome() {
        return gnomADExome;
    }

    public GnomADGenome getGnomADGenome() {
        return gnomADGenome;
    }

    public KnownVarOutput getKnownVar() {
        return knownVarOutput;
    }

    // LoF effects
    public boolean isLOF() {
        return EffectManager.isLOF(effectID);
    }

    // LoF variant and occurs within a ClinGen/OMIM disease gene and genotype is consistent with inheritance
    public boolean isInClinGenOrOMIM(Carrier carrier) {
        if (isLOF()) {
            if (carrier.getGT() == Index.HET) {
                return getKnownVar().isInClinGenSufficientOrSomeEvidence()
                        || getKnownVar().isOMIMDominant();
            } else if (carrier.getGT() == Index.HOM) {
                return getKnownVar().isInClinGenRecessiveEvidence()
                        || getKnownVar().isOMIMRecessive();
            }
        }

        return false;
    }

    // LoF variant and occurs within a ClinVar Pathogenic gene that has pathogenic/likely pathogenic indel or CNV or spice/nonsense SNV
    public boolean isInClinVarPathoratio() {
        return isLOF()
                && knownVarOutput.getClinVarPathoratio().isInClinVarPathoratio();
    }

    // LoF variant in gnomAD LoF depleted genes with pLI >= 0.9
    public boolean isLoFPLIValid() {
        return isLOF() && isPLIValid();
    }

    // LoF variant in gnomAD LoF depleted genes with pREC >= 0.9
    public boolean isLoFPRECValid() {
        return isLOF() && isPRECValid();
    }

    // Missense variant in gnomAD gene with mis_z >= 2
    public boolean isMissenseMisZValid() {
        return isMissense() && isMisZValid();
    }

    public boolean isPLIValid() {
        if (gnomADGene == null) {
            return false;
        } else {
            return gnomADGene.pli >= 0.9;
        }
    }

    public boolean isPRECValid() {
        if (gnomADGene == null) {
            return false;
        } else {
            return gnomADGene.pRec >= 0.9;
        }
    }

    public boolean isMisZValid() {
        if (gnomADGene == null) {
            return false;
        } else {
            return gnomADGene.misZ >= 2;
        }
    }

    public boolean isFDRValid() {
        return mtr.getFDR() != Data.FLOAT_NA
                && mtr.getFDR() < 0.01;
    }

    public boolean isKnownVarSiteValid() {
        return knownVarOutput.isClinVarPLPSite() || knownVarOutput.isHGMDDMSite();
    }
    
    // any variants in 2bp flanking regions either HGMD DM or ClinVar PLP
    public boolean isKnownVar2bpFlankingValid() {
        return knownVarOutput.isKnownVar2bpFlankingValid();
    }

    // missense variant in 25bp flanking regions with >= 6 ClinVar P/LP
    public boolean isClinVar25bpFlankingValid() {
        return isMissense() && knownVarOutput.isClinVar25bpFlankingValid();
    }

    // less than N heterozygous observed from IGM controls + gnomAD (WES & WGS) controls
    public boolean isNHetFromControlsValid(int count) {
        return defaultControl.getControlNHET()
                + gnomADExome.getControlNHET()
                + gnomADGenome.getControlNHET() <= count;
    }

    // less than N homozygous observed from IGM controls + gnomAD (WES & WGS) controls
    public boolean isNHomFromControlsValid(int count) {
        return defaultControl.getNHOM()
                + gnomADExome.getControlNHOM()
                + gnomADGenome.getControlNHOM() <= count;
    }

    // genotype is not observed in Hemizygous or Homozygous from IGM controls and gnomAD (WES & WGS) controls
    public boolean isNotObservedInHomAmongControl() {
        return defaultControl.isNotObservedInControlHemiOrHom()
                && gnomADExome.isNotObservedInControlHemiOrHom()
                && gnomADGenome.isNotObservedInControlHemiOrHom();
    }

    // max 0.5% AF to IGM controls and gnomAD (WES & WGS) controls
    public boolean isControlAFValid() {
        return defaultControl.getAF() < 0.005f
                && gnomADExome.getControlAF() < 0.005f
                && gnomADGenome.getControlAF() < 0.005f;
    }

    // less than 20 alleles observed from IGM controls + gnomAD (WES & WGS) controls
    public boolean isTotalACFromControlsValid() {
        return defaultControl.getAC()
                + gnomADExome.getControlAC()
                + gnomADGenome.getControlAC() < 20;
    }

    // LoF variant or Polyphen Humvar >= 0.95 
    // And meet any of rules below:
    // EdgeCase[EVS] is 'N' and (0.1%RVIS%[EVS] <= 25 or 0.05%_anypopn_RVIS%tile[ExAC] <= 25)
    // EdgeCase[EVS] is 'Y' and (OEratio%tile[EVS] <= 25 or OEratio%tile[ExAC] <= 25) 
    // GenicConstraint[EVS] <= 25
    // GenicConstraint_mis-z%tile[ExAC] <= 25
    public byte isHotZone() {
        if ((isLOF() || polyphenHumvar >= 0.95)
                && RvisManager.isHotZone(geneName)) {
            return 1;
        }

        return 0;
    }

    public boolean isMissense() {
        return EffectManager.isMISSENSE(effectID);
    }

    public boolean isNotSynonymousAndNotSlice() {
        return !isSynonymous() && !isSplice();
    }

    public boolean isNotSynonymousAndNotSliceOrHighTraP() {
        return isNotSynonymousAndNotSlice()
                || trapScore >= 0.4;
    }

    public boolean isSynonymous() {
        return EffectManager.isSYNONYMOUS(effectID);
    }

    public boolean isSplice() {
        return EffectManager.isSPLICE(effectID);
    }

    public boolean isInframe() {
        return EffectManager.isINFRAME(effectID);
    }

    public boolean isStopLost() {
        return EffectManager.isStopLost(effectID);
    }

    // Automated interpretation of ACMG criteria
    // IGM and gnomAD control AF > 5%
    public boolean isBA1() {
        return defaultControl.getAF() > 0.05f
                && gnomADExome.getControlAF() > 0.05f
                && gnomADGenome.getControlAF() > 0.05f;

    }

    // IGM and gnomAD control AF >= 1%
    public boolean isBS1() {
        return defaultControl.getAF() > 0.01f
                && gnomADExome.getControlAF() > 0.01f
                && gnomADGenome.getControlAF() > 0.01f;

    }

    /*
        OMIM Inheritance NA --> do not apply
        OMIM Dominant & Recessive / X-Linked with IGM and gnomAD control HOM/HEMI count >= 3
        OMIM Recessive/X-Linked with IGM and gnomAD control HOM/HEMI count >= 3
        OMIM Dominant with IGM and gnomAD control HET count >= 3
     */
    public boolean isBS2() {
        if (knownVarOutput.getOMIMInheritance().equals(Data.STRING_NA)) {
            return false;
        } else {
            if (knownVarOutput.isOMIMDominant() && knownVarOutput.isOMIMRecessive()) {
                return defaultControl.getNHOM()
                        + gnomADExome.getControlNHOM()
                        + gnomADGenome.getControlNHOM() >= 3;
            } else if (knownVarOutput.isOMIMRecessive()) {
                return defaultControl.getNHOM()
                        + gnomADExome.getControlNHOM()
                        + gnomADGenome.getControlNHOM() >= 3;
            } else if (knownVarOutput.isOMIMDominant()) {
                return defaultControl.getControlNHET()
                        + gnomADExome.getControlNHET()
                        + gnomADGenome.getControlNHET() >= 3;
            } else {
                return false;
            }
        }
    }

    // when effect contains/end with 'synonymous_variant' and TraP < 0.4
    public boolean isBP7() {
        return isSynonymous() && trapScore < 0.4;
    }

    // missense variant and gene fall into intervar bp1_genes list
    public boolean isBP1() {
        return isMissense() && GeneManager.isInterVarBP1Gene(geneName);
    }

    // missense variant in 25bp flanking regions with >= 6 ClinVar P/LP
    public boolean isPM1() {
        return isMissense() && isClinVar25bpFlankingValid();
    }

    // missense variant and gene fall into intervar pp2_gene list
    public boolean isPP2() {
        return isMissense() && GeneManager.isInterVarPP2Gene(geneName);
    }

    // missense variant and (polyphenHumvar < 0.4335 or REVEL < 0.2 or SubRVIS exon/domain centile > 50)
    public boolean isBP4() {
        return isMissense()
                && ((polyphenHumvar < 0.4335 && polyphenHumvar != Data.FLOAT_NA)
                || (revel < 0.2 && revel != Data.FLOAT_NA)
                || subRvisOutput.getExonPercentile() > 50
                || subRvisOutput.getDomainPercentile() > 50);
    }

    // missense variant and (polyphenHumvar >= 0.9035 or REVEL > 0.8 or SubRVIS exon/domain centile < 35 || dbNSFP siftPred is D || dbNSFP polyphen2HDIVPred is D || dbNSFP polyphen2HVARPred is D)
    public boolean isPP3() {
        return isMissense()
                && (polyphenHumvar >= 0.9035
                || revel >= 0.8
                || subRvisOutput.getExonPercentile() <= 35
                || subRvisOutput.getDomainPercentile() <= 35
                || dbNSFP.isValid(stableId));
    }

    // missense variant in 2bp flanking regions either HGMD DM (not ClinVar B/LB) or ClinVar P/LP
    public boolean isPM5() {
        return isMissense() && isKnownVar2bpFlankingValid();
    }

    // In-frame in/del in repeat region
    public boolean isBP3() {
        return isInframe() && isRepeatRegion;
    }

    // in-frame in/del in a non-repeat region or stop-loss variant
    public boolean isPM4() {
        return (isInframe() && !isRepeatRegion) || isStopLost();
    }

    public boolean isRepeatRegion() {
        return isRepeatRegion;
    }

    // Known Pathogenic Variant
    public boolean isPP5() {
        return knownVarOutput.isKnownVariant();
    }

    // ClinVar B/LB
    public boolean isBP6() {
        return knownVarOutput.isClinVarBLB();
    }
}
