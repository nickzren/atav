package atav.analysis.base;

import atav.global.Data;
import atav.manager.data.*;
import atav.manager.utils.CommandValue;
import atav.manager.utils.FormatManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author nick
 */
public class AnnotatedVariant extends Variant {

    String function;
    String geneName;
    String codonChange;
    String aminoAcidChange;
    String stableId;
    HashSet<String> geneSet = new HashSet<String>();
    HashSet<String> transcriptSet = new HashSet<String>();
    String evsCoverage;
    String evsMafStr;
    String evsFilterStatus;
    double evsMaf;
    double evsMhgf;
    double polyphenHumdiv;
    double polyphenHumvar;

    Exac exac;

    boolean isValid = true;

    public AnnotatedVariant(int variantId, boolean isIndel, ResultSet rset) throws Exception {
        super(variantId, isIndel, rset);

        if (isIndel) {
            polyphenHumdiv = Data.NA;
            polyphenHumvar = Data.NA;
        } else {
            polyphenHumdiv = FormatManager.devide(rset.getInt("polyphen_humdiv"), 1000);
            polyphenHumvar = FormatManager.devide(rset.getInt("polyphen_humvar"), 1000);
        }

        function = "";
        geneName = "";
        codonChange = "";
        aminoAcidChange = "";
        stableId = "";

        checkValid();
    }

    public AnnotatedVariant(int v_id, boolean isIndel,
            String alt, String ref, String rs,
            int pos, String chr) throws Exception {
        super(v_id, isIndel, alt, ref, rs, pos, chr);

        checkValid();
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

            if (polyphenHumdiv < annotation.polyphenHumdiv) {
                polyphenHumdiv = annotation.polyphenHumdiv;
            }

            if (polyphenHumvar < annotation.polyphenHumvar) {
                polyphenHumvar = annotation.polyphenHumvar;
            }
        }
    }

    private void checkValid() throws Exception {
        isValid = QualityManager.isCscoreValid(cscorePhred);

        if (isValid) {
            isValid = VariantManager.isValid(this);
        }

        if (isValid) {
            exac = ExacManager.getExac(isSnv(), region.chrStr,
                    region.startPosition, refAllele, allele);

            isValid = QualityManager.isExacMafValid(exac.getMaxMaf())
                    && QualityManager.isExacVqslodValid(exac.getVqslod(), isSnv());
        }

        if (isValid) {
            initEVSInfo();

            isValid = QualityManager.isEvsStatusValid(evsFilterStatus)
                    && QualityManager.isEvsMafValid(evsMaf);
        }
    }

    public void initEVSInfo() throws Exception {
        evsCoverage = EvsManager.getCoverageInfo(region.chrStr,
                String.valueOf(region.startPosition));

        if (CommandValue.isOldEvsUsed) {
            evsMafStr = EvsManager.getMafInfo(isSnv(), region.chrStr,
                    String.valueOf(region.startPosition), refAllele, allele);

            if (evsCoverage.equals("0,0,0,0,0,0")) {
                evsMafStr = evsMafStr.replaceAll("NAMAF", "NA");
            } else {
                evsMafStr = evsMafStr.replaceAll("NAMAF", "0");
            }

            evsFilterStatus = EvsManager.getFilterStatus();

            initEvsMaf();

            initEvsMhgf();
        }
    }

    public int getEvsCoverage(String evsSample) {
        String[] temp = evsCoverage.split(",");

        if (evsSample.equals("ea")) {
            return Integer.valueOf(temp[0]);
        } else if (evsSample.equals("aa")) {
            return Integer.valueOf(temp[2]);
        }

        return Data.NA;
    }

    private void initEvsMaf() {
        String[] mafs = evsMafStr.split(",");

        double[] values = {Data.NA, Data.NA, Data.NA};

        if (CommandValue.evsMafPop.contains("ea")
                && !mafs[0].equals("NA")) {
            values[0] = Double.valueOf(mafs[0]);
        }

        if (CommandValue.evsMafPop.contains("aa")
                && !mafs[2].equals("NA")) {
            values[1] = Double.valueOf(mafs[2]);
        }

        if (CommandValue.evsMafPop.contains("all")
                && !mafs[4].equals("NA")) {
            values[2] = Double.valueOf(mafs[4]);
        }

        evsMaf = Data.NA;

        for (double value : values) {
            if (value >= 0 && evsMaf < value) {
                evsMaf = value;
            }
        }
    }

    private void initEvsMhgf() throws Exception {
        double[] mhgfs = EvsManager.getMhgf(isSnv(), region.chrStr,
                String.valueOf(region.startPosition), refAllele, allele);

        double[] values = {Data.NA, Data.NA, Data.NA};

        if (CommandValue.evsMafPop.contains("ea")) {
            values[0] = mhgfs[0];
        }

        if (CommandValue.evsMafPop.contains("aa")) {
            values[1] = mhgfs[1];
        }

        if (CommandValue.evsMafPop.contains("all")) {
            values[2] = mhgfs[2];
        }

        evsMhgf = Data.NA;

        for (double value : values) {
            if (value >= 0 && evsMhgf < value) {
                evsMhgf = value;
            }
        }
    }

    public boolean isValid() {
        return isValid
                & PolyphenManager.isValid(polyphenHumdiv, function, CommandValue.polyphenHumdiv)
                & PolyphenManager.isValid(polyphenHumvar, function, CommandValue.polyphenHumvar);
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

        int aminoAcidPos = Data.NA;

        if (aminoAcidChange.length() == 2) {
            aminoAcidPos = Integer.valueOf(aminoAcidChange.substring(1));
        } else {
            aminoAcidPos = Integer.valueOf(aminoAcidChange.substring(1,
                    aminoAcidChange.length() - 1));
        }

        String leftStr = codonChange.split("/")[0];
        String rightStr = codonChange.split("/")[1];

        int codingPos = Data.NA;
        int changeIndex = Data.NA;

        for (int i = 0; i < leftStr.length(); i++) {
            if (leftStr.charAt(i) != rightStr.charAt(i)) {
                codingPos = aminoAcidPos * 3 - i;
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

    public String getEvsCoverageStr() {
        return evsCoverage;
    }

    public String getEvsMafStr() {
        return evsMafStr;
    }

    public String getEvsFilterStatus() {
        return evsFilterStatus;
    }

    public String getPolyphenHumdivScore() {
        if (!function.startsWith("NON_SYNONYMOUS")
                || polyphenHumdiv < 0) {
            polyphenHumdiv = Data.NA;
        }

        return FormatManager.getDouble(polyphenHumdiv);
    }

    public String getPolyphenHumvarScore() {
        if (!function.startsWith("NON_SYNONYMOUS")
                || polyphenHumvar < 0) {
            polyphenHumvar = Data.NA;
        }

        return FormatManager.getDouble(polyphenHumvar);
    }

    public String getPolyphenHumdivPrediction() {
        return getPredictionByScore(polyphenHumdiv);
    }

    public String getPolyphenHumvarPrediction() {
        return getPredictionByScore(polyphenHumvar);
    }

    private String getPredictionByScore(double score) {
        String prediction = PolyphenManager.getPrediction(score, function);

        prediction = prediction.replaceAll("probably", "probably_damaging");
        prediction = prediction.replaceAll("possibly", "possibly_damaging");

        return prediction;
    }

    public boolean isEvsMhgfValid() {
        return QualityManager.isEvsMhgf4RecessiveValid(evsMhgf);
    }

    public String getExacStr() {
        return exac.toString();
    }
}
