package function.annotation.base;

import function.external.denovo.DenovoDB;
import function.external.denovo.DenovoDBCommand;
import function.external.evs.Evs;
import function.external.evs.EvsCommand;
import function.external.exac.Exac;
import function.external.exac.ExacCommand;
import function.external.gnomad.GnomADExome;
import function.external.gnomad.GnomADCommand;
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
import function.variant.base.VariantLevelFilterCommand;
import global.Data;
import utils.FormatManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author nick
 */
public class AnnotatedVariant extends Variant {

    // AnnoDB annotations
    private String function;
    private String geneName;
    private String codonChange;
    private String aminoAcidChange;
    private String stableId;
    private HashSet<String> geneSet = new HashSet<>();
    private HashSet<String> transcriptSet = new HashSet<>();
    private float polyphenHumdiv;
    private float polyphenHumdivCCDS;
    private float polyphenHumvar;
    private float polyphenHumvarCCDS;
    private boolean hasCCDS;

    // external db annotations
    private Exac exac;
    private GnomADExome gnomADExome;
    private Kaviar kaviar;
    private Evs evs;
    private float gerpScore;
    private float trapScore;
    private KnownVarOutput knownVarOutput;
    private String rvisStr;
    private SubRvisOutput subRvisOutput;
    private Genomes genomes;
    private String mgiStr;
    private DenovoDB denovoDB;

    public boolean isValid = true; // at variant level

    public AnnotatedVariant(int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(variantId, isIndel, rset);

        function = "";
        geneName = "";
        codonChange = "";
        aminoAcidChange = "";
        stableId = "";
        hasCCDS = false;

        polyphenHumdivCCDS = Data.NA;
        polyphenHumvarCCDS = Data.NA;
        polyphenHumdiv = Data.NA;
        polyphenHumvar = Data.NA;

        checkValid();
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

        if (DenovoDBCommand.isIncludeDenovoDB) {
            denovoDB = new DenovoDB(chrStr, startPosition, refAllele, allele);
        }
    }

    public void update(Annotation annotation) {
        if (isValid) {
            geneSet.add(annotation.geneName);

            if (function.isEmpty()
                    || FunctionManager.isMoreDamage(annotation.function, function)) {
                function = annotation.function;
                geneName = annotation.geneName;
                codonChange = annotation.codonChange;
                aminoAcidChange = annotation.aminoAcidChange;
                stableId = annotation.stableId;
            }

            transcriptSet.add(annotation.function + "|"
                    + annotation.geneName + "|"
                    + annotation.stableId
                    + "(" + annotation.aminoAcidChange + ")");

            polyphenHumdiv = Math.max(polyphenHumdiv, annotation.polyphenHumdiv);
            polyphenHumvar = Math.max(polyphenHumvar, annotation.polyphenHumvar);

            if (annotation.isCCDS) {
                polyphenHumdivCCDS = Math.max(polyphenHumdivCCDS, annotation.polyphenHumdivCCDS);
                polyphenHumvarCCDS = Math.max(polyphenHumvarCCDS, annotation.polyphenHumvarCCDS);

                polyphenHumdiv = polyphenHumdivCCDS;
                polyphenHumvar = polyphenHumvarCCDS;

                hasCCDS = true;
            }
        }
    }

    private void checkValid() throws Exception {
        isValid = VariantLevelFilterCommand.isCscoreValid(cscorePhred);

        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid & GnomADCommand.isIncludeGnomADExome) {
            gnomADExome = new GnomADExome(chrStr, startPosition, refAllele, allele);

            isValid = gnomADExome.isValid();
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

    public boolean isValid() {
        return isValid
                & isTrapValid();
    }

    private boolean isTrapValid() {
        if (TrapCommand.isIncludeTrap) {
            if (isIndel()) {
                trapScore = Data.NA;
            } else {
                trapScore = TrapManager.getScore(chrStr, getStartPosition(), allele, geneName);
            }

            if (polyphenHumvar < 0.4335 && polyphenHumvar >= 0) {
                // filter applied to polyphen humvar benign
                return TrapCommand.isTrapScoreValid(trapScore);
            } else if (function.equals("SYNONYMOUS_CODING")
                    || function.equals("INTRON_EXON_BOUNDARY")
                    || function.equals("INTRON")) {
                // filter applied to SYNONYMOUS_CODING, INTRON_EXON_BOUNDARY or INTRONIC variants
                return TrapCommand.isTrapScoreValid(trapScore);
            }
        }

        return true;
    }

    public String getGeneName() {
        if (geneName.isEmpty()) {
            return "NA";
        }

        return geneName;
    }

    public String getFunction() {
        return function;
    }

    public String getCodonChange() {
        if (codonChange.isEmpty()) {
            return "NA";
        }

        return codonChange;
    }

    public String getStableId() {
        if (stableId.isEmpty()) {
            return "NA";
        }

        return stableId;
    }

    public boolean hasCCDS() {
        return hasCCDS;
    }

    public String getAminoAcidChange() {
        if (aminoAcidChange.isEmpty()) {
            return "NA";
        }

        return aminoAcidChange;
    }

    public String getCodingSequenceChange() {
        if (aminoAcidChange.isEmpty()
                || aminoAcidChange.equals("NA")
                || isIndel()) {
            return "NA";
        }

        String posStr = "";

        for (int i = 0; i < aminoAcidChange.length(); i++) {
            char c = aminoAcidChange.charAt(i);

            if (Character.isDigit(c)) {
                posStr += c;
            }
        }

        int aminoAcidPos = Integer.valueOf(posStr);

        String leftStr = codonChange.split("/")[0];
        String rightStr = codonChange.split("/")[1];

        int codingPos = Data.NA;
        int changeIndex = Data.NA;
        int[] codonOffBase = {2, 1, 0}; // aminoAcidPos * 3 is the last position of codon

        for (int i = 0; i < leftStr.length(); i++) {
            if (leftStr.charAt(i) != rightStr.charAt(i)) {
                codingPos = aminoAcidPos * 3 - codonOffBase[i];
                changeIndex = i;
            }
        }

        return "c." + codingPos + leftStr.charAt(changeIndex)
                + ">" + rightStr.charAt(changeIndex);
    }

    public String getTranscriptSet() {
        if (transcriptSet.size() > 0) {
            Set set = new TreeSet(transcriptSet);
            return set.toString().replaceAll(", ", ";").replace("[", "").replace("]", "");
        }

        return "NA";
    }

    public HashSet<String> getGeneSet() {
        return geneSet;
    }

    public String getPolyphenHumdivScore() {
        if (!function.startsWith("NON_SYNONYMOUS")
                || polyphenHumdiv < 0) {
            polyphenHumdiv = Data.NA;
        }

        return FormatManager.getFloat(polyphenHumdiv);
    }

    public String getPolyphenHumvarScore() {
        if (!function.startsWith("NON_SYNONYMOUS")
                || polyphenHumvar < 0) {
            polyphenHumvar = Data.NA;
        }

        return FormatManager.getFloat(polyphenHumvar);
    }

    public String getPolyphenHumdivPrediction() {
        return PolyphenManager.getPrediction(polyphenHumdiv, function);
    }

    public String getPolyphenHumvarPrediction() {
        return PolyphenManager.getPrediction(polyphenHumvar, function);
    }

    public String getGnomADStr() {
        if (GnomADCommand.isIncludeGnomADExome) {
            return gnomADExome.toString();
        } else {
            return "";
        }
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

    public String getDenovoDB() {
        if (DenovoDBCommand.isIncludeDenovoDB) {
            return denovoDB.getOutput();
        } else {
            return "";
        }
    }
}
