package function.genotype.trio;

import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class TrioManager {

    private static final String DENOVO_RULES_PATH = "data/trio_rule.txt";

    public static final String[] COMP_HET_FLAG = {
        "compound heterozygote", // 0
        "possibly compound heterozygote", // 1
        "no flag", //2
        "denovo with inherited variant", // 3
        "possibly denovo with inherited variant" // 4
    };

    static ArrayList<Trio> trioList = new ArrayList<>();
    static HashSet<Integer> parentIdSet = new HashSet();
    static HashMap<String, String> denovoRules = new HashMap<>();

    private static final int HIGH = 2;
    private static final int MEDIUM = 1;
    private static final int LOW = 0;

    public static String getTitle4Denovo() {
        return "Family ID,"
                + "Child,"
                + "Mother,"
                + "Father,"
                + "Flag,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + getTitleByVariant();
    }

    public static String getTitle4CompHet() {
        return "Family ID,"
                + "Child,"
                + "Mother,"
                + "Father,"
                + "Flag,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "Var Case Freq #1 & #2 (co-occurance),"
                + "Var Ctrl Freq #1 & #2 (co-occurance),"
                + initVarTitleStr("1")
                + initVarTitleStr("2");
    }

    private static String initVarTitleStr(String var) {
        String[] columnList = getTitleByVariant().split(",");

        StringBuilder sb = new StringBuilder();

        for (String column : columnList) {
            sb.append(column).append(" (#").append(var).append(")" + ",");
        }

        return sb.toString();
    }

    private static String getTitleByVariant() {
        return "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + GerpManager.getTitle()
                + TrapManager.getTitle()
                + "Is Minor Ref,"
                + "Genotype (child),"
                + "Samtools Raw Coverage (child),"
                + "Gatk Filtered Coverage (child),"
                + "Reads Alt (child),"
                + "Reads Ref (child),"
                + "Percent Read Alt (child),"
                + "Percent Alt Read Binomial P (child),"
                + "Pass Fail Status (child),"
                + "Genotype Qual GQ (child),"
                + "Qual By Depth QD (child),"
                + "Haplotype Score (child),"
                + "Rms Map Qual MQ (child),"
                + "Qual (child),"
                + "Genotype (mother),"
                + "Samtools Raw Coverage (mother),"
                + "Gatk Filtered Coverage (mother),"
                + "Reads Alt (mother),"
                + "Reads Ref (mother),"
                + "Percent Read Alt (mother),"
                + "Genotype (father),"
                + "Samtools Raw Coverage (father),"
                + "Gatk Filtered Coverage (father),"
                + "Reads Alt (father),"
                + "Reads Ref (father),"
                + "Percent Read Alt (father),"
                + "Major Hom Case,"
                + "Het Case,"
                + "Minor Hom Case,"
                + "Minor Hom Case Freq,"
                + "Het Case Freq,"
                + "Major Hom Ctrl,"
                + "Het Ctrl,"
                + "Minor Hom Ctrl,"
                + "Minor Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "Missing Case,"
                + "QC Fail Case,"
                + "Missing Ctrl,"
                + "QC Fail Ctrl,"
                + "Case MAF,"
                + "Ctrl MAF,"
                + "Case HWE_P,"
                + "Ctrl HWE_P,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle();
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
            ErrorManager.print("Missing trio from sample file.");
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

        if (CommonCommand.isDebug) {
            trioRulesPath = Data.RECOURCE_PATH + trioRulesPath;
        }

        File RuleFile = new File(trioRulesPath);
        if (!RuleFile.exists()) {
            ErrorManager.print("The trio rule file (" + trioRulesPath + ") doesn't exist.");
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(trioRulesPath));
            String str;
            int LineCount = 0;
            while ((str = br.readLine()) != null) {
                if (LineCount > 0) {
                    String[] fields = str.split("\\t");
                    if (fields.length != 10) {
                        ErrorManager.print("Wrong trio rule file format.");
                    }
                    StringBuilder sKey = new StringBuilder();
                    for (int i = 0; i < fields.length - 1; i++) {
                        parse(i, fields[i].trim(), sKey);
                    }
                    String StatusKey = sKey.toString();
                    if (StatusKey.contains("?")) { //un recognized field value
                        ErrorManager.print(sKey.append(": parsing trio rule failed at Line ").append(LineCount + 1).toString());
                    }
                    expand(StatusKey, fields[fields.length - 1].trim()); // the last field holds the value
                }
                LineCount++;
            }
            br.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getStatus(int chr, boolean refMajor, boolean isMale, int oGeno,
            int oCov, int mGeno, int mCov, int dGeno, int dCov) {
        String key = getKey(chr, refMajor, isMale, oGeno, oCov, mGeno, mCov, dGeno, dCov);
        return denovoRules.containsKey(key) ? denovoRules.get(key) : "NA";
    }

    private static String getKey(int chr, boolean refMajor, boolean isMale, int oGeno,
            int oCov, int mGeno, int mCov, int dGeno, int dCov) {
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

        sKey.append(refMajor ? 'Y' : 'N');
        getKeyPartGenotypeAndCoverage(sKey, oGeno, oCov);
        getKeyPartGenotypeAndCoverage(sKey, mGeno, mCov);
        getKeyPartGenotypeAndCoverage(sKey, dGeno, dCov);
        return sKey.toString();
    }

    private static void getKeyPartGenotypeAndCoverage(StringBuilder sKey, int Geno, int Cov) {
        switch (Geno) {
            case 0: //HOM REF
                sKey.append('0');
                break;
            case 1:
                sKey.append('1');
                break;
            case 2:
                sKey.append('2');
                break;
            default:
                sKey.append('N');
                break;
        }

        if (Geno == Data.NA) { //missing
            sKey.append('0');
        } else if (Cov >= GenotypeLevelFilterCommand.minCoverage
                || GenotypeLevelFilterCommand.minCoverage == Data.NO_FILTER) {
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
            case 2:     //ref allele major
                if (field.equalsIgnoreCase("YES")) {
                    sKey.append('Y');
                } else if (field.equalsIgnoreCase("NO")) {
                    sKey.append('N');
                } else if (field.equalsIgnoreCase("ANY")) {
                    sKey.append('_');
                } else {
                    sKey.append('?');
                }
                break;
            case 3:     //child genotype
            case 5:     //mom genetpye
            case 7:     //dad genotype
                if (field.equalsIgnoreCase("HOM-REF")) {
                    sKey.append('0');
                } else if (field.equalsIgnoreCase("HET")) {
                    sKey.append('1');
                } else if (field.equalsIgnoreCase("HOM-VAR")) {
                    sKey.append('2');
                } else if (field.equalsIgnoreCase("NA") || field.equalsIgnoreCase("ANY")) {
                    sKey.append('_');
                } else {
                    sKey.append('?');
                }
                break;
            case 4:     //child coverage
            case 6:     //mom coverage
            case 8:     //dad coverage
                if (field.equalsIgnoreCase(">=10")) {
                    sKey.append('1');
                } else if (field.equalsIgnoreCase("<10")) {
                    sKey.append('2');
                } else if (field.equalsIgnoreCase("NA") || field.equalsIgnoreCase("ANY")) {
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
            int cGeno1, int cCov1,
            int mGeno1, int mCov1,
            int fGeno1, int fCov1,
            boolean isMinorRef1,
            int cGeno2, int cCov2,
            int mGeno2, int mCov2,
            int fGeno2, int fCov2,
            boolean isMinorRef2) {
        int minCov = GenotypeLevelFilterCommand.minCoverage;

        // to limit confusion, we swap genotypes 0<->2 if isMinorRef
        // i.e. hom ref<->hom variant
        // that enables us to ignore the isMinorRef aspect thereafter
        if (isMinorRef1) {
            cGeno1 = swapGenotypes(cGeno1);
            fGeno1 = swapGenotypes(fGeno1);
            mGeno1 = swapGenotypes(mGeno1);
        }
        if (isMinorRef2) {
            cGeno2 = swapGenotypes(cGeno2);
            fGeno2 = swapGenotypes(fGeno2);
            mGeno2 = swapGenotypes(mGeno2);
        }
        // exclude if the child is homozygous, wild type or variant, for either variant
        if (((cGeno1 == Index.REF || cGeno1 == Index.HOM) && cCov1 >= minCov)
                || ((cGeno2 == Index.REF || cGeno2 == Index.HOM) && cCov2 >= minCov)) {
            return COMP_HET_FLAG[2];
        }
        if ((fGeno1 == Data.NA && mGeno1 == Data.NA) || (fGeno2 == Data.NA && mGeno2 == Data.NA)) {
            // if both parents are missing the same call, exclude this candidate
            return COMP_HET_FLAG[2];
        }
        if ((fGeno1 == Data.NA && fGeno2 == Data.NA) || (mGeno1 == Data.NA && mGeno2 == Data.NA)) {
            // if one parent is missing both calls, exclude this candidate
            return COMP_HET_FLAG[2];
        }
        // if any parental call is hom at at least minCov depth, exclude
        if ((fGeno1 == Index.HOM && fCov1 >= minCov) || (fGeno2 == Index.HOM && fCov2 >= minCov)
                || (mGeno1 == Index.HOM && mCov1 >= minCov) || (mGeno2 == Index.HOM && mCov2 >= minCov)) {
            return COMP_HET_FLAG[2];
        }
        // if either parent has both variants, exclude
        if (((fGeno1 == Index.HET || fGeno1 == Index.HOM) && (fGeno2 == Index.HET || fGeno2 == Index.HOM))
                || ((mGeno1 == Index.HET || mGeno1 == Index.HOM) && (mGeno2 == Index.HET || mGeno2 == Index.HOM))) {
            return COMP_HET_FLAG[2];
        }
        // if either parent has neither variant, exclude
        if ((fGeno1 == Index.REF && fCov1 >= minCov && fGeno2 == Index.REF && fCov2 >= minCov)
                || (mGeno1 == Index.REF && mCov1 >= minCov && mGeno2 == Index.REF && mCov2 >= minCov)) {
            return COMP_HET_FLAG[2];
        }
        // if both parents are wild type for the same variant, exclude
        if ((fGeno1 == Index.REF && fCov1 >= minCov && mGeno1 == Index.REF && mCov1 >= minCov)
                || (fGeno2 == Index.REF && fCov2 >= minCov && mGeno2 == Index.REF && mCov2 >= minCov)) {
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
            int cGeno1, int cCov1,
            int mGeno1, int mCov1,
            int fGeno1, int fCov1,
            boolean isMinorRef1,
            String denovoFlag1,
            int cGeno2, int cCov2,
            int mGeno2, int mCov2,
            int fGeno2, int fCov2,
            boolean isMinorRef2,
            String denovoFlag2) {
        if (compHetFlag.equals(COMP_HET_FLAG[2])) {
            if (isMinorRef1) {
                cGeno1 = swapGenotypes(cGeno1);
                fGeno1 = swapGenotypes(fGeno1);
                mGeno1 = swapGenotypes(mGeno1);
            }
            if (isMinorRef2) {
                cGeno2 = swapGenotypes(cGeno2);
                fGeno2 = swapGenotypes(fGeno2);
                mGeno2 = swapGenotypes(mGeno2);
            }

            int denovoConfidence1 = getDenovoConfidence(denovoFlag1);
            int denovoConfidence2 = getDenovoConfidence(denovoFlag2);

            boolean isDenovo1 = denovoConfidence1 != Data.NA;
            boolean isDenovo2 = denovoConfidence2 != Data.NA;

            if (isDenovo1 ^ isDenovo2) {
                int cGenoInherited = cGeno1;
                int cCovInherited = cCov1;
                int fGenoInherited = fGeno1;
                int fCovInherited = fCov1;
                int mGenoInherited = mGeno1;
                int mCovInherited = mCov1;
                int denovoConfidence = denovoConfidence2;

                if (isDenovo1) {
                    cGenoInherited = cGeno2;
                    cCovInherited = cCov2;
                    fGenoInherited = fGeno2;
                    fCovInherited = fCov2;
                    mGenoInherited = mGeno2;
                    mCovInherited = mCov2;
                    denovoConfidence = denovoConfidence1;
                }
                // Only consider situations in which no one is homozygous for the inherited variant
                // and the child is not homozygous variant or wild-type 
                int minCov = GenotypeLevelFilterCommand.minCoverage;
                if (!((cGenoInherited == Index.HOM || cGenoInherited == Index.REF) && cCovInherited >= minCov)
                        && !(fGenoInherited == Index.HOM && fCovInherited >= minCov)
                        && !(mGenoInherited == Index.HOM && mCovInherited >= minCov)) {
                    // Treat the inherited variant as high confidence if neither parent
                    // is homozygous or missing a genotype, at least one parent is heterozygous,
                    // and the child is heterozygous
                    if (cGenoInherited == Index.HET
                            && !(fGenoInherited == Index.HOM || fGenoInherited == Data.NA)
                            && !(mGenoInherited == Index.HOM || mGenoInherited == Data.NA)
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

    private static int getDenovoConfidence(String denovoFlag) {
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

        return Data.NA;
    }

    private static int swapGenotypes(
            int genotype) {
        switch (genotype) {
            case Index.REF:
                return Index.HOM;
            case Index.HOM:
                return Index.REF;
            default:
                return genotype;
        }
    }

    /*
     * The number of people who have BOTH of the variants divided by the total
     * number of covered people. freq[0] Frequency of Variant #1 & #2
     * (co-occurance) in cases. freq[1] Frequency of Variant #1 & #2
     * (co-occurance) in ctrls
     */
    public static double[] getCoOccurrenceFreq(TrioOutput output1, TrioOutput output2) {
        double[] freq = new double[2];

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
        int geno1 = output1.getCalledVariant().getGenotype(index);
        int geno2 = output2.getCalledVariant().getGenotype(index);

        return output1.isQualifiedGeno(geno1)
                && output2.isQualifiedGeno(geno2);
    }
}
