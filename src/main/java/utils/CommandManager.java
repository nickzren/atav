package utils;

import function.genotype.family.FamilyManager;
import global.Data;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.sql.SQLException;
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

            initDataFilter();

            initQualityFilter();

            initMainFunctionSubOptions();

            initMaf();

            initOptions4Debug();

            outputInvalidOptions();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initCommandOptions(String[] options) {
        if (options.length == 0) {
            if (CommandValue.isDebug) {
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

        if (CommandValue.outputPath.isEmpty()) {
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

            CommandValue.realOutputPath = path;
            CommandValue.outputDirName = dir.getName();
            CommandValue.outputPath = path + File.separator + dir.getName() + "_";
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
                CommandValue.isDebug = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    /*
     * main functions: --ped-map, --fisher, --collapsing, --var-list,
     * --list-trio-denovo, --list-trio-comp-het, --family-analysis,
     * --coverage-summary
     */
    private static void initMainFunction() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;
        boolean hasMainFunction = false;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();

            if (option.getName().equals("--ped-map")) {
                CommandValue.isPedMap = true;
            } else if (option.getName().equals("--fisher")) {
                CommandValue.isFisher = true;
                CommandValue.models = new String[4];
                CommandValue.models[0] = "allelic";
                CommandValue.models[1] = "dominant";
                CommandValue.models[2] = "recessive";
                CommandValue.models[3] = "genotypic";
            } else if (option.getName().equals("--linear")) {
                CommandValue.isLinear = true;
                CommandValue.models = new String[4];
                CommandValue.models[0] = "allelic";
                CommandValue.models[1] = "dominant";
                CommandValue.models[2] = "recessive";

                CommandValue.models[3] = "additive";
                //CommandValue.models[4] = "genotypic";

            } else if (option.getName().equals("--fisher-allelic")) {
                CommandValue.isFisher = true;
                CommandValue.models = new String[1];
                CommandValue.models[0] = "allelic";
            } else if (option.getName().equals("--fisher-dom")) {
                CommandValue.isFisher = true;
                CommandValue.models = new String[1];
                CommandValue.models[0] = "dominant";
            } else if (option.getName().equals("--fisher-rec")) {
                CommandValue.isFisher = true;
                CommandValue.models = new String[1];
                CommandValue.models[0] = "recessive";
            } else if (option.getName().equals("--fisher-gen")) {
                CommandValue.isFisher = true;
                CommandValue.models = new String[1];
                CommandValue.models[0] = "genotypic";
            } else if (option.getName().equals("--collapsing-dom")
                    || option.getName().equals("--collapsing")
                    || option.getName().equals("--collapsing-trunk")) {
                CommandValue.isCollapsingSingleVariant = true;
            } else if (option.getName().equals("--collapsing-rec")) {
                CommandValue.isCollapsingSingleVariant = true;
                CommandValue.isRecessive = true;
            } else if (option.getName().equals("--collapsing-comp-het")) {
                CommandValue.isCollapsingCompHet = true;
            } else if (option.getName().equals("--var-list")
                    || option.getName().equals("--list-var-geno")) {
                CommandValue.isListVarGeno = true;
            } else if (option.getName().equals("--list-var-anno")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isListVarAnno = true;
            } else if (option.getName().equals("--list-gene-dx")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isListGeneDx = true;
            } else if (option.getName().equals("--list-flanking-seq")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isListFlankingSeq = true;
            } else if (option.getName().equals("--jon-evs-tool")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isJonEvsTool = true;
                CommandValue.isOldEvsUsed = true;
            } else if (option.getName().equals("--list-known-var")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isListKnownVar = true;
            } else if (option.getName().equals("--list-evs")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isOldEvsUsed = true;
                CommandValue.isListEvs = true;
            } else if (option.getName().equals("--list-exac")) {
                CommandValue.isNonSampleAnalysis = true;
                CommandValue.isListExac = true;
            } else if (option.getName().equals("--list-trio-denovo")) {
                CommandValue.isTrioDenovo = true;
            } else if (option.getName().equals("--list-trio-comp-het")) {
                CommandValue.isTrioCompHet = true;
            } else if (option.getName().equals("--family-analysis")) {
                CommandValue.isFamilyAnalysis = true;
            } else if (option.getName().equals("--coverage-summary")) {
                CommandValue.isCoverageSummary = true;
            } else if (option.getName().equals("--site-coverage-summary")) {
                CommandValue.isSiteCoverageSummary = true;
            } else if (option.getName().equals("--coverage-comparison")) {
                CommandValue.isCoverageComparison = true;
            } else if (option.getName().equals("--coverage-summary-pipeline")) {
                CommandValue.isCoverageSummaryPipeline = true;
            } else if (option.getName().equals("--list-sibling-comp-het")) {
                CommandValue.isSiblingCompHet = true;
            } else if (option.getName().equals("--parental-mosaic")) {
                CommandValue.isParentalMosaic = true;
            } else {
                continue;
            }

            iterator.remove();
            hasMainFunction = true;
            break;
        }

        if (!hasMainFunction) {
            ErrorManager.print("Missing main functions: --ped-map, "
                    + "--fisher, --linear, --collapsing-dom, --collapsing-rec, "
                    + "--collapsing-comp-het, --var-list, --list-trio-denovo, "
                    + "--list-trio-comp-het, --family-analysis, --coverage-summary...");
        }
    }

    private static void initDataFilter() throws SQLException, FileNotFoundException, Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--function")) {
                CommandValue.functionInput = getValidPath(option);
            } else if (option.getName().equals("--sample")
                    || option.getName().equals("--pedinfo")) {
                CommandValue.sampleFile = getValidPath(option);
            } else if (option.getName().equals("--all-sample")) {
                CommandValue.isAllSample = true;
            } else if (option.getName().equals("--variant-input-file")) {
                CommandValue.variantInputFile = getValidPath(option);
            } else if (option.getName().equals("--variant")) {
                CommandValue.includeVariantId = getValidPath(option);
            } else if (option.getName().equals("--exclude-variant")) {
                CommandValue.excludeVariantId = getValidPath(option);
            } else if (option.getName().equals("--gene")) {
                CommandValue.geneInput = getValidPath(option);
            } else if (option.getName().equals("--transcript")) {
                CommandValue.transcriptFile = getValidPath(option);
            } else if (option.getName().equals("--region")) {
                CommandValue.regionInput = option.getValue();
            } else if (option.getName().equals("--exclude-snv")) {
                CommandValue.isExcludeSnv = true;
            } else if (option.getName().equals("--exclude-indel")) {
                CommandValue.isExcludeIndel = true;
            } else if (option.getName().equals("--all-non-ref")) {
                CommandValue.isAllNonRef = true;
            } else if (option.getName().equals("--include-all-geno")) {
                CommandValue.isIncludeAllGeno = true;
            } else if (option.getName().equals("--exclude-artifacts")) {
                CommandValue.isExcludeArtifacts = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initQualityFilter() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();

            if (option.getName().equals("--min-coverage")) {
                checkValueValid(new String[]{"0", "3", "10", "20", "201"}, option);
                CommandValue.minCoverage = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minCaseCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-case-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                CommandValue.minCaseCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-call")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minCtrlCoverageCall = getValidInteger(option);
            } else if (option.getName().equals("--min-ctrl-coverage-no-call")) {
                checkValueValid(new String[]{"3", "10", "20", "201"}, option);
                CommandValue.minCtrlCoverageNoCall = getValidInteger(option);
            } else if (option.getName().equals("--ctrlMAF")
                    || option.getName().equals("--ctrl-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.ctrlMaf = getValidDouble(option);
            } else if (option.getName().equals("--ctrl-maf-rec")
                    || option.getName().equals("--ctrl-maf-recessive")) {
                CommandValue.maf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--ctrl-mhgf-rec")
                    || option.getName().equals("--ctrl-mhgf-recessive")) {
                CommandValue.mhgf4Recessive = getValidDouble(option);
                checkValueValid(0.5, 0, option);
            } else if (option.getName().equals("--include-evs-sample")) {
                checkValueValid(Data.EVS_POP, option);
                CommandValue.evsSample = option.getValue();
            } else if (option.getName().equals("--evs-pop")
                    || option.getName().equals("--evs-maf-pop")) {
                checkValuesValid(Data.EVS_POP, option);
                CommandValue.evsMafPop = option.getValue();
            } else if (option.getName().equals("--evs-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.evsMaf = getValidDouble(option);
                CommandValue.isOldEvsUsed = true;
            } else if (option.getName().equals("--evs-mhgf-rec")
                    || option.getName().equals("--evs-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.evsMhgf4Recessive = getValidDouble(option);
                CommandValue.isOldEvsUsed = true;
            } else if (option.getName().equals("--exclude-evs-qc-failed")) {
                CommandValue.isExcludeEvsQcFailed = true;
                CommandValue.isOldEvsUsed = true;
            } else if (option.getName().equals("--exac-pop")) {
                checkValuesValid(Data.EXAC_POP, option);
                CommandValue.exacPop = option.getValue();
            } else if (option.getName().equals("--exac-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.exacMaf = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-snv")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.exacVqslodSnv = getValidFloat(option);
            } else if (option.getName().equals("--min-exac-vqslod-indel")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.exacVqslodIndel = getValidFloat(option);
            } else if (option.getName().equals("--var-status")) {
                checkValueValid(Data.VARIANT_STATUS, option);
                String varStatus = option.getValue().replace("+", ",");
                if (varStatus.contains("all")) {
                    CommandValue.varStatus = null;
                } else {
                    CommandValue.varStatus = varStatus.split(",");
                }
            } else if (option.getName().equals("--min-variant-present")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minVarPresent = getValidInteger(option);
            } else if (option.getName().equals("--min-case-carrier")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minCaseCarrier = getValidInteger(option);
            } else if (option.getName().equals("--gq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.genotypeQualGQ = getValidDouble(option);
            } else if (option.getName().equals("--fs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.strandBiasFS = getValidDouble(option);
            } else if (option.getName().equals("--hap-score")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.haplotypeScore = getValidDouble(option);
            } else if (option.getName().equals("--mq")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.rmsMapQualMQ = getValidDouble(option);
            } else if (option.getName().equals("--qd")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.qualByDepthQD = getValidDouble(option);
            } else if (option.getName().equals("--qual")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.qual = getValidDouble(option);
            } else if (option.getName().equals("--rprs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.readPosRankSum = getValidDouble(option);
            } else if (option.getName().equals("--mqrs")) {
                checkValueValid(Data.NO_FILTER, Data.NO_FILTER, option);
                CommandValue.mapQualRankSum = getValidDouble(option);
            } else if (option.getName().equals("--polyphen")
                    || option.getName().equals("--polyphen-humdiv")) {
                checkValuesValid(Data.POLYPHEN_CAT, option);
                CommandValue.polyphenHumdiv = option.getValue();
            } else if (option.getName().equals("--polyphen-humvar")) {
                checkValuesValid(Data.POLYPHEN_CAT, option);
                CommandValue.polyphenHumvar = option.getValue();
            } else if (option.getName().equals("--ccds-only")) {
                CommandValue.isCcdsOnly = true;
                TranscriptManager.initCCDSTranscriptPath();
            } else if (option.getName().equals("--canonical-only")) {
                CommandValue.isCanonicalOnly = true;
                TranscriptManager.initCanonicalTranscriptPath();
            } else if (option.getName().equals("--het-percent-alt-read")) {
                checkRangeValid("0-1", option);
                CommandValue.hetPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--hom-percent-alt-read")) {
                checkRangeValid("0-1", option);
                CommandValue.homPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--min-c-score")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minCscore = getValidDouble(option);
            } else if (option.getName().equals("--flip-maf")) {
                CommandValue.isFlipMaf = true;
            } else if (option.getName().equals("--include-qc-missing")) {
                CommandValue.isQcMissingIncluded = true;
            } else {
                continue;
            }

            iterator.remove();
        }

        initMinCoverage();
    }

    private static void initMinCoverage() {
        if (CommandValue.minCoverage != Data.NO_FILTER) {
            if (CommandValue.minCaseCoverageCall == Data.NO_FILTER) {
                CommandValue.minCaseCoverageCall = CommandValue.minCoverage;
            }

            if (CommandValue.minCaseCoverageNoCall == Data.NO_FILTER) {
                CommandValue.minCaseCoverageNoCall = CommandValue.minCoverage;
            }

            if (CommandValue.minCtrlCoverageCall == Data.NO_FILTER) {
                CommandValue.minCtrlCoverageCall = CommandValue.minCoverage;
            }

            if (CommandValue.minCtrlCoverageNoCall == Data.NO_FILTER) {
                CommandValue.minCtrlCoverageNoCall = CommandValue.minCoverage;
            }
        }
    }

    private static void initMaf() {
        if (CommandValue.looMaf != Data.NO_FILTER) {
            CommandValue.maf = CommandValue.looMaf;
        } else if (CommandValue.popCtrlMaf != Data.NO_FILTER) {
            CommandValue.maf = CommandValue.popCtrlMaf;
        } else {
            CommandValue.maf = CommandValue.ctrlMaf;
        }

        if (CommandValue.maf4Recessive == Data.NO_FILTER) { // need to be changed for pop
            CommandValue.maf4Recessive = CommandValue.maf;
        }
    }

    private static void initPedMap() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--variant-id-only")) {
                CommandValue.isVariantIdOnly = true;
            } else if (option.getName().equals("--combine-multiple-alleles")) {
                CommandValue.isCombineMultiAlleles = true;
            } else if (option.getName().equals("--eigenstrat")) {
                CommandValue.isEigenstrat = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initFisher() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--threshold-sort")) {
                CommandValue.threshold4Sort = getValidDouble(option);
                checkValueValid(1, 0, option);
            } else if (option.getName().equals("--case-only")) {
                CommandValue.isCaseOnly = true;
            } else if (option.getName().equals("--min-hom-case-rec")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minHomCaseRec = getValidInteger(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initLinear() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--threshold-sort")) {
                CommandValue.threshold4Sort = getValidDouble(option);
                checkValueValid(1, 0, option);
            } else if (option.getName().equals("--covariate")) {
                CommandValue.covariateFile = getValidPath(option);
            } else if (option.getName().equals("--quantitative")) {
                CommandValue.quantitativeFile = getValidPath(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initCollapsingSingleVariant() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--var-missing-rate")) {
                CommandValue.varMissingRate = getValidDouble(option);
            } else if (option.getName().equals("--loo-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.looMaf = getValidDouble(option);
            } else if (option.getName().equals("--loo-maf-rec")
                    || option.getName().equals("--loo-maf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.maf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--loo-mhgf-rec")
                    || option.getName().equals("--loo-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.mhgf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--gene-boundaries")) {
                CommandValue.geneBoundariesFile = getValidPath(option);
            } else if (option.getName().equals("--read-coverage-summary")) {
                CommandValue.coverageSummaryFile = getValidPath(option);
                GeneManager.initCoverageSummary();
            } else if (option.getName().equals("--covariate")) {
                CommandValue.covariateFile = getValidPath(option);
                CommandValue.isCollapsingDoLogistic = true;
            } else if (option.getName().equals("--quantitative")) {
                CommandValue.quantitativeFile = getValidPath(option);
                CommandValue.isCollapsingDoLinear = true;
            } else if (option.getName().equals("--min-hom-case-rec")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.minHomCaseRec = getValidInteger(option);
            } else {
                continue;
            }

            iterator.remove();
        }

        if (CommandValue.isCollapsingDoLinear) {
            CommandValue.isCollapsingDoLogistic = false;
        }
    }

    private static void initCollapsingCompHet() throws Exception {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--loo-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.looMaf = getValidDouble(option);
            } else if (option.getName().equals("--loo-maf-rec")
                    || option.getName().equals("--loo-maf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.maf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--loo-mhgf-rec")
                    || option.getName().equals("--loo-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.mhgf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--loo-comb-freq")) {
                checkValueValid(1, 0, option);
                CommandValue.looCombFreq = getValidDouble(option);
            } else if (option.getName().equals("--gene-boundaries")) {
                CommandValue.geneBoundariesFile = getValidPath(option);
            } else if (option.getName().equals("--read-coverage-summary")) {
                CommandValue.coverageSummaryFile = getValidPath(option);
                GeneManager.initCoverageSummary();
            } else if (option.getName().equals("--covariate")) {
                CommandValue.covariateFile = getValidPath(option);
                CommandValue.isCollapsingDoLogistic = true;
            } else if (option.getName().equals("--quantitative")) {
                CommandValue.quantitativeFile = getValidPath(option);
                CommandValue.isCollapsingDoLinear = true;
            } else {
                continue;
            }

            iterator.remove();
        }

        if (CommandValue.isCollapsingDoLinear) {
            CommandValue.isCollapsingDoLogistic = false;
        }
    }

    private static void initListVarGeno() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--case-only")) {
                CommandValue.isCaseOnly = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initDenovo() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--include-noflag")) {
                CommandValue.isIncludeNoflag = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initCompHet() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--combfreq")
                    || option.getName().equals("--comb-freq")) {
                checkValueValid(1, 0, option);
                CommandValue.combFreq = getValidDouble(option);
            } else if (option.getName().equals("--include-noflag")) {
                CommandValue.isIncludeNoflag = true;
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initFamilyAnalysis() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--family-id")) { // need change here
                CommandValue.familyId = getValidPath(option);
                FamilyManager.initFamilyIdList();
            } else if (option.getName().equals("--pop-ctrl-maf")) {
                checkValueValid(0.5, 0, option);
                CommandValue.popCtrlMaf = getValidDouble(option);
            } else if (option.getName().equals("--pop-ctrl-maf-rec")
                    || option.getName().equals("--pop-ctrl-maf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.maf4Recessive = getValidDouble(option);
            } else if (option.getName().equals("--pop-ctrl-mhgf-rec")
                    || option.getName().equals("--pop-ctrl-mhgf-recessive")) {
                checkValueValid(0.5, 0, option);
                CommandValue.mhgf4Recessive = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }

        if (CommandValue.familyId.isEmpty()) {
            ErrorManager.print("Please specify a family id by using '--family-id'.");
        }
    }

    private static void initCoverageSummaryPipeline() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) { //this will be the only option allowed for pipeline
                CommandValue.coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--terse")) {
                CommandValue.isTerse = true;
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initCoverageComparison() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;
        CommandValue.isByExon = true;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--gene-boundaries")) {
                CommandValue.coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--exon-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                CommandValue.exonCleanCutoff = getValidDouble(option);
            } else if (option.getName().equals("--gene-max-percent-cov-difference")) {
                checkValueValid(1, 0, option);
                CommandValue.geneCleanCutoff = getValidDouble(option);
            } else if (option.getName().equals("--quantitative")) {
                CommandValue.isCoverageComparisonDoLinear = true;
                CommandValue.quantitativeFile = getValidPath(option);
            } else if (option.getName().equals("--exon-max-cov-diff-p-value")) {
                checkValueValid(1, 0, option);
                CommandValue.ExonMaxCovDiffPValue = getValidDouble(option);
            } else if (option.getName().equals("--exon-max-percent-var-explained")) {
                checkValueValid(100, 0, option);
                CommandValue.ExonMaxPercentVarExplained = getValidDouble(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initCoverageSummary() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) {
                CommandValue.coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--percent-region-covered")) {
                CommandValue.minPercentRegionCovered = getValidDouble(option);
            } else if (option.getName().equals("--exclude-utr")) {
                CommandValue.isExcludeUTR = true;
            } else if (option.getName().equals("--by-exon")
                    || option.getName().equals("--include-exon-file")) {
                CommandValue.isByExon = true;
            } else if (option.getName().equals("--terse")) {
                CommandValue.isTerse = true;
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initSiteCoverageSummary() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--covered-region")) {
                CommandValue.coveredRegionFile = getValidPath(option);
            } else if (option.getName().equals("--gene-boundaries")) {
                CommandValue.coveredRegionFile = getValidPath(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initListFlankingSeq() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.width = getValidInteger(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initJonEvsTool() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--jon-evs-input")) {
                CommandValue.jonEvsInput = getValidPath(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initKnownVar() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--snv-width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.snvWidth = getValidInteger(option);
            } else if (option.getName().equals("--indel-width")) {
                checkValueValid(Data.NO_FILTER, 0, option);
                CommandValue.indelWidth = getValidInteger(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }

    private static void initParentalMosaic() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--proband-qd")) {
                CommandValue.probandQD = getValidDouble(option);
            } else if (option.getName().equals("--proband-het-percent-alt-read")) {
                checkRangeValid("0-1", option);
                CommandValue.probandHetPercentAltRead = getValidRange(option);
            } else if (option.getName().equals("--proband-binomial")) {
                CommandValue.probandBinomial = getValidDouble(option);
            } else {
                continue;
            }

            iterator.remove();
        }
    }

    private static void initMainFunctionSubOptions() throws Exception {
        if (CommandValue.isPedMap) {
            initPedMap();
        } else if (CommandValue.isFisher) {
            initFisher();
        } else if (CommandValue.isLinear) {
            initLinear();
        } else if (CommandValue.isCollapsingSingleVariant) {
            initCollapsingSingleVariant();
        } else if (CommandValue.isCollapsingCompHet) {
            initCollapsingCompHet();
        } else if (CommandValue.isListVarGeno) {
            initListVarGeno();
        } else if (CommandValue.isTrioDenovo) {
            initDenovo();
        } else if (CommandValue.isTrioCompHet) {
            initCompHet();
        } else if (CommandValue.isFamilyAnalysis) {
            initFamilyAnalysis();
        } else if (CommandValue.isListFlankingSeq) {
            initListFlankingSeq();
        } else if (CommandValue.isJonEvsTool) {
            initJonEvsTool();
        } else if (CommandValue.isCoverageSummary) {
            initCoverageSummary();
        } else if (CommandValue.isSiteCoverageSummary) {
            initSiteCoverageSummary();
        } else if (CommandValue.isCoverageComparison) {
            initCoverageComparison();
        } else if (CommandValue.isCoverageSummaryPipeline) {
            initCoverageSummaryPipeline();
        } else if (CommandValue.isListKnownVar) {
            initKnownVar();
        } else if (CommandValue.isParentalMosaic) {
            initParentalMosaic();
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
