package function.annotation.base;

import function.external.evs.Evs;
import function.external.evs.EvsCommand;
import function.external.exac.Exac;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.genomes.Genomes;
import function.external.genomes.GenomesCommand;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.variant.base.Variant;
import function.variant.base.VariantManager;
import function.external.kaviar.Kaviar;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarOutput;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
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
    int stableId;
    String stableIdStr = "";
    String effect = "";
    String HGVS_c = "";
    String HGVS_p = "";
    float polyphenHumdiv;
    float polyphenHumvar;
    String geneName = "";

    HashSet<String> geneSet = new HashSet<>();
    StringBuilder allGeneTranscriptSB = new StringBuilder();

    // external db annotations
    Exac exac;
    Kaviar kaviar;
    Evs evs;
    float gerpScore;
    float trapScore;
    KnownVarOutput knownVarOutput;
    private String rvisStr;
    private SubRvisOutput subRvisOutput;
    Genomes genomes;
    private String mgiStr;

    public boolean isValid = true;

    public AnnotatedVariant(String chr, int variantId, ResultSet rset) throws Exception {
        super(chr, variantId, rset);

        polyphenHumdiv = MathManager.devide(FormatManager.getInt(rset, "polyphen_humdiv"), 1000);
        polyphenHumdiv = MathManager.devide(FormatManager.getInt(rset, "polyphen_humvar"), 1000);

        checkValid();
    }

    private void checkValid() throws Exception {
        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid & ExacCommand.isIncludeExac) {
            exac = new Exac(chrStr, startPosition, refAllele, allele);

            isValid = exac.isValid();
        }

        if (isValid & EvsCommand.isIncludeEvs) {
            evs = new Evs(chrStr, startPosition, refAllele, allele);

            isValid = evs.isValid();
        }

        if (isValid & GerpCommand.isIncludeGerp) {
            gerpScore = GerpManager.getScore(chrStr, startPosition, refAllele, allele);

            isValid = GerpCommand.isGerpScoreValid(gerpScore);
        }

        if (isValid & KaviarCommand.isIncludeKaviar) {
            kaviar = new Kaviar(chrStr, startPosition, refAllele, allele);

            isValid = kaviar.isValid();
        }

        if (isValid & GenomesCommand.isInclude1000Genomes) {
            genomes = new Genomes(chrStr, startPosition, refAllele, allele);

            isValid = genomes.isValid();
        }
    }

    public void update(Annotation annotation) {
        if (isValid) {
            if (effect.isEmpty()) { // init most damaging effect annotations
                stableId = annotation.stableId;
                stableIdStr = annotation.getStableId();
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
                    .append(annotation.getStableId()).append("|")
                    .append(annotation.HGVS_p);

            if (polyphenHumdiv < annotation.polyphenHumdiv) {
                polyphenHumdiv = annotation.polyphenHumdiv;
            }

            if (polyphenHumvar < annotation.polyphenHumvar) {
                polyphenHumvar = annotation.polyphenHumvar;
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

        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(getGeneName(), getChrStr(), getStartPosition());
        }

        if (MgiCommand.isIncludeMgi) {
            mgiStr = MgiManager.getLine(getGeneName());
        }
    }

    public boolean isValid() {
        return isValid
                & PolyphenManager.isValid(polyphenHumdiv, effect, AnnotationLevelFilterCommand.polyphenHumdiv)
                & PolyphenManager.isValid(polyphenHumvar, effect, AnnotationLevelFilterCommand.polyphenHumvar);
//                & isTrapValid();
    }

    private boolean isTrapValid() {
        if (TrapCommand.isIncludeTrap) {
            if (isIndel()) {
                trapScore = Data.NA;
            } else {
                trapScore = TrapManager.getScore(chrStr, getStartPosition(), allele, geneName);
            }

            if (effect.equals("SYNONYMOUS_CODING")
                    || effect.equals("INTRON_EXON_BOUNDARY")
                    || effect.equals("INTRON")) {
                // filter only apply to SYNONYMOUS_CODING, INTRON_EXON_BOUNDARY and INTRONIC variants
                return TrapCommand.isTrapScoreValid(trapScore);
            }
        }

        return true;
    }

    public void getAnnotationData(StringBuilder sb) {
        sb.append(stableIdStr).append(",");
        sb.append(TranscriptManager.isCCDSTranscript(stableIdStr)).append(",");
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
        return stableIdStr;
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

    public String getAllGeneTranscript() {
        return allGeneTranscriptSB.toString();
    }

    public void getExternalData(StringBuilder sb) {
        sb.append(getEvsStr());
        sb.append(getExacStr());
        sb.append(getKnownVarStr());
        sb.append(getKaviarStr());
        sb.append(get1000Genomes());
        sb.append(getRvis());
        sb.append(getSubRvis());
        sb.append(getGerpScore());
        sb.append(getTrapScore());
        sb.append(getMgi());
    }

    public String getExacStr() {
        if (ExacCommand.isIncludeExac) {
            return exac.toString();
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
}
