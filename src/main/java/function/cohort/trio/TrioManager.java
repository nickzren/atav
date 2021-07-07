package function.cohort.trio;

import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.Sample;
import global.Data;
import function.cohort.base.SampleManager;
import function.variant.base.Output;
import global.Index;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class TrioManager {

    private static final String DENOVO_RULES_PATH = Data.ATAV_HOME + "data/trio_rule_021717.txt.gz";

    public static final String[] COMP_HET_FLAG = {
        "COMPOUND HETEROZYGOTE", // 0
        "POSSIBLY COMPOUND HETEROZYGOTE", // 1
        "NO FLAG", //2
        "DENOVO WITH INHERITED VARIANT", // 3
        "POSSIBLY DENOVO WITH INHERITED VARIANT" // 4
    };

    static ArrayList<Trio> trioList = new ArrayList<>();
    static HashSet<Integer> parentIdSet = new HashSet();
    static HashMap<String, String> denovoRules = new HashMap<>();

    private static final byte HIGH = 2;
    private static final byte MEDIUM = 1;
    private static final byte LOW = 0;

    public static String getHeader4Denovo() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Family ID");
        sj.add("Proband");
        sj.add("Mother");
        sj.add("Father");
        sj.add("Comp Het Flag");
        sj.add("Compound Var");
        sj.add("Var Ctrl Freq #1 & #2 (co-occurance)");
        sj.add("Tier Flag (Compound Var)");
        sj.add("Tier Flag (Single Var)");

        sj.merge(getHeaderByVariant());

        return sj.toString();
    }

    private static StringJoiner getHeaderByVariant() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Dominant and ClinGen Haploinsufficient");
        sj.add("Previously Pathogenic Reported");
        sj.add("Denovo Flag");
        sj.add("Is Inherited Variant");
        sj.merge(Output.getVariantDataHeader());
        sj.merge(Output.getAnnotationDataHeader());
        sj.merge(Output.getCarrierDataHeader());
        sj.add("GT (mother)");
        sj.add("DP Bin (mother)");
        sj.add("GT (father)");
        sj.add("DP Bin (father)");
        sj.merge(Output.getCohortLevelHeader());
        sj.merge(Output.getExternalDataHeader());

        return sj;
    }

    public static void init() {
        initTriosFromInputSamples();
        initDenovoRules();
    }

    private static void initTriosFromInputSamples() {
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()
                    && !sample.getPaternalId().equals("0")
                    && !sample.getMaternalId().equals("0")
                    && !sample.getPaternalId().equals(sample.getMaternalId())) {

                Trio trio = new Trio(sample);

                if (trio.isValid()) {
                    trioList.add(trio);

                    parentIdSet.add(trio.getFatherId());
                    parentIdSet.add(trio.getMotherId());
                }
            }
        }

        if (trioList.isEmpty()) {
            ErrorManager.print("Missing trio from sample file.", ErrorManager.INPUT_PARSING);
        } else {
            LogManager.writeAndPrint("Total trios: " + trioList.size());
        }
    }

    public static ArrayList<Trio> getList() {
        return trioList;
    }

    public static boolean isParent(int id) {
        return parentIdSet.contains(id);
    }

    private static void initDenovoRules() {
        String trioRulesPath = DENOVO_RULES_PATH;

        File f = new File(trioRulesPath);
        if (!f.exists()) {
            ErrorManager.print("The trio rule file (" + trioRulesPath + ") doesn't exist.", ErrorManager.INPUT_PARSING);
        }
        try {
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null) {
                if (LineCount > 0) {
                    String[] fields = str.split("\\t");
                    if (fields.length != 9) {
                        ErrorManager.print("Wrong trio rule file format.", ErrorManager.INPUT_PARSING);
                    }
                    StringBuilder sKey = new StringBuilder();
                    for (int i = 0; i < fields.length - 1; i++) {
                        parse(i, fields[i].trim(), sKey);
                    }
                    String StatusKey = sKey.toString();
                    if (StatusKey.contains("?")) { //un recognized field value
                        ErrorManager.print(sKey.append(": parsing trio rule failed at Line ").append(LineCount + 1).toString(), ErrorManager.INPUT_PARSING);
                    }
                    expand(StatusKey, fields[fields.length - 1].trim()); // the last field holds the value
                }
                LineCount++;
            }
            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getStatus(int chr, boolean isMale, byte oGeno,
            short oDpBin, byte mGeno, short mDpBin, byte dGeno, short dDpBin) {
        String key = getKey(chr, isMale, oGeno, oDpBin, mGeno, mDpBin, dGeno, dDpBin);
        return denovoRules.containsKey(key) ? denovoRules.get(key) : Data.STRING_NA;
    }

    private static String getKey(int chr, boolean isMale, byte oGeno,
            short oDpBin, byte mGeno, short mDpBin, byte dGeno, short dDpBin) {
        StringBuilder sKey = new StringBuilder();
        if (chr < 23) {
            sKey.append('A');
        } else if (chr == 23) {
            sKey.append('X');
        } else if (chr == 24) {
            sKey.append('Y');
        } else if (chr == 26) { // T = chr MT
            sKey.append('T');
        } else {
            sKey.append('N');   //unknown chromosome
        }
        //offspring gender
        if (isMale) { //male
            sKey.append('M');
        } else {
            sKey.append('F');
        }

        getKeyPartGenotypeAndCoverage(sKey, oGeno, oDpBin);
        getKeyPartGenotypeAndCoverage(sKey, mGeno, mDpBin);
        getKeyPartGenotypeAndCoverage(sKey, dGeno, dDpBin);
        return sKey.toString();
    }

    private static void getKeyPartGenotypeAndCoverage(StringBuilder sKey, byte geno, short dpBin) {
        switch (geno) {
            case Index.REF: //HOM REF
                sKey.append('0');
                break;
            case Index.HET:
                sKey.append('1');
                break;
            case Index.HOM:
                sKey.append('2');
                break;
            default:
                sKey.append('N');
                break;
        }

        if (geno == Data.BYTE_NA) { //missing
            sKey.append('0');
        } else if (dpBin >= GenotypeLevelFilterCommand.minDpBin
                || GenotypeLevelFilterCommand.minDpBin == Data.NO_FILTER) {
            sKey.append('1');
        } else {
            sKey.append('2');
        }
    }

    private static void parse(int i, String field, StringBuilder sKey) {
        switch (i) {
            case 0:         //chromosome 
                if (field.equalsIgnoreCase("AUTOSOME")) {
                    sKey.append('A');
                } else if (field.equalsIgnoreCase("X")) {
                    sKey.append('X');
                } else if (field.equalsIgnoreCase("Y")) {
                    sKey.append('Y');
                } else if (field.equalsIgnoreCase("MT")) {
                    sKey.append('T'); // T = chr MT
                } else {
                    sKey.append('?');
                }
                break;
            case 1:        //offspring gender
                if (field.equalsIgnoreCase("MALE")) {
                    sKey.append('M');
                } else if (field.equalsIgnoreCase("FEMALE")) {
                    sKey.append('F');
                } else if (field.equalsIgnoreCase("MALE OR FEMALE")) {
                    sKey.append('_');
                } else {
                    sKey.append('?');
                }
                break;
            case 2:     //child genotype
            case 4:     //mom genetpye
            case 6:     //dad genotype
                if (field.equalsIgnoreCase("HOM-REF")) {
                    sKey.append('0');
                } else if (field.equalsIgnoreCase("HET")) {
                    sKey.append('1');
                } else if (field.equalsIgnoreCase("HOM-VAR")) {
                    sKey.append('2');
                } else if (field.equalsIgnoreCase(Data.STRING_NA) || field.equalsIgnoreCase("ANY")) {
                    sKey.append('_');
                } else {
                    sKey.append('?');
                }
                break;
            case 3:     //child coverage
            case 5:     //mom coverage
            case 7:     //dad coverage
                if (field.equalsIgnoreCase(">=10")) {
                    sKey.append('1');
                } else if (field.equalsIgnoreCase("<10")) {
                    sKey.append('2');
                } else if (field.equalsIgnoreCase(Data.STRING_NA) || field.equalsIgnoreCase("ANY")) {
                    sKey.append('_');
                } else {
                    sKey.append('?');
                }
                break;
        }
    }

    private static void expand(String key, String value) {
        int depth = key.indexOf('_');
        if (depth > 0) { // need to be further expanded
            switch (depth) {
                case 1:
                    expand(key.replaceFirst("_", "M"), value);
                    expand(key.replaceFirst("_", "F"), value);
                    break;
                case 2: //ref allele major
                    expand(key.replaceFirst("_", "Y"), value);
                    expand(key.replaceFirst("_", "N"), value);
                    break;
                case 3:
                case 5:
                case 7:
                    expand(key.replaceFirst("_", "N"), value);
                case 4:
                case 6:
                case 8:
                    expand(key.replaceFirst("_", "0"), value);
                    expand(key.replaceFirst("_", "1"), value);
                    expand(key.replaceFirst("_", "2"), value);
                    break;
            }
        } else {
            denovoRules.put(key, value);
        }
    }

    public static String getCompHetFlag(
            byte cGeno1, byte mGeno1, byte fGeno1,
            byte cGeno2, byte mGeno2, byte fGeno2) {

        // exclude if the child is homozygous, wild type or variant, for either variant
        if (((cGeno1 == Index.REF || cGeno1 == Index.HOM))
                || (cGeno2 == Index.REF || cGeno2 == Index.HOM)) {
            return COMP_HET_FLAG[2];
        }
        if ((fGeno1 == Data.BYTE_NA && mGeno1 == Data.BYTE_NA)
                || (fGeno2 == Data.BYTE_NA && mGeno2 == Data.BYTE_NA)) {
            // if both parents are missing the same call, exclude this candidate
            return COMP_HET_FLAG[2];
        }
        if ((fGeno1 == Data.BYTE_NA && fGeno2 == Data.BYTE_NA)
                || (mGeno1 == Data.BYTE_NA && mGeno2 == Data.BYTE_NA)) {
            // if one parent is missing both calls, exclude this candidate
            return COMP_HET_FLAG[2];
        }
        // if any parental call is hom at at least minCov depth, exclude
        if ((fGeno1 == Index.HOM) || (fGeno2 == Index.HOM)
                || (mGeno1 == Index.HOM) || (mGeno2 == Index.HOM)) {
            return COMP_HET_FLAG[2];
        }
        // if either parent has both variants, exclude
        if (((fGeno1 == Index.HET || fGeno1 == Index.HOM) && (fGeno2 == Index.HET || fGeno2 == Index.HOM))
                || ((mGeno1 == Index.HET || mGeno1 == Index.HOM) && (mGeno2 == Index.HET || mGeno2 == Index.HOM))) {
            return COMP_HET_FLAG[2];
        }
        // if either parent has neither variant, exclude
        if ((fGeno1 == Index.REF && fGeno2 == Index.REF)
                || (mGeno1 == Index.REF && mGeno2 == Index.REF)) {
            return COMP_HET_FLAG[2];
        }
        // if both parents are wild type for the same variant, exclude
        if ((fGeno1 == Index.REF && mGeno1 == Index.REF)
                || (fGeno2 == Index.REF && mGeno2 == Index.REF)) {
            return COMP_HET_FLAG[2];
        }
        // if both parents have the same variant, exclude
        if (((fGeno1 == Index.HET || fGeno1 == Index.HOM) && (mGeno1 == Index.HET || mGeno1 == Index.HOM))
                || ((fGeno2 == Index.HET || fGeno2 == Index.HOM) && (mGeno2 == Index.HET || mGeno2 == Index.HOM))) {
            return COMP_HET_FLAG[2];
        }
        // we've excluded all that should be excluded - the possibilities are now that
        // the compound het is "Shared" or that it's "Possibly Shared", i.e. there's a
        // possibility the variants don't segregate properly but there wasn't sufficient
        // cause to exclude entirely
        if ((fGeno1 == Index.HET && fGeno2 == Index.REF && mGeno1 == Index.REF && mGeno2 == Index.HET)
                || (fGeno1 == Index.REF && fGeno2 == Index.HET && mGeno1 == Index.HET && mGeno2 == Index.REF)) {
            if (cGeno1 == Index.HET && cGeno2 == Index.HET) {
                return COMP_HET_FLAG[0];
            } else {
                return COMP_HET_FLAG[1];
            }
        } else {
            return COMP_HET_FLAG[1];
        }
    }

    public static String getCompHetFlagByDenovo(
            String compHetFlag,
            byte cGeno1, byte mGeno1, byte fGeno1, String denovoFlag1,
            byte cGeno2, byte mGeno2, byte fGeno2, String denovoFlag2) {
        if (compHetFlag.equals(COMP_HET_FLAG[2])) {
            byte denovoConfidence1 = getDenovoConfidence(denovoFlag1);
            byte denovoConfidence2 = getDenovoConfidence(denovoFlag2);

            boolean isDenovo1 = denovoConfidence1 != Data.BYTE_NA;
            boolean isDenovo2 = denovoConfidence2 != Data.BYTE_NA;

            if (isDenovo1 ^ isDenovo2) {
                byte cGenoInherited = cGeno1;
                byte fGenoInherited = fGeno1;
                byte mGenoInherited = mGeno1;
                byte denovoConfidence = denovoConfidence2;

                if (isDenovo1) {
                    cGenoInherited = cGeno2;
                    fGenoInherited = fGeno2;
                    mGenoInherited = mGeno2;
                    denovoConfidence = denovoConfidence1;
                }
                // Only consider situations in which no one is homozygous for the inherited variant
                // and the child is not homozygous variant or wild-type 
                if (!((cGenoInherited == Index.HOM || cGenoInherited == Index.REF))
                        && !(fGenoInherited == Index.HOM)
                        && !(mGenoInherited == Index.HOM)) {
                    // Treat the inherited variant as high confidence if neither parent
                    // is homozygous or missing a genotype, at least one parent is heterozygous,
                    // and the child is heterozygous
                    if (cGenoInherited == Index.HET
                            && !(fGenoInherited == Index.HOM || fGenoInherited == Data.BYTE_NA)
                            && !(mGenoInherited == Index.HOM || mGenoInherited == Data.BYTE_NA)
                            && (fGenoInherited == Index.HET || mGenoInherited == Index.HET)) {
                        if (denovoConfidence == HIGH) {
                            // Both variants are high confidence
                            return COMP_HET_FLAG[3];
                        }
                        // denovo variant is either medium or low confidence and inherited variant is high confidence
                        return COMP_HET_FLAG[4];
                    } else if (denovoConfidence == HIGH) {
                        // Only return a possible compound het if the de novo is high confident.
                        return COMP_HET_FLAG[4];
                    }
                }
            }
        }

        return compHetFlag;
    }

    private static byte getDenovoConfidence(String denovoFlag) {
        if (denovoFlag.startsWith("DE NOVO")
                || denovoFlag.startsWith("NEWLY HEMIZYGOUS")) {
            return HIGH;
        } else if (denovoFlag.startsWith("POSSIBLY DE NOVO")
                || denovoFlag.startsWith("POSSIBLY NEWLY HEMIZYGOUS")) {
            return MEDIUM;
        } else if (denovoFlag.startsWith("UNLIKELY DE NOVO")
                || denovoFlag.startsWith("UNLIKELY NEWLY HEMIZYGOUS")) {
            return LOW;
        }

        return Data.BYTE_NA;
    }

    /*
     * The number of people who have BOTH of the variants divided by the total
     * number of covered people. freq[0] Frequency of Variant #1 & #2
     * (co-occurance) in cases. freq[1] Frequency of Variant #1 & #2
     * (co-occurance) in ctrls
     */
    public static float[] getCoOccurrenceFreq(TrioOutput output1, TrioOutput output2) {
        float[] freq = new float[2];

        int quanlifiedCaseCount = 0, qualifiedCtrlCount = 0;
        int totalCaseCount = 0, totalCtrlCount = 0;

        for (Sample sample : SampleManager.getList()) {
            if (sample.getName().equals(output1.fatherName)
                    || sample.getName().equals(output1.motherName)) // ignore parents trio
            {
                continue;
            }

            boolean isCoQualifiedGeno = isCoQualifiedGeno(output1, output2, sample.getIndex());

            if (sample.isCase()) {
                totalCaseCount++;
                if (isCoQualifiedGeno) {
                    quanlifiedCaseCount++;
                }
            } else {
                totalCtrlCount++;
                if (isCoQualifiedGeno) {
                    qualifiedCtrlCount++;
                }
            }
        }

        freq[Index.CTRL] = MathManager.devide(qualifiedCtrlCount, totalCtrlCount);
        freq[Index.CASE] = MathManager.devide(quanlifiedCaseCount, totalCaseCount);

        return freq;
    }

    private static boolean isCoQualifiedGeno(TrioOutput output1,
            TrioOutput output2, int index) {
        byte geno1 = output1.getCalledVariant().getGT(index);
        byte geno2 = output2.getCalledVariant().getGT(index);

        return output1.isQualifiedGeno(geno1)
                && output2.isQualifiedGeno(geno2);
    }

}
