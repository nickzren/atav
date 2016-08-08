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
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class TrioManager {

    private static final String DENOVO_RULES_PATH = "data/trio_rule.txt";

    static ArrayList<Trio> trioList = new ArrayList<>();
    static HashSet<Integer> parentIdSet = new HashSet();
    static HashMap<String, String> denovoRules = new HashMap<>();

    public static String getTitle4Denovo() {
        return "Family ID,"
                + "Child,"
                + "Mother,"
                + "Father,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "Denovo Flag,"
                + getTitleByVariant();
    }

    public static String getTitle4CompHet() {
        return "Family ID,"
                + "Child,"
                + "Mother,"
                + "Father,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "Comp Het Flag,"
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
        initList();
        initDenovoRules();
    }

    public static void initList() {
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
            ErrorManager.print("There is no trio from your sample file.");
        } else {
            LogManager.writeAndPrint("There are " + trioList.size()
                    + " trios available from your sample file.");
        }
    }

    public static ArrayList<Trio> getList() {
        return trioList;
    }

    public static boolean isParent(int id) {
        if (parentIdSet.isEmpty()) {
            return false;
        }

        return parentIdSet.contains(id);
    }

    public static void initDenovoRules() {
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
        return denovoRules.containsKey(key) ? denovoRules.get(key) : "unknown";
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
        } else { // no wild cardm so it is a final rule
            denovoRules.put(key, value.toLowerCase());
        }
    }
}
