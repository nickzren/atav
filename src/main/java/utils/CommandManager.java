package utils;

import global.Data;
import function.annotation.base.TranscriptManager;
import function.annotation.genedx.GeneDxCommand;
import function.annotation.varanno.VarAnnoCommand;
import function.coverage.base.CoverageCommand;
import function.external.evs.EvsCommand;
import function.external.exac.ExacCommand;
import function.external.flanking.FlankingCommand;
import function.external.knownvar.KnownVarCommand;
import function.genotype.collapsing.CollapsingCommand;
import function.genotype.family.FamilyCommand;
import function.genotype.parental.ParentalCommand;
import function.genotype.pedmap.PedMapCommand;
import function.genotype.sibling.SiblingCommand;
import function.genotype.statistics.StatisticsCommand;
import function.genotype.trio.TrioCommand;
import function.genotype.vargeno.VarGenoCommand;
import function.nondb.ppi.PPICommand;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author nick
 */
public class CommandManager {

    private static String[] optionArray;
    private static ArrayList<CommandOption> optionList = new ArrayList<CommandOption>();
    public static String command = "";
    private static String commandFile = "";

    private static void initCommand4Debug() {
        String cmd = "";

        optionArray = cmd.split("\\s+");
    }

    public static void initOptions(String[] options) {
        try {
            initCommandOptions(options);

            LogManager.initBasicInfo();

            initOptionList();

            initOutput();

            LogManager.initPath();

            initMainFunction();

            initVariantLevelFilterOptions();

            initAnnotationLevelFilterOptions();

            initGenotypeLevelFilterOptions();

            initMaf();

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
                System.out.println("\nError: without any input parameters to run ATAV. \n\nExit...\n");
                System.exit(0);
            }
        } else {
            // init options from command file or command line
            if (isCommandFileIncluded(options)) {
                initCommandFromFile();
            } else {
                optionArray = options;
            }
        }

        initCommand4Log();
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
        String version = Data.version;

        if (Data.version.contains(" ")) {
            version = Data.version.substring(Data.version.indexOf(" ") + 1);
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
            System.out.println("\nPlease specify output path: --out $PATH \n\nExit...\n");
            System.exit(0);
        }
    }

    private static void initOutputPath(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            } else {
                if (!dir.canWrite()) {
                    System.out.println("\nYou don't have write permissions into " + path + "! \n\nExit...\n");
                    System.exit(0);
                }
            }

            CommonCommand.realOutputPath = path;
            CommonCommand.outputDirName = dir.getName();
            CommonCommand.outputPath = path + File.separator + dir.getName() + "_";
        } catch (Exception e) {
            System.out.println("\nError in creating an output folder, caused by " + e.toString() + " \n\nExit...\n");
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

    private static void initMainFunction() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;
        boolean hasMainFunction = false;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();

            if (option.getName().equals("--list-var-geno")) { // Genotype Analysis Functions
                VarGenoCommand.isListVarGeno = true;
                VarGenoCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--collapsing-dom")) {
                CollapsingCommand.isCollapsingSingleVariant = true;
                CollapsingCommand.initSingleVarOptions(optionList.iterator());
            } else if (option.getName().equals("--collapsing-rec")) {
                CollapsingCommand.isCollapsingSingleVariant = true;
                CollapsingCommand.isRecessive = true;
                CollapsingCommand.initSingleVarOptions(optionList.iterator());
            } else if (option.getName().equals("--collapsing-comp-het")) {
                CollapsingCommand.isCollapsingCompHet = true;
                CollapsingCommand.initCompHetOptions(optionList.iterator());
            } else if (option.getName().equals("--fisher")) {
                StatisticsCommand.isFisher = true;
                StatisticsCommand.models = new String[4];
                StatisticsCommand.models[0] = "allelic";
                StatisticsCommand.models[1] = "dominant";
                StatisticsCommand.models[2] = "recessive";
                StatisticsCommand.models[3] = "genotypic";
                StatisticsCommand.initFisherOptions(optionList.iterator());
            } else if (option.getName().equals("--linear")) {
                StatisticsCommand.isLinear = true;
                StatisticsCommand.models = new String[4];
                StatisticsCommand.models[0] = "allelic";
                StatisticsCommand.models[1] = "dominant";
                StatisticsCommand.models[2] = "recessive";
                StatisticsCommand.models[3] = "additive";
                StatisticsCommand.initLinearOptions(optionList.iterator());
            } else if (option.getName().equals("--family-analysis")) {
                FamilyCommand.isFamilyAnalysis = true;
                FamilyCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--list-sibling-comp-het")) {
                SiblingCommand.isSiblingCompHet = true;
            } else if (option.getName().equals("--list-trio-denovo")) {
                TrioCommand.isTrioDenovo = true;
                TrioCommand.initDenovoOptions(optionList.iterator());
            } else if (option.getName().equals("--list-trio-comp-het")) {
                TrioCommand.isTrioCompHet = true;
                TrioCommand.initCompHetOptions(optionList.iterator());
            } else if (option.getName().equals("--parental-mosaic")) {
                ParentalCommand.isParentalMosaic = true;
                ParentalCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--ped-map")) {
                PedMapCommand.isPedMap = true;
                PedMapCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--list-var-anno")) { // Variant Annotation Functions
                CommonCommand.isNonSampleAnalysis = true;
                VarAnnoCommand.isListVarAnno = true;
            } else if (option.getName().equals("--list-gene-dx")) {
                CommonCommand.isNonSampleAnalysis = true;
                GeneDxCommand.isListGeneDx = true;
            } else if (option.getName().equals("--coverage-summary")) { // Coverage Analysis Functions
                CoverageCommand.isCoverageSummary = true;
                CoverageCommand.initCoverageSummary(optionList.iterator());
            } else if (option.getName().equals("--site-coverage-summary")) {
                CoverageCommand.isSiteCoverageSummary = true;
                CoverageCommand.initSiteCoverageSummary(optionList.iterator());
            } else if (option.getName().equals("--coverage-comparison")) {
                CoverageCommand.isCoverageComparison = true;
                CoverageCommand.initCoverageComparison(optionList.iterator());
            } else if (option.getName().equals("--coverage-summary-pipeline")) {
                CoverageCommand.isCoverageSummaryPipeline = true;
                CoverageCommand.initCoverageComparison(optionList.iterator());
            } else if (option.getName().equals("--list-evs")) { // External Datasets Functions
                CommonCommand.isNonSampleAnalysis = true;
                CommonCommand.isOldEvsUsed = true;
                EvsCommand.isListEvs = true;
            } else if (option.getName().equals("--jon-evs-tool")) {
                CommonCommand.isNonSampleAnalysis = true;
                EvsCommand.isJonEvsTool = true;
                CommonCommand.isOldEvsUsed = true;
                EvsCommand.initJonEvsToolOptions(optionList.iterator());
            } else if (option.getName().equals("--list-known-var")) {
                CommonCommand.isNonSampleAnalysis = true;
                KnownVarCommand.isListKnownVar = true;
                KnownVarCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--list-exac")) {
                CommonCommand.isNonSampleAnalysis = true;
                ExacCommand.isListExac = true;
            } else if (option.getName().equals("--list-flanking-seq")) {
                CommonCommand.isNonSampleAnalysis = true;
                FlankingCommand.isListFlankingSeq = true;
                FlankingCommand.initOptions(optionList.iterator());
            } else if (option.getName().equals("--ppi")) { // Non Database Functions
                PPICommand.isPPI = true;
                CommonCommand.isNonDBAnalysis = true;
                CommonCommand.isNonSampleAnalysis = true;
                PPICommand.initOptions(optionList.iterator());
            } else {
                continue;
            }

            iterator.remove();
            hasMainFunction = true;
            break;
        }

        if (!hasMainFunction) {
            ErrorManager.print("Missing function command: --ped-map, "
                    + "--fisher, --linear, --collapsing-dom, --collapsing-rec, "
                    + "--collapsing-comp-het, --var-list, --list-trio-denovo, "
                    + "--list-trio-comp-het, --family-analysis, --coverage-summary...");
        }
    }

    private static void initVariantLevelFilterOptions() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--region")) {
                CommonCommand.regionInput = option.getValue();
            } else if (option.getName().equals("--variant")) {
                CommonCommand.includeVariantId = getValidPath(option);
            } else if (option.getName().equals("--exclude-variant")) {
                CommonCommand.excludeVariantId = getValidPath(option);
            } else if (option.getName().equals("--exclude-artifacts")) {
                CommonCommand.isExcludeArtifacts = true;
            } else if (option.getName().equals("--exclude-snv")) {
                CommonCommand.isExcludeSnv = true;
            } else if (option.getName().equals("--exclude-indel")) {
                CommonCommand.isExcludeIndel = true;
            } else if (option.getName().equals("--evs-pop")
                    || option.getName().equals("--evs-maf-pop")) {
                checkValuesValid(Data.EVS_POP, option);
                CommonCommand.evsMafPop = option.getValue();
            } else if (option.getName().equals("--evs-maf")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.evsMaf = getValidDouble(option);
                CommonCommand.isOldEvsUsed = true;
            } else if (option.getName().equals("--evs-mhgf-rec")
                    || option.getName().equals("--evs-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.evsMhgf4Recessive = getValidDouble(option);
                CommonCommand.isOldEvsUsed = true;
            } else if (option.getName().equals("--exclude-evs-qc-failed")) {
                CommonCommand.isExcludeEvsQcFailed = true;
                CommonCommand.isOldEvsUsed = true;
            } else if (option.getName().equals("--exac-pop")) {
                checkValuesValid(Data.EXAC_POP, option);
                CommonCommand.exacPop = option.getValue();
            } else if (option.getName().equals("--exac-maf")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.exacMaf = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-snv")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.exacVqslodSnv = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-indel")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.exacVqslodIndel = getValidFloat(option);
            } else if (option.getName().equals("--min-c-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.minCscore = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initAnnotationLevelFilterOptions() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--function")) {
                CommonCommand.functionInput = getValidPath(option);
            } else if (option.getName().equals("--gene")) {
                CommonCommand.geneInput = getValidPath(option);
            } else if (option.getName().equals("--transcript")) {
                CommonCommand.transcriptFile = getValidPath(option);
            } else if (option.getName().equals("--ccds-only")) {
                CommonCommand.isCcdsOnly = true;
                TranscriptManager.initCCDSTranscriptPath();
            } else if (option.getName().equals("--canonical-only")) {
                CommonCommand.isCanonicalOnly = true;
                TranscriptManager.initCanonicalTranscriptPath();
            } else if (option.getName().equals("--polyphen")
                    || option.getName().equals("--polyphen-humdiv")) {
                checkValuesValid(Data.POLYPHEN_CAT, option);
                CommonCommand.polyphenHumdiv = option.getValue();
            } else if (option.getName().equals("--polyphen-humvar")) {
                checkValuesValid(Data.POLYPHEN_CAT, option);
                CommonCommand.polyphenHumvar = option.getValue();
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initGenotypeLevelFilterOptions() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--sample")
                    || option.getName().equals("--pedinfo")) {
                CommonCommand.sampleFile = getValidPath(option);
            } else if (option.getName().equals("--all-sample")) {
                CommonCommand.isAllSample = true;
            } else if (option.getName().equals("--all-non-ref")) {
                CommonCommand.isAllNonRef = true;
            } else if (option.getName().equals("--include-evs-sample")) {
                checkValueValid(Data.EVS_POP, option);
                CommonCommand.evsSample = option.getValue();
            } else if (option.getName().equals("--exclude-artifacts")) {
                CommonCommand.isExcludeArtifacts = true;
            } else if (option.getName().equals("--ctrlMAF")
                    || option.getName().equals("--ctrl-maf")) {
                checkValueValid(0.5, 0, option);
                CommonCommand.ctrlMaf = getValidDouble(option);
            } else if (option.getName().equals("--ctrl-maf-rec")
                    || option.getName().equals("--ctrl-maf-recessive")) {
                CommonCommand.maf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--ctrl-mhgf-rec")
                    || option.getName().equals("--ctrl-mhgf-recessive")) {
                CommonCommand.mhgf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--min-coverage")) {
                checkValueValid(new String[]{"0", "3", "10", "20", "201"}, option);
                CommonCommand.minCoverage = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.minCaseCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                CommonCommand.minCaseCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.minCtrlCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                CommonCommand.minCtrlCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--min-variant-present")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.minVarPresent = getValidInteger(option);
            } else if (option.getName().equals("--min-case-carrier")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.minCaseCarrier = getValidInteger(option);
            } else if (option.getName().equals("--var-status")) {
                checkValueValid(Data.VARIANT_STATUS, option);
                String varStatus = option.getValue().replace("+", ",");
                if (varStatus.contains("all")) {
                    CommonCommand.varStatus = null;
                } else {
                    CommonCommand.varStatus = varStatus.split(",");
                }
            } else if (option.getName().equals("--het-percent-alt-read")) {
                checkRangeValid("0-1", option);
                CommonCommand.hetPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--hom-percent-alt-read")) {
                checkRangeValid("0-1", option);
                CommonCommand.homPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--gq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.genotypeQualGQ = getValidDouble(option);
            } else if (option.getName().equals("--fs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.strandBiasFS = getValidDouble(option);
            } else if (option.getName().equals("--hap-score")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.haplotypeScore = getValidDouble(option);
            } else if (option.getName().equals("--mq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.rmsMapQualMQ = getValidDouble(option);
            } else if (option.getName().equals("--qd")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.qualByDepthQD = getValidDouble(option);
            } else if (option.getName().equals("--qual")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.qual = getValidDouble(option);
            } else if (option.getName().equals("--rprs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.readPosRankSum = getValidDouble(option);
            } else if (option.getName().equals("--mqrs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommonCommand.mapQualRankSum = getValidDouble(option);
            } else if (option.getName().equals("--include-qc-missing")) {
                CommonCommand.isQcMissingIncluded = true;
            } else if (option.getName().equals("--max-qc-fail-sample")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommonCommand.maxQcFailSample = getValidInteger(option);
            } else {
                continue;
            }

            iterator.remove();
        }

        initMinCoverage();
    }

    private static void initMinCoverage() {
        if (CommonCommand.minCoverage != Data.NO_FILTER) {
            if (CommonCommand.minCaseCoverageCall == Data.NO_FILTER) {
                CommonCommand.minCaseCoverageCall = CommonCommand.minCoverage;
            }

            if (CommonCommand.minCaseCoverageNoCall == Data.NO_FILTER) {
                CommonCommand.minCaseCoverageNoCall = CommonCommand.minCoverage;
            }

            if (CommonCommand.minCtrlCoverageCall == Data.NO_FILTER) {
                CommonCommand.minCtrlCoverageCall = CommonCommand.minCoverage;
            }

            if (CommonCommand.minCtrlCoverageNoCall == Data.NO_FILTER) {
                CommonCommand.minCtrlCoverageNoCall = CommonCommand.minCoverage;
            }
        }
    }

    private static void initMaf() {
        if (CollapsingCommand.looMaf != Data.NO_FILTER) {
            CommonCommand.maf = CollapsingCommand.looMaf;
        } else if (FamilyCommand.popCtrlMaf != Data.NO_FILTER) {
            CommonCommand.maf = FamilyCommand.popCtrlMaf;
        } else {
            CommonCommand.maf = CommonCommand.ctrlMaf;
        }

        if (CommonCommand.maf4Recessive == Data.NO_FILTER) { // need to be changed for pop
            CommonCommand.maf4Recessive = CommonCommand.maf;
        }
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
        HashSet<String> set = new HashSet<String>();

        for (String str : array) {
            set.add(str);
        }

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

    private static boolean isFileExist(String path) {
        if (path.isEmpty()) {
            return false;
        }

        if (path.contains(File.separator)) {
            File file = new File(path);
            if (!file.isFile()) {
                return false;
            }
        }

        return true;
    }

    public static void outputInvalidOptionValue(CommandOption option) {
        ErrorManager.print("\nInvalid value '" + option.getValue()
                + "' for '" + option.getName() + "' option.");
    }
}
