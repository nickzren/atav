package utils;

import function.annotation.base.AnnotationLevelFilterCommand;
import global.Data;
import function.annotation.varanno.VarAnnoCommand;
import function.coverage.base.CoverageCommand;
import function.external.bis.BisCommand;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.EvsCommand;
import function.external.exac.ExacCommand;
import function.external.gnomad.GnomADCommand;
import function.external.flanking.FlankingCommand;
import function.external.genomes.GenomesCommand;
import function.external.gerp.GerpCommand;
import function.external.kaviar.KaviarCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.mgi.MgiCommand;
import function.external.mtr.MTRCommand;
import function.external.rvis.RvisCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.trap.TrapCommand;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.collapsing.CollapsingCommand;
import function.genotype.parental.ParentalCommand;
import function.genotype.pedmap.PedMapCommand;
import function.genotype.sibling.SiblingCommand;
import function.genotype.statistics.StatisticsCommand;
import function.genotype.trio.TrioCommand;
import function.genotype.var.VarCommand;
import function.genotype.vargeno.VarGenoCommand;
import function.test.TestCommand;
import function.variant.base.VariantLevelFilterCommand;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author nick
 */
public class CommandManager {

    private static String[] optionArray;
    private static ArrayList<CommandOption> optionList = new ArrayList<>();
    public static String command = "";
    private static String commandFile = "";

    private static void initCommand4Debug() {
        String cmd = "";

        optionArray = cmd.split("\\s+");
    }

    public static void initOptions(String[] options) {
        try {
            initCommandOptions(options);

            initOptionList();

            initOutput();

            LogManager.initPath();

            initFunctionOptions();

            initSubOptions();

            initCommonOptions();

            initOptions4Debug();

            outputInvalidOptions();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initCommandOptions(String[] options) {
        if (options.length == 0) {
            if (CommonCommand.isDebug) {
                initCommand4Debug();
            } else {
                System.out.println("\nError: without any input parameters to run ATAV. \n\nExit\n");
                System.exit(0);
            }
        } else // init options from command file or command line
         if (isCommandFileIncluded(options)) {
                initCommandFromFile();
            } else {
                optionArray = options;
            }

        cleanUpOddSymbol();

        initCommand4Log();
    }

    private static void cleanUpOddSymbol() {
        for (int i = 0; i < optionArray.length; i++) {
            // below solve situation: dash hyphen or dash only
            optionArray[i] = optionArray[i].replaceAll("\\u2013", "--"); // en dash --> hyphen
            optionArray[i] = optionArray[i].replaceAll("\\u2014", "--"); // em dash --> hyphen
            optionArray[i] = optionArray[i].replace("---", "--");
        }
    }

    private static boolean isCommandFileIncluded(String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals("--command-file")) {
                if (isFileExist(options[i + 1])) {
                    commandFile = options[i + 1];
                } else {
                    System.out.println("\nInvalid value '" + options[i + 1]
                            + "' for '--command-file' option.");
                    System.exit(0);
                }

                return true;
            }
        }

        return false;
    }

    private static void initCommandFromFile() {
        File f = new File(commandFile);

        String lineStr = "";
        String cmd = "";

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                cmd += lineStr + " ";
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        optionArray = cmd.split("\\s+");
    }

    private static void initCommand4Log() {
        String version = Data.VERSION;

        if (Data.VERSION.contains(" ")) {
            version = Data.VERSION.substring(Data.VERSION.indexOf(" ") + 1);
        }

        command = "atav_" + version + ".sh";

        if (commandFile.isEmpty()) {
            for (String str : optionArray) {
                command += " " + str;
            }
        } else {
            command += " " + "--command-file " + commandFile;
        }
    }

    /*
     * init option list by user ATAV command
     */
    private static void initOptionList() {
        int valueIndex;

        for (int i = 0; i < optionArray.length; i++) {
            if (optionArray[i].startsWith("--")) {
                valueIndex = i + 1;

                try {
                    if (valueIndex == optionArray.length
                            || (optionArray[valueIndex].startsWith("-")
                            && !FormatManager.isDouble(optionArray[valueIndex]))) {
                        optionList.add(new CommandOption(optionArray[i], ""));
                    } else {
                        optionList.add(new CommandOption(optionArray[i], optionArray[++i]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Invalid command option: " + optionArray[i]);
                System.exit(0);
            }
        }
    }

    /*
     * get output value from ATAV command then init output path
     */
    private static void initOutput() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--out")) {
                initOutputPath(option.getValue());
                iterator.remove();
                break;
            }
        }

        if (CommonCommand.outputPath.isEmpty()) {
            System.out.println("\nPlease specify output path: --out $PATH \n\nExit\n");
            System.exit(0);
        }
    }

    private static void initOutputPath(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            } else if (!dir.canWrite()) {
                System.out.println("\nYou don't have write permissions into " + path + "! \n\nExit\n");
                System.exit(0);
            }

            CommonCommand.realOutputPath = path;
            CommonCommand.outputDirName = dir.getName();
            CommonCommand.outputPath = path + File.separator + dir.getName() + "_";
        } catch (Exception e) {
            System.out.println("\nError in creating an output directory. \n\nExit\n");
            System.exit(0);
        }
    }

    private static void initOptions4Debug() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--db-host")) {
                DBManager.setDBHost(option.getValue());
            } else if (option.getName().equals("--debug")) {
                CommonCommand.isDebug = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initFunctionOptions() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;
        boolean hasMainFunction = false;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();

            switch (option.getName()) {
                // Genotype Analysis Functions
                case "--list-var-geno":
                    VarGenoCommand.isListVarGeno = true;
                    break;
                case "--list-var":
                    VarCommand.isListVar = true;
                    break;
                case "--collapsing-dom":
                    CollapsingCommand.isCollapsingSingleVariant = true;
                    break;
                case "--collapsing-rec":
                    CollapsingCommand.isCollapsingSingleVariant = true;
                    CollapsingCommand.isRecessive = true;
                    break;
                case "--collapsing-comp-het":
                    CollapsingCommand.isCollapsingCompHet = true;
                    break;
                case "--fisher":
                    StatisticsCommand.isFisher = true;
                    break;
                case "--linear":
                    StatisticsCommand.isLinear = true;
                    break;
                case "--list-sibling-comp-het":
                    SiblingCommand.isSiblingCompHet = true;
                    break;
                case "--list-trio":
                    TrioCommand.isListTrio = true;
                    GenotypeLevelFilterCommand.minCaseCarrier = 1;
                    break;
                case "--list-parental-mosaic":
                    ParentalCommand.isParentalMosaic = true;
                    break;
                case "--ped-map":
                    PedMapCommand.isPedMap = true;
                    break;

                // Variant Annotation Functions
                case "--list-var-anno":
                    CommonCommand.isNonSampleAnalysis = true;
                    VarAnnoCommand.isListVarAnno = true;
                    EvsCommand.isIncludeEvs = true;
                    ExacCommand.isIncludeExac = true;
                    GnomADCommand.isIncludeGnomADExome = true;
                    GerpCommand.isIncludeGerp = true;
                    TrapCommand.isIncludeTrap = true;
//                    KaviarCommand.isIncludeKaviar = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    RvisCommand.isIncludeRvis = true;
                    SubRvisCommand.isIncludeSubRvis = true;
//                    GenomesCommand.isInclude1000Genomes = true;
                    MgiCommand.isIncludeMgi = true;
                    DenovoDBCommand.isIncludeDenovoDB = true;
                    DiscovEHRCommand.isIncludeDiscovEHR = true;
                    MTRCommand.isIncludeMTR = true;
                    break;
                // Coverage Analysis Functions    
                case "--coverage-summary":
                    CoverageCommand.isCoverageSummary = true;
                    break;
                case "--site-coverage-summary":
                    CoverageCommand.isSiteCoverageSummary = true;
                    break;
                case "--coverage-comparison":
                    CoverageCommand.isCoverageComparison = true;
                    break;
                case "--site-coverage-comparison":
                    CoverageCommand.isSiteCoverageComparison = true;
                    break;

                // External Datasets Functions    
                case "--list-evs":
                    CommonCommand.isNonSampleAnalysis = true;
                    EvsCommand.isListEvs = true;
                    EvsCommand.isIncludeEvs = true;
                    break;
                case "--list-exac":
                    CommonCommand.isNonSampleAnalysis = true;
                    ExacCommand.isListExac = true;
                    ExacCommand.isIncludeExac = true;
                    break;
                case "--list-gnomad-exome":
                    CommonCommand.isNonSampleAnalysis = true;
                    GnomADCommand.isListGnomADExome = true;
                    GnomADCommand.isIncludeGnomADExome = true;
                    break;
                case "--list-gnomad-genome":
                    CommonCommand.isNonSampleAnalysis = true;
                    GnomADCommand.isListGnomADGenome = true;
                    GnomADCommand.isIncludeGnomADGenome = true;
                    break;
                case "--list-known-var":
                    CommonCommand.isNonSampleAnalysis = true;
                    KnownVarCommand.isListKnownVar = true;
                    KnownVarCommand.isIncludeKnownVar = true;
                    break;
                case "--list-flanking-seq":
                    CommonCommand.isNonSampleAnalysis = true;
                    FlankingCommand.isListFlankingSeq = true;
                    break;
                case "--list-kaviar":
                    CommonCommand.isNonSampleAnalysis = true;
                    KaviarCommand.isListKaviar = true;
                    KaviarCommand.isIncludeKaviar = true;
                    break;
                case "--list-gerp":
                    CommonCommand.isNonSampleAnalysis = true;
                    GerpCommand.isListGerp = true;
                    GerpCommand.isIncludeGerp = true;
                    break;
                case "--list-trap":
                    CommonCommand.isNonSampleAnalysis = true;
                    TrapCommand.isListTrap = true;
                    TrapCommand.isIncludeTrap = true;
                    break;
                case "--list-sub-rvis":
                    CommonCommand.isNonSampleAnalysis = true;
                    SubRvisCommand.isListSubRvis = true;
                    SubRvisCommand.isIncludeSubRvis = true;
                    break;
                case "--list-bis":
                    CommonCommand.isNonSampleAnalysis = true;
                    BisCommand.isListBis = true;
                    BisCommand.isIncludeBis = true;
                    break;
                case "--list-rvis":
                    CommonCommand.isNonSampleAnalysis = true;
                    RvisCommand.isListRvis = true;
                    RvisCommand.isIncludeRvis = true;
                    break;
                case "--list-1000-genomes":
                    CommonCommand.isNonSampleAnalysis = true;
                    GenomesCommand.isList1000Genomes = true;
                    GenomesCommand.isInclude1000Genomes = true;
                    break;
                case "--list-mgi":
                    CommonCommand.isNonSampleAnalysis = true;
                    MgiCommand.isListMgi = true;
                    MgiCommand.isIncludeMgi = true;
                    break;
                case "--list-denovo-db":
                    CommonCommand.isNonSampleAnalysis = true;
                    DenovoDBCommand.isListDenovoDB = true;
                    DenovoDBCommand.isIncludeDenovoDB = true;
                    break;
                case "--list-discovehr":
                    CommonCommand.isNonSampleAnalysis = true;
                    DiscovEHRCommand.isListDiscovEHR = true;
                    DiscovEHRCommand.isIncludeDiscovEHR = true;
                    break;
                case "--test":
                    // Test Functions
//                    CommonCommand.isNonDBAnalysis = true;
                    CommonCommand.isNonSampleAnalysis = true;
                    TestCommand.isTest = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
            hasMainFunction = true;
            break;
        }

        if (!hasMainFunction) {
            ErrorManager.print("Missing function command: --list-var-geno, --collapsing-dom, --collapsing-rec, "
                    + "--collapsing-comp-het, --fisher, --linear");
        }
    }

    private static void initSubOptions() throws Exception {
        if (VarGenoCommand.isListVarGeno) { // Genotype Analysis Functions
            VarGenoCommand.initOptions(optionList.iterator());
        } else if (VarCommand.isListVar) {

        } else if (CollapsingCommand.isCollapsingSingleVariant) {
            CollapsingCommand.initSingleVarOptions(optionList.iterator());
        } else if (CollapsingCommand.isCollapsingCompHet) {
            CollapsingCommand.initCompHetOptions(optionList.iterator());
        } else if (StatisticsCommand.isFisher) {
            StatisticsCommand.initFisherOptions(optionList.iterator());
        } else if (StatisticsCommand.isLinear) {
            StatisticsCommand.initLinearOptions(optionList.iterator());
        } else if (SiblingCommand.isSiblingCompHet) {

        } else if (TrioCommand.isListTrio) {
            TrioCommand.initOptions(optionList.iterator());
        } else if (ParentalCommand.isParentalMosaic) {
            ParentalCommand.initOptions(optionList.iterator());
        } else if (PedMapCommand.isPedMap) {
            PedMapCommand.initOptions(optionList.iterator());
        } else if (VarAnnoCommand.isListVarAnno) { // Variant Annotation Functions

        } else if (CoverageCommand.isCoverageSummary) { // Coverage Analysis Functions
            CoverageCommand.initCoverageSummary(optionList.iterator());
        } else if (CoverageCommand.isSiteCoverageSummary) {

        } else if (CoverageCommand.isCoverageComparison) {
            CoverageCommand.initCoverageComparison(optionList.iterator());
        } else if (CoverageCommand.isSiteCoverageComparison) {
            CoverageCommand.initCoverageComparisonSite(optionList.iterator());
        } else if (EvsCommand.isListEvs) { // External Datasets Functions

        } else if (GnomADCommand.isListGnomADExome) {

        } else if (KnownVarCommand.isListKnownVar) {

        } else if (FlankingCommand.isListFlankingSeq) {
            FlankingCommand.initOptions(optionList.iterator());
        } else if (TestCommand.isTest) { // Test Functions

        }
    }

    private static void initCommonOptions() throws Exception {
        VariantLevelFilterCommand.initOptions(optionList.iterator());

        AnnotationLevelFilterCommand.initOptions(optionList.iterator());

        GenotypeLevelFilterCommand.initOptions(optionList.iterator());
    }

    public static void outputInvalidOptions() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        boolean hasInvalid = false;

        while (iterator.hasNext()) {
            hasInvalid = true;

            option = (CommandOption) iterator.next();

            LogManager.writeAndPrint("Invalid option: " + option.getName());
        }

        if (hasInvalid) {
            ErrorManager.print("You have invalid options in your ATAV command.");
        }
    }

    /*
     * output invalid option & value if value > max or value < min ATAV stop
     */
    public static void checkValueValid(double max, double min, CommandOption option) {
        double value = Double.parseDouble(option.getValue());
        if (max != Data.NO_FILTER) {
            if (value > max) {
                outputInvalidOptionValue(option);
            }
        }

        if (min != Data.NO_FILTER) {
            if (value < min) {
                outputInvalidOptionValue(option);
            }
        }
    }

    /*
     * output invalid option & value if value is not in strList ATAV stop
     */
    public static void checkValueValid(String[] strList, CommandOption option) {
        for (String str : strList) {
            if (option.getValue().equals(str)) {
                return;
            }
        }

        outputInvalidOptionValue(option);
    }

    /*
     * output invalid option & value if value is not in strList ATAV stop
     */
    public static void checkValuesValid(String[] array, CommandOption option) {
        HashSet<String> set = new HashSet<>();

        set.addAll(Arrays.asList(array));

        String[] values = option.getValue().split(",");

        for (String str : values) {
            if (!set.contains(str)) {
                outputInvalidOptionValue(option);
            }
        }
    }

    /*
     * output invalid option & value if value is not a valid range
     */
    public static void checkRangeValid(String range, CommandOption option) {
        boolean isValid = false;

        String[] pos = range.split("-");
        double minStart = Double.valueOf(pos[0]);
        double maxEnd = Double.valueOf(pos[1]);

        if (option.getValue().contains("-")) {
            pos = option.getValue().split("-");
            double start = Double.valueOf(pos[0]);
            double end = Double.valueOf(pos[1]);

            if (start >= minStart && end <= maxEnd) {
                isValid = true;
            }
        }

        if (!isValid) {
            outputInvalidOptionValue(option);
        }
    }

    public static double[] getValidRange(CommandOption option) {
        double[] range = {0, 1};

        String[] pos = option.getValue().split("-");
        double start = Double.valueOf(pos[0]);
        double end = Double.valueOf(pos[1]);

        range[0] = start;
        range[1] = end;

        return range;
    }

    public static int getValidInteger(CommandOption option) {
        int i = 0;
        try {
            i = Integer.parseInt(option.getValue());
        } catch (NumberFormatException nfe) {
            outputInvalidOptionValue(option);
        }

        return i;
    }

    public static double getValidDouble(CommandOption option) {
        double i = 0;
        try {
            i = Double.parseDouble(option.getValue());
        } catch (NumberFormatException nfe) {
            outputInvalidOptionValue(option);
        }

        return i;
    }

    public static float getValidFloat(CommandOption option) {
        float i = 0;
        try {
            i = Float.parseFloat(option.getValue());
        } catch (NumberFormatException nfe) {
            outputInvalidOptionValue(option);
        }

        return i;
    }

    /*
     * output invalid option & value if value is a valid file path return either
     * a valid file path or a string value ATAV stop
     */
    public static String getValidPath(CommandOption option) {
        String path = option.getValue();

        if (!isFileExist(path)) {
            outputInvalidOptionValue(option);
        }

        return path;
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);

        if (file.exists()) {
            if (file.isFile()) {
                return true;
            } else if (file.isDirectory()) {
                return false;
            }
        }

        return false;
    }

    public static void outputInvalidOptionValue(CommandOption option) {
        ErrorManager.print("\nInvalid value '" + option.getValue()
                + "' for '" + option.getName() + "' option.");
    }
}
