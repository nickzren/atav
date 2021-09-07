package function.annotation.base;

import function.cohort.base.Carrier;
import function.cohort.singleton.SingletonCommand;
import function.cohort.trio.TrioCommand;
import function.cohort.vcf.VCFCommand;
import function.external.acmg.ACMGCommand;
import function.external.acmg.ACMGManager;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCROutput;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.clingen.ClinGen;
import function.external.clingen.ClinGenCommand;
import function.external.clingen.ClinGenManager;
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
import function.external.gnomad.GnomADGenome;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.gnomad.GnomADManager;
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
import function.external.omim.OMIMCommand;
import function.external.omim.OMIMManager;
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
import function.variant.base.Output;
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
    private Evs evs;
    private float gerpScore;
    private float trapScore;
    private float pextRatio;
    private KnownVarOutput knownVarOutput;
    private ClinGen clinGen;
    private String omimDiseaseName;
    private String omimInheritance;
    private String acmg;
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
    private float igmAF;
    private DefaultControl defaultControl;
    private DBNSFP dbNSFP;

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
            isValid = knownVarOutput.isValid();
        }

        if (isValid && CHMCommand.isExclude) {
            isRepeatRegion = CHMManager.isRepeatRegion(chrStr, startPosition);
            isValid = !isRepeatRegion; // invalid when variant's repeat region is true
        }

        if (isValid && IGMAFCommand.getInstance().isInclude) {
            igmAF = IGMAFManager.getAF(chrStr, variantId);

            isValid = IGMAFCommand.getInstance().isAFValid(igmAF);

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && DefaultControlCommand.getInstance().isInclude) {
            defaultControl = DefaultControlManager.getDefaultControlAF(chrStr, startPosition, refAllele, allele);

            isValid = DefaultControlCommand.getInstance().isAFValid(defaultControl.getAF());

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && GMECommand.getInstance().isInclude) {
            gmeAF = GMEManager.getAF(variantIdStr);

            isValid = GMECommand.getInstance().isAFValid(gmeAF);

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && IranomeCommand.getInstance().isInclude) {
            iranomeAF = IranomeManager.getAF(variantIdStr);

            isValid = IranomeCommand.getInstance().isAFValid(iranomeAF);

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && TopMedCommand.getInstance().isInclude) {
            topmedAF = TopMedManager.getAF(variantIdStr);

            isValid = TopMedCommand.getInstance().isAFValid(topmedAF);

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && GenomeAsiaCommand.getInstance().isInclude) {
            genomeasiaAF = GenomeAsiaManager.getAF(variantIdStr);

            isValid = GenomeAsiaCommand.getInstance().isAFValid(genomeasiaAF);

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && GnomADExomeCommand.getInstance().isInclude) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADExome.isValid();

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && GnomADGenomeCommand.getInstance().isInclude) {
            gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADGenome.isValid();

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && ExACCommand.getInstance().isInclude) {
            exac = new ExAC(chrStr, startPosition, refAllele, allele);

            isValid = exac.isValid();

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
        }

        if (isValid && EvsCommand.isInclude) {
            evs = new Evs(chrStr, startPosition, refAllele, allele);

            isValid = evs.isValid();

            if (SingletonCommand.isList || TrioCommand.isList) {
                isValid = isValid || getKnownVar().hasKnownVariantOnSite();
            }
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
            }

            StringJoiner annotationSJ = new StringJoiner("|");
            annotationSJ.add(annotation.effect);
            annotationSJ.add(annotation.geneName);
            annotationSJ.add(getStableId(annotation.stableId));
            annotationSJ.add(annotation.HGVS_c);
            annotationSJ.add(FormatManager.getString(annotation.HGVS_p));
//            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumdiv));
//            annotationSJ.add(FormatManager.getFloat(annotation.polyphenHumvar));

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
            knownVarOutput.initClinPathoratio(getGeneName());
        }

        if (ClinGenCommand.isInclude) {
            clinGen = ClinGenManager.getClinGen(getGeneName());
        }

        if (OMIMCommand.isInclude) {
            omimDiseaseName = OMIMManager.getOMIM(getGeneName());
            omimInheritance = getOMIMInheritance();
        }

        if (ACMGCommand.isInclude) {
            acmg = ACMGManager.getACMG(getGeneName());
        }

        if (MgiCommand.isInclude) {
            mgiStr = MgiManager.getLine(getGeneName());
        }

        if (DenovoDBCommand.isInclude) {
            denovoDB = new DenovoDB(chrStr, startPosition, refAllele, allele);
        }

        if (DBNSFPCommand.isInclude) {
            dbNSFP = DBNSFPManager.getDBNSFP(chrStr, startPosition, allele, isSnv(), transcriptSet);
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
                    && knownVarOutput.hasKnownVariantOnSite();
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
            // MTR filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                mtr = new MTR(chrStr, startPosition);

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
//        sj.add(Boolean.toString(hasCCDS));
        sj.add(impact);
        sj.add(effect);
        sj.add(getCanonicalEffect());
        sj.add("'" + geneName + "'");
        sj.add(getGeneLink());
        sj.add(getStableId(stableId));
        sj.add(HGVS_c);
        sj.add(HGVS_p);
//        sj.add(FormatManager.getFloat(polyphenHumdiv));
//        sj.add(PolyphenManager.getPrediction(polyphenHumdiv, effect));
//        sj.add(FormatManager.getFloat(polyphenHumdivCCDS));
//        sj.add(PolyphenManager.getPrediction(polyphenHumdivCCDS, effect));
        sj.add(FormatManager.getFloat(polyphenHumvar));
//        sj.add(PolyphenManager.getPrediction(polyphenHumvar, effect));
//        sj.add(FormatManager.getFloat(polyphenHumvarCCDS));
//        sj.add(PolyphenManager.getPrediction(polyphenHumvarCCDS, effect));
//        sj.add("'" + GeneManager.getUpToDateGene(geneName) + "'");
//        sj.add(GeneManager.getAllGeneSymbol(geneTranscriptCountMap.keySet()));
//        sj.add(GeneManager.getAllGeneTranscriptCount(geneTranscriptCountMap));
        sj.add(FormatManager.appendDoubleQuote(getAllAnnotation()));
    }

    private String getGeneLink() {
        // "=HYPERLINK(""url"",""name"")"
        if (isOMIMGene()) {
            return "\"=HYPERLINK(\"\"https://omim.org/search?search=" + geneName + "\"\",\"\"OMIM\"\")\"";
        } else {
            return "\"=HYPERLINK(\"\"https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + geneName + "\"\",\"\"GeneCards\"\")\"";
        }
    }

    private String getCanonicalEffect() {
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

        if (KnownVarCommand.isInclude) {
            sj.merge(getKnownVarStringJoiner());
        }

        if (ClinGenCommand.isInclude) {
            sj.add(getClinGenStr());
        }

        if (OMIMCommand.isInclude) {
            sj.merge(getOMIMStringJoiner());
        }

        if (ACMGCommand.isInclude) {
            sj.add(getACMG());
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
            sj.add(getMTR());
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
            if (isRepeatRegion == null) {
                isRepeatRegion = CHMManager.isRepeatRegion(chrStr, startPosition);
            }

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
            sj.merge(getDefaultControlAFStringJoiner());
        }

        if (DBNSFPCommand.isInclude) {
            sj.add(dbNSFP.toString());
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
        return GnomADManager.getGeneMetrics(getGeneName());
    }

    public StringJoiner getKnownVarStringJoiner() {
        return knownVarOutput.getStringJoiner();
    }

    public String getClinGenStr() {
        return clinGen.toString();
    }

    public StringJoiner getOMIMStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(FormatManager.appendDoubleQuote(omimDiseaseName));
        sj.add(omimInheritance);

        return sj;
    }

    public String getACMG() {
        return acmg;
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

    public String getIranome() {
        return FormatManager.getFloat(iranomeAF);
    }

    public String getIGMAF() {
        return FormatManager.getFloat(igmAF);
    }

    public StringJoiner getDefaultControlAFStringJoiner() {
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

    public ClinGen getClinGen() {
        return clinGen;
    }

    public boolean isLOF() {
        return EffectManager.isLOF(effectID) || trapScore >= 0.676;
    }

    // tier 2 inclusion criteria
    public boolean isMetTier2InclusionCriteria() {
        return knownVarOutput.hasKnownVariantOnSite()
                || isKnownVar10bpFlankingValid()
                || isInClinGenOrOMIM()
                || isInClinVarPathoratio()
                || isGnomADGenePLIValid()
                || isGeneMisZValid();
    }

    // LoF variant and occurs within a ClinGen/OMIM disease gene
    private boolean isInClinGenOrOMIM() {
        return isLOF()
                && (clinGen.isInClinGen() || isOMIMGene());
    }

    // LoF variant and occurs within a ClinVar Pathogenic gene that has pathogenic/likely pathogenic indel or CNV or spice/nonsense SNV
    private boolean isInClinVarPathoratio() {
        return isLOF()
                && knownVarOutput.getClinVarPathoratio().isInClinVarPathoratio();
    }

    // LoF variant in gnomAD LoF depleted genes with pLI >= 0.9
    private boolean isGnomADGenePLIValid() {
        return isLOF()
                && GnomADManager.isGenePLIValid(geneName);
    }

    // Missense variant in gnomAD gene with mis_z >= 2
    private boolean isGeneMisZValid() {
        return effect.startsWith("missense_variant")
                && GnomADManager.isGeneMisZValid(geneName);
    }

    // any variants in 10bp flanking regions either HGMD DM or ClinVar PLP
    public boolean isKnownVar10bpFlankingValid() {
        return knownVarOutput.isKnownVar10bpFlankingValid();
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

    public boolean isOMIMGene() {
        return omimInheritance.contains("AD")
                || omimInheritance.contains("XLD")
                || omimInheritance.contains("PD")
                || omimInheritance.contains("DD")
                || omimInheritance.contains("SMo")
                || omimInheritance.contains("SMu")
                || omimInheritance.contains("AR")
                || omimInheritance.contains("PR")
                || omimInheritance.contains("DR")
                || omimInheritance.contains("XLR")
                || omimInheritance.contains("XL")
                || omimInheritance.contains("YL");
    }

    public boolean isOMIMDominant() {
        return omimInheritance.contains("AD")
                || omimInheritance.contains("XLD")
                || omimInheritance.contains("PD")
                || omimInheritance.contains("DD")
                || omimInheritance.contains("SMo")
                || omimInheritance.contains("SMu");
    }

    public String getOMIMInheritance() {
        StringJoiner sj = new StringJoiner("|");

        if (omimDiseaseName.contains("Autosomal dominant")) {
            sj.add("AD");
        }

        if (omimDiseaseName.contains("Autosomal recessive")) {
            sj.add("AR");
        }

        if (omimDiseaseName.contains("Pseudoautosomal dominant")) {
            sj.add("PD");
        }

        if (omimDiseaseName.contains("Pseudoautosomal recessive")) {
            sj.add("PR");
        }

        if (omimDiseaseName.contains("Digenic dominant")) {
            sj.add("DD");
        }

        if (omimDiseaseName.contains("Digenic recessive")) {
            sj.add("DR");
        }

        if (omimDiseaseName.contains("Isolated cases")) {
            sj.add("IC");
        }

        if (omimDiseaseName.contains("Inherited chromosomal imbalance")) {
            sj.add("ICB");
        }

        if (omimDiseaseName.contains("Mitochondrial")) {
            sj.add("Mi");
        }

        if (omimDiseaseName.contains("Multifactorial")) {
            sj.add("Mu");
        }

        if (omimDiseaseName.contains("Somatic mosaicism")) {
            sj.add("SMo");
        }

        if (omimDiseaseName.contains("Somatic mutation")) {
            sj.add("SMu");
        }

        if (omimDiseaseName.contains("X-linked")) {
            sj.add("XL");
        }

        if (omimDiseaseName.contains("X-linked dominant")) {
            sj.add("XLD");
        }

        if (omimDiseaseName.contains("X-linked recessive")) {
            sj.add("XLR");
        }

        if (omimDiseaseName.contains("Y-linked")) {
            sj.add("YL");
        }

        if (sj.length() == 0) {
            return Data.STRING_NA;
        }

        return sj.toString();
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
            Output.hotZoneVarCount++;
            return 1;
        }

        return 0;
    }

    // High or Moderate impacts or TraP >= 0.4 or HGMD DM? or ClinVar P/LP
    public boolean isImpactHighOrModerate() {
        return impact.equals("HIGH")
                || impact.equals("MODERATE")
                || trapScore >= 0.4
                || knownVarOutput.hasKnownVariantOnSite();
    }

    public boolean isMissense() {
        return effect.startsWith("missense_variant");
    }
}
