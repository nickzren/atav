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
    private String geneDomainName;
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

    public boolean isValid = true; // at variant level

    public AnnotatedVariant(int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(variantId, isIndel, rset);

        function = "";
        geneName = "";
        geneDomainName = "";
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

    public void update(Annotation annotation) {
        if (isValid) {
            if (annotation.geneDomainName.isEmpty()) {
                geneSet.add(annotation.geneName);
            } else {
                geneSet.add(annotation.geneDomainName);
            }

            if (function.isEmpty()
                    || FunctionManager.isMoreDamage(annotation.function, function)) {
                function = annotation.function;
                geneName = annotation.geneName;
                geneDomainName = annotation.geneDomainName;
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

    public boolean isValid() {
        return isValid
                && isTrapValid()
                && isSubRVISValid()
                && isBisValid()
                && isMTRValid();
    }

    // init trap score base on most damaging gene and applied filter
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

    // init sub rvis score base on most damaging gene and applied filter
    private boolean isSubRVISValid() {
        if (SubRvisCommand.isIncludeSubRvis) {
            subRvisOutput = new SubRvisOutput(getGeneName(), getChrStr(), getStartPosition());

            // sub rvis filters will only apply missense variants
            if (function.startsWith("NON_SYNONYMOUS")) {
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

            // bis filters will only apply missense variants
            if (function.startsWith("NON_SYNONYMOUS")) {
                BisGene geneDomain = bisOutput.getGeneDomain();
                BisGene geneExon = bisOutput.getGeneExon();

                return BisCommand.isBisDomainScore0005Valid(geneDomain == null ? Data.NA : geneDomain.getScore0005())
                        && BisCommand.isBisDomainScore0001Valid(geneDomain == null ? Data.NA : geneDomain.getScore0001())
                        && BisCommand.isBisDomainScore00005Valid(geneDomain == null ? Data.NA : geneDomain.getScore00005())
                        && BisCommand.isBisDomainScore00001Valid(geneDomain == null ? Data.NA : geneDomain.getScore00001())
                        && BisCommand.isBisExonScore0005Valid(geneExon == null ? Data.NA : geneExon.getScore0005())
                        && BisCommand.isBisExonScore0001Valid(geneExon == null ? Data.NA : geneExon.getScore0001())
                        && BisCommand.isBisExonScore00005Valid(geneExon == null ? Data.NA : geneExon.getScore00005())
                        && BisCommand.isBisExonScore00001Valid(geneExon == null ? Data.NA : geneExon.getScore00001());
            } else {
                return true;
            }
        }

        return true;
    }

    // init MTR score based on most damaging transcript and applied filter
    private boolean isMTRValid() {
        if (MTRCommand.isIncludeMTR) {
            mtr = new MTR(chrStr, startPosition);

            // MTR filters will only apply missense variants
            if (function.startsWith("NON_SYNONYMOUS")) {
                if (mtr.getFeature().isEmpty()) {
                    return true;
                } else if (mtr.getFeature().equals(stableId)) {
                    return mtr.isValid();
                }
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

    public String getGeneDomainName() {
        if (geneDomainName.isEmpty()) {
            return "NA";
        }

        return geneDomainName;
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
            return denovoDB.getOutput();
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
            return mtr.toString();
        } else {
            return "";
        }
    }
}
