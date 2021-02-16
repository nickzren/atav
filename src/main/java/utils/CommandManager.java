package utils;

import function.annotation.base.AnnotationLevelFilterCommand;
import global.Data;
import function.annotation.varanno.VarAnnoCommand;
import function.cohort.af.AFCommand;
import function.coverage.base.CoverageCommand;
import function.external.ccr.CCRCommand;
import function.external.limbr.LIMBRCommand;
import function.external.denovo.DenovoDBCommand;
import function.external.discovehr.DiscovEHRCommand;
import function.external.evs.EvsCommand;
import function.external.exac.ExACCommand;
import function.external.gnomad.GnomADCommand;
import function.external.gerp.GerpCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.mgi.MgiCommand;
import function.external.mtr.MTRCommand;
import function.external.primateai.PrimateAICommand;
import function.external.revel.RevelCommand;
import function.external.rvis.RvisCommand;
import function.external.subrvis.SubRvisCommand;
import function.external.trap.TrapCommand;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.collapsing.CollapsingCommand;
import function.cohort.parent.ParentCommand;
import function.cohort.parental.ParentalCommand;
import function.cohort.pedmap.PedMapCommand;
import function.cohort.sibling.SiblingCommand;
import function.cohort.statistics.StatisticsCommand;
import function.cohort.trio.TrioCommand;
import function.cohort.var.VarCommand;
import function.cohort.vargeno.VarGenoCommand;
import function.cohort.vcf.VCFCommand;
import function.external.chm.CHMCommand;
import function.external.dbnsfp.DBNSFPCommand;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.gevir.GeVIRCommand;
import function.external.gme.GMECommand;
import function.external.gnomad.GnomADExomeCommand;
import function.external.gnomad.GnomADGenomeCommand;
import function.external.igmaf.IGMAFCommand;
import function.external.iranome.IranomeCommand;
import function.external.mpc.MPCCommand;
import function.external.pext.PextCommand;
import function.external.synrvis.SynRvisCommand;
import function.external.topmed.TopMedCommand;
import function.test.TestCommand;
import function.variant.base.VariantLevelFilterCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String cmd = "--region 21 --disable-timestamp-from-out-path --sample data/sample/sample.txt "
                + "--collapsing-dom --mann-whitney-test --gene-boundaries data/ccds/addjusted.CCDS.genes.index.r20.hg19.txt --include-rvis --include-known-var --effect HIGH:exon_lo"
                + "ss_variant,HIGH:frameshift_variant,HIGH:rare_amino_acid_variant,HIGH:stop_gained,HIGH:start_lost,HIGH:stop_lost,HIGH:splice_acceptor_variant,HIGH:splice_donor_variant,HIGH:gene_fusion,HIGH:bidirectional_gene_fu"
                + "sion,MODERATE:3_prime_UTR_truncation+exon_loss_variant,MODERATE:5_prime_UTR_truncation+exon_loss_variant,MODERATE:coding_sequence_variant,MODERATE:disruptive_inframe_deletion,MODERATE:disruptive_inframe_inserti"
                + "on,MODERATE:conservative_inframe_deletion,MODERATE:conservative_inframe_insertion,MODERATE:missense_variant+splice_region_variant,MODERATE:missense_variant --exclude-artifacts --filter pass,likely,intermediate "
                + "--exclude-evs-qc-failed --ccds-only --min-dp-bin 10 --include-qc-missing --qd 5 --qual 50 --mq 40 --gq 20 --snv-sor 3 --indel-sor 10 --snv-fs 60 --indel-fs 200 --rprs -3 --mqrs -10 --het-percent-alt-read 0.3-"
                + "1 --min-exac-vqslod-snv -2.632 --min-exac-vqslod-indel 1.262 --gnomad-exome-af 0.001 --gnomad-exome-rf-tp-probability-snv 0.01 --gnomad-exome-rf-tp-probability-indel 0.02 --gnomad-exome-pop afr,amr,nfe,fin,eas,"
                + "asj,sas --exac-pop afr,amr,nfe,fin,eas,sas --exac-af 0.001 --loo-af 0.001 --max-qc-fail-sample 2 --include-qc-missing --include-known-var --include-evs --include-exac --include-gnomad-genome --include-gnomad-ex"
                + "ome --include-gerp --include-rvis --include-sub-rvis --include-revel --include-mgi --include-trap --include-denovo-db --include-discovehr --include-mtr --include-primate-ai --include-ccr --out dominantFlexible_MAF0.1_NoIntoleranceFilter";

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

            checkOptionDependency();

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
        {
            if (isCommandFileIncluded(options)) {
                initCommandFromFile();
            } else {
                optionArray = options;
            }
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
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                cmd += lineStr + " ";
            }

            br.close();
            fr.close();
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
     * get outputPath value from ATAV command then init outputPath path
     */
    private static void initOutput() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;
        String outputPath = "";
        boolean isTimestampEnabled = true;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--out":
                    outputPath = option.getValue();
                    break;
                case "--disable-timestamp-from-out-path":
                    isTimestampEnabled = false;
                    break;
                case "--email":
                    CommonCommand.email = true;
                    CommonCommand.emailReceiver = option.getValue();
                    break;
                case "--gzip":
                    CommonCommand.gzip = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }

        if (!outputPath.isEmpty()) {
            initOutputPath(outputPath, isTimestampEnabled);
        }

        if (CommonCommand.outputPath.isEmpty()) {
            System.out.println("\nPlease specify output path: --out $PATH \n\nExit\n");
            System.exit(0);
        }
    }

    private static void initOutputPath(String path, boolean isTimestampEnabled) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            } else if (!dir.canWrite()) {
                System.out.println("\nYou don't have write permissions into " + path + "! \n\nExit\n");
                System.exit(0);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

            CommonCommand.realOutputPath = path;
            CommonCommand.outputDirName = dir.getName();

            StringBuilder outputPathSB = new StringBuilder();

            outputPathSB.append(path);
            outputPathSB.append(File.separator);
            if (isTimestampEnabled) {
                outputPathSB.append(LocalDateTime.now().format(formatter) + "_");
            }
            outputPathSB.append(dir.getName() + "_");

            CommonCommand.outputPath = outputPathSB.toString().replaceAll(File.separator + File.separator, File.separator);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nError in creating an output directory. \n\nExit\n");
            System.exit(0);
        }
    }

    private static void initOptions4Debug() {
        Iterator<CommandOption> iterator = optionList.iterator();
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--db-host":
                    DBManager.setDBHost(option.getValue());
                    break;
                case "--debug":
                    CommonCommand.isDebug = true;
                    break;
                default:
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
                    VarGenoCommand.isList = true;
                    break;
                case "--list-var-geno-lite":
                    VarGenoCommand.isListLite = true;
                    break;
                case "--list-var":
                    VarCommand.isList = true;
                    break;
                case "--list-vcf":
                    VCFCommand.isList = true;
                    VCFCommand.isOutputVCF = true;
                    break;
                case "--list-vcf-lite":
                    VCFCommand.isListLite = true;
                    VCFCommand.isOutputVCF = true;
                    break;
                case "--list-af":
                    AFCommand.isList = true;
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
                case "--collapsing-lite":
                    CollapsingCommand.isCollapsingLite = true;
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
                    TrioCommand.isList = true;
                    CohortLevelFilterCommand.isCaseOnly = true;
                    break;
                case "--list-parent-comp-het":
                    ParentCommand.isList = true;
                    break;
                case "--list-parental-mosaic":
                    ParentalCommand.isParentalMosaic = true;
                    CohortLevelFilterCommand.isCaseOnly = true;
                    break;
                case "--ped-map":
                    PedMapCommand.isPedMap = true;
                    break;

                // Variant Annotation Functions
                case "--list-var-anno":
                    CommonCommand.isNonSampleAnalysis = true;
                    VarAnnoCommand.isList = true;
                    EvsCommand.isInclude = true;
                    ExACCommand.getInstance().isInclude = true;
                    GnomADExomeCommand.getInstance().isInclude = true;
                    GnomADGenomeCommand.getInstance().isInclude = true;
                    GnomADCommand.isIncludeGeneMetrics = true;
                    GerpCommand.isInclude = true;
                    TrapCommand.isInclude = true;
                    KnownVarCommand.isInclude = true;
                    RvisCommand.isInclude = true;
                    SubRvisCommand.isInclude = true;
                    GeVIRCommand.isInclude = true;
                    SynRvisCommand.isInclude = true;
                    LIMBRCommand.isInclude = true;
                    CCRCommand.isInclude = true;
                    MgiCommand.isInclude = true;
                    DenovoDBCommand.isInclude = true;
                    DiscovEHRCommand.isInclude = true;
                    MTRCommand.isInclude = true;
                    RevelCommand.isInclude = true;
                    PrimateAICommand.isInclude = true;
                    VariantLevelFilterCommand.isIncludeLOFTEE = true;
                    MPCCommand.isInclude = true;
                    PextCommand.isInclude = true;
                    CHMCommand.isFlag = true;
                    GMECommand.getInstance().isInclude = true;
                    TopMedCommand.getInstance().isInclude = true;
                    GenomeAsiaCommand.getInstance().isInclude = true;
                    IranomeCommand.getInstance().isInclude = true;
                    IGMAFCommand.getInstance().isInclude = true;
                    DBNSFPCommand.isInclude = true;
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
                    EvsCommand.isList = true;
                    EvsCommand.isInclude = true;
                    break;
                case "--list-exac":
                    CommonCommand.isNonSampleAnalysis = true;
                    ExACCommand.getInstance().isList = true;
                    ExACCommand.getInstance().isInclude = true;
                    break;
                case "--list-gnomad-exome":
                    CommonCommand.isNonSampleAnalysis = true;
                    GnomADExomeCommand.getInstance().isList = true;
                    GnomADExomeCommand.getInstance().isInclude = true;
                    break;
                case "--list-gnomad-genome":
                    CommonCommand.isNonSampleAnalysis = true;
                    GnomADGenomeCommand.getInstance().isList = true;
                    GnomADGenomeCommand.getInstance().isInclude = true;
                    break;
                case "--list-known-var":
                    CommonCommand.isNonSampleAnalysis = true;
                    KnownVarCommand.isList = true;
                    KnownVarCommand.isInclude = true;
                    break;
                case "--list-gerp":
                    CommonCommand.isNonSampleAnalysis = true;
                    GerpCommand.isList = true;
                    GerpCommand.isInclude = true;
                    break;
                case "--list-trap":
                    CommonCommand.isNonSampleAnalysis = true;
                    TrapCommand.isList = true;
                    TrapCommand.isInclude = true;
                    break;
                case "--list-sub-rvis":
                    CommonCommand.isNonSampleAnalysis = true;
                    SubRvisCommand.isList = true;
                    SubRvisCommand.isInclude = true;
                    break;
                case "--list-limbr":
                    CommonCommand.isNonSampleAnalysis = true;
                    LIMBRCommand.isList = true;
                    LIMBRCommand.isInclude = true;
                    break;
                case "--list-ccr":
                    CommonCommand.isNonSampleAnalysis = true;
                    CCRCommand.isList = true;
                    CCRCommand.isInclude = true;
                    break;
                case "--list-rvis":
                    CommonCommand.isNonSampleAnalysis = true;
                    RvisCommand.isList = true;
                    RvisCommand.isInclude = true;
                    break;
                case "--list-mgi":
                    CommonCommand.isNonSampleAnalysis = true;
                    MgiCommand.isList = true;
                    MgiCommand.isInclude = true;
                    break;
                case "--list-denovo-db":
                    CommonCommand.isNonSampleAnalysis = true;
                    DenovoDBCommand.isList = true;
                    DenovoDBCommand.isInclude = true;
                    break;
                case "--list-discovehr":
                    CommonCommand.isNonSampleAnalysis = true;
                    DiscovEHRCommand.isList = true;
                    DiscovEHRCommand.isInclude = true;
                    break;
                case "--list-revel":
                    CommonCommand.isNonSampleAnalysis = true;
                    RevelCommand.isList = true;
                    RevelCommand.isInclude = true;
                    break;
                case "--list-primate-ai":
                    CommonCommand.isNonSampleAnalysis = true;
                    PrimateAICommand.isList = true;
                    PrimateAICommand.isInclude = true;
                    break;
                case "--list-mpc":
                    CommonCommand.isNonSampleAnalysis = true;
                    MPCCommand.isList = true;
                    MPCCommand.isInclude = true;
                    break;
                case "--list-pext":
                    CommonCommand.isNonSampleAnalysis = true;
                    PextCommand.isList = true;
                    PextCommand.isInclude = true;
                    break;
                case "--list-gme":
                    CommonCommand.isNonSampleAnalysis = true;
                    GMECommand.getInstance().isList = true;
                    GMECommand.getInstance().isInclude = true;
                    break;
                case "--list-top-med":
                    CommonCommand.isNonSampleAnalysis = true;
                    TopMedCommand.getInstance().isList = true;
                    TopMedCommand.getInstance().isInclude = true;
                    break;
                case "--list-genome-asia":
                    CommonCommand.isNonSampleAnalysis = true;
                    GenomeAsiaCommand.getInstance().isList = true;
                    GenomeAsiaCommand.getInstance().isInclude = true;
                    break;
                case "--list-iranome":
                    CommonCommand.isNonSampleAnalysis = true;
                    IranomeCommand.getInstance().isList = true;
                    IranomeCommand.getInstance().isInclude = true;
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
                    + "--collapsing-comp-het, --fisher, --linear... etc.", ErrorManager.COMMAND_PARSING);
        }
    }

    private static void initSubOptions() throws Exception {
        if (VarGenoCommand.isList) { // Genotype Analysis Functions
            VarGenoCommand.initOptions(optionList.iterator());
        } else if (VarGenoCommand.isListLite) { // Genotype Analysis Functions
            VarGenoCommand.initOptions(optionList.iterator());
        } else if (VarCommand.isList) {

        } else if (VCFCommand.isList) {

        } else if (CollapsingCommand.isCollapsingSingleVariant) {
            CollapsingCommand.initSingleVarOptions(optionList.iterator());
        } else if (CollapsingCommand.isCollapsingCompHet) {
            CollapsingCommand.initCompHetOptions(optionList.iterator());
        } else if (CollapsingCommand.isCollapsingLite) {
            CollapsingCommand.initLiteOptions(optionList.iterator());
        } else if (StatisticsCommand.isFisher) {
            StatisticsCommand.initFisherOptions(optionList.iterator());
        } else if (StatisticsCommand.isLinear) {
            StatisticsCommand.initLinearOptions(optionList.iterator());
        } else if (SiblingCommand.isSiblingCompHet) {

        } else if (TrioCommand.isList) {
            TrioCommand.initOptions(optionList.iterator());
        } else if (ParentalCommand.isParentalMosaic) {
            ParentalCommand.initOptions(optionList.iterator());
        } else if (PedMapCommand.isPedMap) {
            PedMapCommand.initOptions(optionList.iterator());
        } else if (VarAnnoCommand.isList) { // Variant Annotation Functions

        } else if (CoverageCommand.isCoverageSummary) { // Coverage Analysis Functions
            CoverageCommand.initCoverageSummary(optionList.iterator());
        } else if (CoverageCommand.isSiteCoverageSummary) {
            CoverageCommand.initSiteCoverageSummary(optionList.iterator());
        } else if (CoverageCommand.isCoverageComparison) {
            CoverageCommand.initCoverageComparison(optionList.iterator());
        } else if (CoverageCommand.isSiteCoverageComparison) {
            CoverageCommand.initSiteCoverageComparison(optionList.iterator());
        } else if (EvsCommand.isList) { // External Datasets Functions

        } else if (GnomADExomeCommand.getInstance().isList) {

        } else if (KnownVarCommand.isList) {

        } else if (TestCommand.isTest) { // Test Functions
            TestCommand.initOptions(optionList.iterator());
        }
    }

    private static void initCommonOptions() throws Exception {
        VariantLevelFilterCommand.initOptions(optionList.iterator());

        AnnotationLevelFilterCommand.initOptions(optionList.iterator());

        GenotypeLevelFilterCommand.initOptions(optionList.iterator());

        CohortLevelFilterCommand.initOptions(optionList.iterator());
    }

    public static void checkOptionDependency() {
        if (CollapsingCommand.isCollapsingLite) {
            if (CohortLevelFilterCommand.sampleFile.isEmpty()) {
                ErrorManager.print("Please specify your sample file: --sample $PATH", ErrorManager.INPUT_PARSING);
            } else if (GenotypeLevelFilterCommand.genotypeFile.isEmpty()) {
                ErrorManager.print("Please specify your genotype file: --genotype $PATH", ErrorManager.INPUT_PARSING);
            }
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
            ErrorManager.print("You have invalid options in your ATAV command.", ErrorManager.COMMAND_PARSING);
        }
    }

    /*
     * outputPath invalid option & value if value > max or value < min ATAV stop
     */
    public static void checkValueValid(double max, double min, CommandOption option) {
        double value = getValidDouble(option);
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
     * outputPath invalid option & value if value is not in strList ATAV stop
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
     * outputPath invalid option & value if value is not in strList ATAV stop
     */
    public static void checkValuesValid(String[] array, CommandOption option) {
        HashSet<String> set = new HashSet<>(Arrays.asList(array));

        String[] values = option.getValue().split(",");

        for (String value : values) {
            if (!set.contains(value)) {
                outputInvalidOptionValue(option);
            }
        }
    }

    /*
     * outputPath invalid option & value if value is not a valid range
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
     * outputPath invalid option & value if value is a valid file path return either
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

    public static String getNonEmptyValue(CommandOption option) {
        if (option.getValue().isEmpty()) {
            CommandManager.outputInvalidOptionValue(option);
        }

        return option.getValue();
    }

    public static void outputInvalidOptionValue(CommandOption option) {
        ErrorManager.print("\nInvalid value '" + option.getValue()
                + "' for option '" + option.getName() + "'", ErrorManager.COMMAND_PARSING);
    }
}
