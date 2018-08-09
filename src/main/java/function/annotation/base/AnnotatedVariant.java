package function.annotation.base;

import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBRGene;
import function.external.limbr.LIMBROutput;
import function.external.denovo.DenovoDB;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHR;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.Evs;
import function.external.evs.EvsCommand;
import function.external.exac.Exac;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADCommand;
import function.external.genomes.Genomes;
import function.external.genomes.GenomesCommand;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gnomad.GnomADGenome;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import function.external.kaviar.Kaviar;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarOutput;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mtr.MTR;
import function.external.mtr.MTRCommand;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisOutput;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import global.Data;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class AnnotatedVariant extends Variant {

    // AnnoDB annotations / most damaging effect annotations
    private int stableId;
    private String effect = "";
    private String HGVS_c = "";
    private String HGVS_p = "";
    private float polyphenHumdiv = Data.FLOAT_NA;
    private float polyphenHumdivCCDS = Data.FLOAT_NA;
    private float polyphenHumvar = Data.FLOAT_NA;
    private float polyphenHumvarCCDS = Data.FLOAT_NA;
    private boolean hasCCDS = false;
    private String geneName = "";

    private HashSet<String> geneSet = new HashSet<>();
    private StringBuilder allGeneTranscriptSB = new StringBuilder();

    // external db annotations
    private Exac exac;
    private String exacGeneVariantCountStr;
    private GnomADExome gnomADExome;
    private GnomADGenome gnomADGenome;
    private Kaviar kaviar;
    private Evs evs;
    private float gerpScore;
    private float trapScore;
    private KnownVarOutput knownVarOutput;
    private String rvisStr;
    private SubRvisOutput subRvisOutput;
    private LIMBROutput limbrOutput;
    private Genomes genomes;
    private String mgiStr;
    private DenovoDB denovoDB;
    private DiscovEHR discovEHR;
    private MTR mtr;

    public boolean isValid = true;

    public AnnotatedVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        checkValid();
    }

    private void checkValid() throws Exception {
        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid && GnomADCommand.isIncludeGnomADExome) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADExome.isValid();
        }

        if (isValid && GnomADCommand.isIncludeGnomADGenome) {
            gnomADGenome = new GnomADGenome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADGenome.isValid();
        }

        if (isValid && ExacCommand.isIncludeExac) {
            exac = new Exac(chrStr, startPosition, refAllele, allele);

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
    }

    public void update(Annotation annotation) {
        if (isValid) {
            if (effect.isEmpty()) { // init most damaging effect annotations
                stableId = annotation.stableId;
                effect = annotation.effect;
                HGVS_c = annotation.HGVS_c;
                HGVS_p = annotation.HGVS_p;
                geneName = annotation.geneName;
            } else {
                allGeneTranscriptSB.append(";");
            }

            allGeneTranscriptSB
                    .append(annotation.effect).append("|")
                    .append(annotation.geneName).append("|")
                    .append(annotation.stableId).append("|")
                    .append(annotation.HGVS_p).append("|")
                    .append(PolyphenManager.getPrediction(annotation.polyphenHumdiv, annotation.effect)).append("|")
                    .append(PolyphenManager.getPrediction(annotation.polyphenHumvar, annotation.effect));

            polyphenHumdiv = MathManager.max(polyphenHumdiv, annotation.polyphenHumdiv);
            polyphenHumvar = MathManager.max(polyphenHumvar, annotation.polyphenHumvar);

            if (annotation.isCCDS) {
                polyphenHumdivCCDS = MathManager.max(polyphenHumdivCCDS, annotation.polyphenHumdivCCDS);
                polyphenHumvarCCDS = MathManager.max(polyphenHumvarCCDS, annotation.polyphenHumvarCCDS);

                hasCCDS = true;
            }

            geneSet.add(annotation.geneName);
        }
    }

    public void initExternalData() {
        if (KnownVarCommand.isIncludeKnownVar) {
            knownVarOutput = new KnownVarOutput(this);
        }

        if (RvisCommand.isIncludeRvis) {
            rvisStr = RvisManager.getLine(getGeneName());
        }

        if (MgiCommand.isIncludeMgi) {
            mgiStr = MgiManager.getLine(getGeneName());
        }

        if (DenovoDBCommand.isIncludeDenovoDB) {
            denovoDB = new DenovoDB(chrStr, startPosition, refAllele, allele);
        }

        if (ExacCommand.isIncludeExacGeneVariantCount) {
            exacGeneVariantCountStr = ExacManager.getLine(getGeneName());
        }

        if (TrapCommand.isIncludeTrap) {
            trapScore = isIndel() ? Data.FLOAT_NA : TrapManager.getScore(chrStr, getStartPosition(), allele, geneName);
        }
    }

    public boolean isValid() {
        return isValid
                && isSubRVISValid()
                && isLIMBRValid()
                && isMTRValid();
    }

    // init sub rvis score base on most damaging gene and applied filter
    private boolean isSubRVISValid() {
        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(getGeneName(), getChrStr(), getStartPosition());

            // sub rvis filters will only apply missense variants except gene boundary option at domain level used
            if (effect.startsWith("missense_variant") || GeneManager.hasGeneDomainInput()) {
                return SubRvisCommand.isSubRVISDomainScoreValid(subRvisOutput.getDomainScore())
                        && SubRvisCommand.isSubRVISDomainOEratioValid(subRvisOutput.getDomainOEratio())
                        && SubRvisCommand.isSubRVISExonScoreValid(subRvisOutput.getExonScore())
                        && SubRvisCommand.isSubRVISExonOEratioValid(subRvisOutput.getExonOEratio());
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
                LIMBRGene geneDomain = limbrOutput.getGeneDomain();
                LIMBRGene geneExon = limbrOutput.getGeneExon();

                return LIMBRCommand.isLIMBRDomainPercentileValid(geneDomain == null ? Data.FLOAT_NA : geneDomain.getPercentiles())
                        && LIMBRCommand.isLIMBRExonPercentileValid(geneExon == null ? Data.FLOAT_NA : geneExon.getPercentiles());
            } else {
                return true;
            }
        }

        return true;
    }

    // init MTR score based on most damaging transcript and applied filter
    private boolean isMTRValid() {
        if (MTRCommand.isIncludeMTR) {
            // MTR filters will only apply missense variants
            if (effect.startsWith("missense_variant")) {
                mtr = new MTR(chrStr, startPosition, getStableId());

                return mtr.isValid();
            }
        }

        return true;
    }

    public void getAnnotationData(StringJoiner sj) {
        sj.add(getStableId());
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
        sj.add(allGeneTranscriptSB.toString());
    }

    public String getStableId() {
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

    public HashSet<String> getGeneSet() {
        return geneSet;
    }

    public void getExternalData(StringJoiner sj) {
        if (EvsCommand.isIncludeEvs) {
            sj.merge(getEvsStringJoiner());
        }

        if (ExacCommand.isIncludeExac) {
            sj.merge(getExacStringJoiner());
        }

        if (ExacCommand.isIncludeExacGeneVariantCount) {
            sj.add(getExacGeneVariantCount());
        }

        if (GnomADCommand.isIncludeGnomADExome) {
            sj.merge(getGnomADExomeStringJoiner());
        }

        if (GnomADCommand.isIncludeGnomADGenome) {
            sj.merge(getGnomADGenomeStringJoiner());
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
        return rvisStr;
    }

    public StringJoiner getSubRvisStringJoiner() {
        return subRvisOutput.getStringJoiner();
    }

    public StringJoiner getLIMBRStringJoiner() {
        return limbrOutput.getStringJoiner();
    }

    public String getGerpScore() {
        return FormatManager.getFloat(gerpScore);
    }

    public String getTrapScore() {
        return FormatManager.getFloat(trapScore);
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
}
