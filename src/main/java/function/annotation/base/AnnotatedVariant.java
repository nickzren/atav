package function.annotation.base;

import function.external.bis.BisCommand;
import function.external.bis.BisGene;
import function.external.bis.BisOutput;
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
    private BisOutput bisOutput;
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

                polyphenHumdiv = polyphenHumdivCCDS;
                polyphenHumvar = polyphenHumvarCCDS;

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
    }

    public boolean isValid() {
        return isValid
                && PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                && PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar)
                && isTrapValid()
                && isSubRVISValid()
                && isBisValid()
                && isMTRValid();
    }

    private boolean isTrapValid() {
        if (TrapCommand.isIncludeTrap) {
            if (isIndel()) {
                trapScore = Data.FLOAT_NA;
            } else {
                trapScore = TrapManager.getScore(chrStr, getStartPosition(), allele, geneName);
            }

            if (effect.equals("missense_variant")
                    || effect.equals("intron_variant")) {
                // filter only apply to missense_variant and intron_variant variants
                return TrapCommand.isTrapScoreValid(trapScore);
            }
        }

        return true;
    }

    // init sub rvis score base on most damaging gene and applied filter
    private boolean isSubRVISValid() {
        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(getGeneName(), getChrStr(), getStartPosition());

            // sub rvis filters will only apply missense variants except gene boundary option at domain level used
            if (effect.equals("missense_variant") || GeneManager.hasGeneDomainInput()) {
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

    // init bis score base on most damaging gene and applied filter
    private boolean isBisValid() {
        if (BisCommand.isIncludeBis) {
            bisOutput = new BisOutput(getGeneName(), getChrStr(), getStartPosition());

            // bis filters will only apply missense variants except gene boundary option at domain level used
            if (effect.equals("missense_variant") || GeneManager.hasGeneDomainInput()) {
                BisGene geneExon = bisOutput.getGeneExon();

                return BisCommand.isBisExonPercentileValid(geneExon == null ? Data.FLOAT_NA : geneExon.getPercentiles());
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
            if (effect.equals("missense_variant")) {
                mtr = new MTR(chrStr, startPosition, getStableId());

                return mtr.isValid();
            }
        }

        return true;
    }

    public void getAnnotationData(StringBuilder sb) {
        sb.append(getStableId()).append(",");
        sb.append(hasCCDS).append(",");
        sb.append(effect).append(",");
        sb.append(HGVS_c).append(",");
        sb.append(HGVS_p).append(",");
        sb.append(FormatManager.getFloat(polyphenHumdiv)).append(",");
        sb.append(PolyphenManager.getPrediction(polyphenHumdiv, effect)).append(",");
        sb.append(FormatManager.getFloat(polyphenHumvar)).append(",");
        sb.append(PolyphenManager.getPrediction(polyphenHumvar, effect)).append(",");
        sb.append("'").append(geneName).append("'").append(",");
        sb.append(allGeneTranscriptSB.toString()).append(",");
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

    public String getPolyphenHumdivScore() {
        return FormatManager.getFloat(polyphenHumdiv);
    }

    public String getPolyphenHumvarScore() {
        return FormatManager.getFloat(polyphenHumvar);
    }

    public String getPolyphenHumdivPrediction() {
        return PolyphenManager.getPrediction(polyphenHumdiv, effect);
    }

    public String getPolyphenHumvarPrediction() {
        return PolyphenManager.getPrediction(polyphenHumvar, effect);
    }

    public String getGeneName() {
        return geneName;
    }

    public HashSet<String> getGeneSet() {
        return geneSet;
    }

    public void getExternalData(StringBuilder sb) {
        sb.append(getEvsStr());
        sb.append(getExacStr());
        sb.append(getExacGeneVariantCount());
        sb.append(getGnomADExomeStr());
        sb.append(getGnomADGenomeStr());
        sb.append(getKnownVarStr());
        sb.append(getKaviarStr());
        sb.append(get1000Genomes());
        sb.append(getRvis());
        sb.append(getSubRvis());
        sb.append(getBis());
        sb.append(getGerpScore());
        sb.append(getTrapScore());
        sb.append(getMgi());
        sb.append(getDenovoDB());
        sb.append(getDiscovEHR());
        sb.append(getMTR());
    }

    public String getExacStr() {
        if (ExacCommand.isIncludeExac) {
            return exac.toString();
        } else {
            return "";
        }
    }

    public String getGnomADExomeStr() {
        if (GnomADCommand.isIncludeGnomADExome) {
            return gnomADExome.toString();
        } else {
            return "";
        }
    }

    public String getGnomADGenomeStr() {
        if (GnomADCommand.isIncludeGnomADGenome) {
            return gnomADGenome.toString();
        } else {
            return "";
        }
    }

    public String getKaviarStr() {
        if (KaviarCommand.isIncludeKaviar) {
            return kaviar.toString();
        } else {
            return "";
        }
    }

    public String getEvsStr() {
        if (EvsCommand.isIncludeEvs) {
            return evs.toString();
        } else {
            return "";
        }
    }

    public String getKnownVarStr() {
        if (KnownVarCommand.isIncludeKnownVar) {
            return knownVarOutput.toString();
        } else {
            return "";
        }
    }

    public String getGerpScore() {
        if (GerpCommand.isIncludeGerp) {
            return FormatManager.getFloat(gerpScore) + ",";
        } else {
            return "";
        }
    }

    public String getTrapScore() {
        if (TrapCommand.isIncludeTrap) {
            return FormatManager.getFloat(trapScore) + ",";
        } else {
            return "";
        }
    }

    public String getRvis() {
        if (RvisCommand.isIncludeRvis) {
            return rvisStr;
        } else {
            return "";
        }
    }

    public String getSubRvis() {
        if (SubRvisCommand.isIncludeSubRvis) {
            return subRvisOutput.toString();
        } else {
            return "";
        }
    }

    public String getBis() {
        if (BisCommand.isIncludeBis) {
            return bisOutput.toString();
        } else {
            return "";
        }
    }

    public String get1000Genomes() {
        if (GenomesCommand.isInclude1000Genomes) {
            return genomes.toString();
        } else {
            return "";
        }
    }

    public String getMgi() {
        if (MgiCommand.isIncludeMgi) {
            return mgiStr;
        } else {
            return "";
        }
    }

    public String getDenovoDB() {
        if (DenovoDBCommand.isIncludeDenovoDB) {
            return denovoDB.toString();
        } else {
            return "";
        }
    }

    public String getDiscovEHR() {
        if (DiscovEHRCommand.isIncludeDiscovEHR) {
            return discovEHR.toString();
        } else {
            return "";
        }
    }

    public String getExacGeneVariantCount() {
        if (ExacCommand.isIncludeExacGeneVariantCount) {
            return exacGeneVariantCountStr;
        } else {
            return "";
        }
    }

    public String getMTR() {
        if (MTRCommand.isIncludeMTR) {
            if (mtr != null) {
                return mtr.toString();
            } else {
                return "NA,NA,NA,";
            }
        } else {
            return "";
        }
    }
}
