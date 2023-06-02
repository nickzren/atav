package function.cohort.base;

import function.cohort.collapsing.CollapsingCommand;
import function.cohort.family.FamilyCommand;
import function.cohort.family.FamilyManager;
import function.cohort.pedmap.PedMapCommand;
import function.cohort.singleton.SingletonCommand;
import function.cohort.statistics.StatisticsCommand;
import function.cohort.trio.TrioCommand;
import function.coverage.base.CoverageCommand;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SampleManager {

    public static final String TMP_SAMPLE_ID_TABLE = "tmp_sample_id";

    // sample permission
    private static HashMap<String, String> sampleGroupMap = new HashMap<>();// sample_name, group_name
    private static HashMap<String, HashSet<String>> userGroupMap = new HashMap<>();// group_name, user set

    private static ArrayList<Sample> sampleList = new ArrayList<>();
    private static HashMap<Integer, Sample> sampleMap = new HashMap<>();
    private static HashSet<String> sampleNameSet = new HashSet<>();
    private static HashSet<String> broadPhenotypeSet = new HashSet<>();

    private static int totalSampleNum; // case + ctrl
    private static int caseNum = 0;
    private static int ctrlNum = 0;

    private static ArrayList<Sample> failedSampleList = new ArrayList<>();
    private static ArrayList<Sample> diffTypeSampleList = new ArrayList<>();
    private static ArrayList<Sample> notExistSampleList = new ArrayList<>();

    private static boolean isSampleFileCorrect = true;

    private static ArrayList<Sample> restrictedSampleList = new ArrayList<>();

    private static String tempCovarFile;
    private static String covariateFileHeader = "";
    private static int covariateNum = Data.INTEGER_NA;

    // output existing / qualifed samples
    private static BufferedWriter bwExistingSample = null;
    private final static String existingSampleFile = CommonCommand.outputPath + "existing.sample.txt";

    // output all existing samples
    private static BufferedWriter bwAllSample = null;
    private final static String allSampleFile = CommonCommand.outputPath + "all.sample.txt";

    private static StringJoiner caseIDSJ = new StringJoiner(",");

    // igm gnomad sample
    private static final String IGM_GNOMAD_SAMPLE_PATH = Data.ATAV_HOME + "data/sample/igm_gnomad_sample_062620.txt";
    private static Set<String> excludeIGMGnomadSampleSet = new HashSet<>();

    // default control sample
    private static final String DEFAULT_CONTROL_SAMPLE_PATH = Data.ATAV_HOME + "data/sample/default_control_sample.tsv";
    private static ArrayList<Sample> defaultControlSampleList = new ArrayList<>();

    public static void init() {
        if (CommonCommand.isNonSampleAnalysis) {
            return;
        }

        checkSampleFile();

        initExcludeIGMGnomADSample();

        initBroadPhenotype();

        if (!CohortLevelFilterCommand.inputSample.isEmpty()) {
            initExistingSampleFile();
            initFromSampleInput(CohortLevelFilterCommand.inputSample);
            initIncludeDefaultControlSample();
            closeExistingSampleFile();
        } else if (CohortLevelFilterCommand.isAllSample
                || CohortLevelFilterCommand.isAllExome) {
            initAllSampleFile();
            initAllSampleFromDB();
            closeAllSampleFile();
            CohortLevelFilterCommand.inputSample = allSampleFile;
        } else if (FamilyCommand.isList) {
            initExistingSampleFile();
            initFamilyIdInput(FamilyCommand.inputFamilyId);
            initIncludeDefaultControlSample();
            closeExistingSampleFile();
        }

        initCovariate();

        initQuantitative();

        initSampleIndexAndSize();

        initTempTables();

        outputSampleListSummary();

        checkCaseCtrlOptions();
    }

    private static void initBroadPhenotype() {
        if (!CohortLevelFilterCommand.inputBroadPhenotype.isEmpty()) {
            // init predefined broad phenotype list from database
            HashSet<String> existingBroadPhenotypeSet = new HashSet<>();
            try {
                String sqlCode = "SHOW COLUMNS FROM sample LIKE 'broad_phenotype'";

                PreparedStatement preparedStatement = DBManager.initPreparedStatement(sqlCode);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    String type = rs.getString("Type");
                    String[] enumValues = type.substring(5, type.length() - 1).split(",");

                    for (String value : enumValues) {
                        existingBroadPhenotypeSet.add(value.trim().replaceAll("'", ""));
                    }
                }
            } catch (SQLException e) {
                ErrorManager.send(e);
            }

            // init user's input broad phenotype list
            try {
                broadPhenotypeSet = Files.lines(Paths.get(CohortLevelFilterCommand.inputBroadPhenotype))
                        .map(String::toLowerCase) // convert each line to lowercase
                        .filter(line -> {
                            if (!existingBroadPhenotypeSet.contains(line)) {
                                LogManager.writeAndPrint("Excluding nonexistent broad phenotype: " + line);
                                return false; // exclude line if it is not in existingBroadPhenotypeSet
                            }
                            return true;
                        })
                        .collect(Collectors.toCollection(HashSet::new));

                broadPhenotypeSet.addAll(Arrays.asList(CohortLevelFilterCommand.inputBroadPhenotype.split(",")));
            } catch (IOException e) {
                LogManager.writeAndPrintNoNewLine("\nError input: " + CohortLevelFilterCommand.inputBroadPhenotype);

                ErrorManager.send(e);
            }
        }
    }

    private static boolean isBroadPhenotypeValid(String broadPhenotype) {
        if (CohortLevelFilterCommand.inputBroadPhenotype.isEmpty()) {
            return true;
        }

        return broadPhenotypeSet.contains(broadPhenotype);
    }

    private static void initExistingSampleFile() {
        try {
            bwExistingSample = new BufferedWriter(new FileWriter(existingSampleFile));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private static void initAllSampleFile() {
        try {
            bwAllSample = new BufferedWriter(new FileWriter(allSampleFile));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private static void closeExistingSampleFile() {
        try {
            bwExistingSample.flush();
            bwExistingSample.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private static void closeAllSampleFile() {
        try {
            bwAllSample.flush();
            bwAllSample.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private static void checkSampleFile() {
        if (CohortLevelFilterCommand.inputSample.isEmpty()
                && !CohortLevelFilterCommand.isAllSample
                && !CohortLevelFilterCommand.isAllExome
                && !FamilyCommand.isList) {
            ErrorManager.print("Please specify your sample file: --sample $PATH", ErrorManager.INPUT_PARSING);
        }
    }

    private static void initExcludeIGMGnomADSample() {
        if (CohortLevelFilterCommand.isExcludeIGMGnomadSample) {
            try ( BufferedReader br = Files.newBufferedReader(Paths.get(IGM_GNOMAD_SAMPLE_PATH))) {
                excludeIGMGnomadSampleSet = br.lines().collect(Collectors.toSet());
            } catch (IOException e) {
                ErrorManager.print("Error: parsing IGM GnomAD Sample file", ErrorManager.INPUT_PARSING);
            }
        }
    }

    public static void initIncludeDefaultControlSample() {
        if (CohortLevelFilterCommand.isIncludeDefaultControlSample) {
            try {
                File f = new File(DEFAULT_CONTROL_SAMPLE_PATH);
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);

                String lineStr = "";
                while ((lineStr = br.readLine()) != null) {
                    String[] values = lineStr.replaceAll("( )+", "").split("\t");

                    String familyId = values[0];
                    String individualId = values[1];
                    String paternalId = values[2];
                    String maternalId = values[3];
                    byte sex = Byte.valueOf(values[4]);
                    byte pheno = Byte.valueOf(values[5]);
                    String sampleType = values[6];
                    String captureKit = values[7];

                    TempSample tempSample = getTempSample(individualId, sampleType, captureKit);

                    Sample sample = new Sample(tempSample.sampleId, familyId, individualId,
                            paternalId, maternalId, sex, pheno, sampleType, captureKit,
                            tempSample.experimentId, tempSample.ancestry, tempSample.broadPhenotype);

                    if (sampleMap.containsKey(tempSample.sampleId)) {
                        continue;
                    }

                    sampleList.add(sample);
                    sampleMap.put(tempSample.sampleId, sample);

                    countSampleNum(sample);

                    bwExistingSample.write(initStringLineForSampleFile(sample));
                    bwExistingSample.newLine();
                }

                br.close();
                fr.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
    }

    private static void initSampleIndexAndSize() {
        int index = 0;

        for (Sample sample : sampleList) {
            sample.setIndex(index++);
        }

        totalSampleNum = sampleList.size();
    }

    private static void initAllSampleFromDB() {
        String sqlCode = "SELECT * FROM sample "
                + "WHERE sample_finished = 1 and sample_failure = 0 "
                + "and sample_type != 'custom_capture' and low_quality != 1";

        if (CohortLevelFilterCommand.isAllExome) {
            sqlCode += " and sample_type = 'Exome'";
        }

        if (CohortLevelFilterCommand.isAvailableControlUseOnly) {
            sqlCode += " and available_control_use = 1";
        }

        sqlCode += " order by sample_id desc ";

        initSampleFromDB(sqlCode);
    }

    private static void initFamilyIdInput(String familyIds) {
        if (familyIds.isEmpty()) {
            return;
        }
        String[] familyIdList = familyIds.split(",");

        for (String id : familyIdList) {
            addFamilyIdToList(id);
        }
    }

    private static void initFromSampleInput(String input) {
        if (input.isEmpty()) {
            return;
        }

        File f = new File(input);

        if (f.isFile()) {
            initFromSampleFile(input);
        } else {
            String[] list = input.split(",");

            for (String s : list) {
                addSampleToList(s);
            }
        }
    }

    private static void addFamilyIdToList(String familyId) {
        if (!FamilyCommand.isList) {
            LogManager.writeAndPrintNoNewLine("\nError ID (" + familyId + ") in sample file.");
            ErrorManager.print("Only --list-family allows input family id for option --family-id.", ErrorManager.INPUT_PARSING);
        }

        try {
            addSampleToFamilyIdList(familyId);

        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError Family ID (" + familyId + ") in sample file.");

            ErrorManager.send(e);
        }
    }

    // only trio or singleton or coverage summary analysis support sample name as input for --sample
    private static void addSampleToList(String sampleName) {
        if (!SingletonCommand.isList
                && !TrioCommand.isList
                && !CoverageCommand.isCoverageSummary
                && !PedMapCommand.isPedMap) {
            LogManager.writeAndPrintNoNewLine("\nError sample (" + sampleName + ") in sample file.");
            ErrorManager.print("Only --list-trio or --list-singleton "
                    + "or --coverage-summary or --ped-map allows input sample name for option --sample.", ErrorManager.INPUT_PARSING);
        }

        try {
            if (SingletonCommand.isList
                    || (CoverageCommand.isCoverageSummary && !CoverageCommand.isTrio)
                    || (PedMapCommand.isPedMap && !PedMapCommand.isTrio)) {
                addSampleToSingletonList(sampleName);
            }

            if (TrioCommand.isList
                    || (CoverageCommand.isCoverageSummary && CoverageCommand.isTrio)
                    || (PedMapCommand.isPedMap && PedMapCommand.isTrio)) {
                addSampleToTrioList(sampleName);
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError sample (" + sampleName + ") in sample file.");

            ErrorManager.send(e);
        }
    }

    private static void addSampleToTrioList(String sampleName) throws Exception {
        TempSample tempSample = getTempSample(sampleName);

        if (tempSample.sampleId != Data.INTEGER_NA) {
            if (tempSample.familyRelationProband != null
                    && !tempSample.familyRelationProband.equals("Proband")
                    && !tempSample.familyRelationProband.equals("Sibling")
                    && !tempSample.familyRelationProband.equals("Half sibling")
                    && !tempSample.familyRelationProband.equals("Monozygotic twin")
                    && !tempSample.familyRelationProband.equals("Child")
                    && !tempSample.familyId.equals("N/A")) {
                LogManager.writeAndPrint("Invalid proband: " + sampleName);
                return;
            }

            if (tempSample.isGenderMismatch()) {
                LogManager.writeAndPrint("Proband gender mismatch: " + sampleName);
                return;
            }

            if (!initParents(tempSample)) {
                return;
            }

            tempSample.pheno = Byte.valueOf("2");

            // add proband
            addSample(tempSample);

            // add parents
            addParent(tempSample.paternalId);
            addParent(tempSample.maternalId);
        } else {
            LogManager.writeAndPrint("Proband is missing: " + sampleName);
        }
    }

    private static void addParent(String sampleName) throws Exception {
        if (sampleName == null || sampleName.equals("0")) {
            return;
        }

        TempSample tempSample = getTempSample(sampleName);

        tempSample.paternalId = "0";
        tempSample.maternalId = "0";
        tempSample.pheno = Byte.valueOf("1");

        addSample(tempSample);
    }

    private static void addSampleToFamilyIdList(String familyId) throws Exception {
        ArrayList<Sample> sampleList = getFamilySamples(familyId);

        if (FamilyManager.initFamily(sampleList)) {

            for (Sample sample : sampleList) {             
                addSample(sample);
            }
        }
    }

    private static void addSampleToSingletonList(String sampleName) throws Exception {
        TempSample tempSample = getTempSample(sampleName);

        if (tempSample.sampleId != Data.INTEGER_NA) {
            if (tempSample.isGenderMismatch()) {
                LogManager.writeAndPrint("Gender mismatch: " + sampleName);
                return;

            }
        } else {
            LogManager.writeAndPrint("Invalid singleton sample: " + sampleName);
            return;
        }

        tempSample.paternalId = "0";
        tempSample.maternalId = "0";
        tempSample.pheno = Byte.valueOf("2");

        addSample(tempSample);
    }

    private static boolean initParents(TempSample tempSample) {
        try {
            String sql = "SELECT sample_name, seq_gender, self_decl_gender FROM sample WHERE family_id=? ";

            if (tempSample.familyRelationProband.equals("Proband")
                    || tempSample.familyRelationProband.equals("Sibling")
                    || tempSample.familyRelationProband.equals("Half sibling")
                    || tempSample.familyRelationProband.equals("Monozygotic twin")) {
                sql += "AND family_relation_proband = 'Parent' ";
            } else if (tempSample.familyRelationProband.equals("Child")) {
                sql += "AND family_relation_proband in ('Proband','Spouse') ";
            }

            sql += "AND sample_finished = 1 AND sample_failure = 0 order by sample_id desc";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, tempSample.familyId);
            ResultSet rs = preparedStatement.executeQuery();

            tempSample.paternalId = "0";
            tempSample.maternalId = "0";

            while (rs.next()) {
                String sampleName = FormatManager.getString(rs.getString("sample_name"));
                String seqGender = FormatManager.getString(rs.getString("seq_gender"));
                String selfDeclGender = FormatManager.getString(rs.getString("self_decl_gender"));

                if (seqGender.equals("Ambiguous") || seqGender.equals(Data.STRING_NA)
                        || selfDeclGender.equals("Unknown") || selfDeclGender.equals(Data.STRING_NA)
                        || !seqGender.equals(selfDeclGender)) {
                    LogManager.writeAndPrint("Parent gender mismatch: " + sampleName);
                    return false;
                }

                if (seqGender.equals("M")) {
                    tempSample.paternalId = sampleName;
                } else {
                    tempSample.maternalId = sampleName;
                }
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return true;
    }

    private static void addSample(Sample sample) throws Exception {
        if (sample.isCase()) {
            caseIDSJ.add(String.valueOf(sample.getId()));
        }

        sampleList.add(sample);
        sampleMap.put(sample.getId(), sample);
                
        countSampleNum(sample);
                
        bwExistingSample.write(initStringLineForSampleFile(sample));
        bwExistingSample.newLine();
    }

    private static void addSample(TempSample tempSample) throws Exception {
        if (CohortLevelFilterCommand.isExcludeLowQualitySample
                && isLowQualitySample(tempSample.sampleId)) {
            LogManager.writeAndPrint("Excluded low quality sample: " + tempSample.sampleName);
            return;
        }

        if (sampleMap.containsKey(tempSample.sampleId)) {
            return;
        }

        Sample sample = new Sample(tempSample.sampleId, tempSample.familyId, tempSample.sampleName,
                tempSample.paternalId, tempSample.maternalId, tempSample.sex, tempSample.pheno,
                tempSample.sampleType, tempSample.captureKit,
                tempSample.experimentId, tempSample.ancestry, tempSample.broadPhenotype);

        if (tempSample.sampleId == Data.INTEGER_NA) {
            checkSampleList(sample);
            return;
        }

        if (sample.isCase()) {
            caseIDSJ.add(String.valueOf(sample.getId()));
        }

        sampleList.add(sample);
        sampleMap.put(tempSample.sampleId, sample);

        countSampleNum(sample);

        bwExistingSample.write(initStringLineForSampleFile(sample));
        bwExistingSample.newLine();
    }

    private static void initFromSampleFile(String sampleFile) {
        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(sampleFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                String[] values = lineStr.replaceAll("( )+", "").split("\t");

                if (values.length == 1) {
                    addSampleToList(values[0]);
                } else {
                    String familyId = values[0];

                    if (familyId.equals("N/A")) {
                        ErrorManager.print("\nWrong FamilyID: " + familyId
                                + " (line " + lineNum + " in sample file)", ErrorManager.INPUT_PARSING);
                    }

                    String individualId = values[1];

                    if (CohortLevelFilterCommand.isExcludeIGMGnomadSample
                            && excludeIGMGnomadSampleSet.contains((individualId))) {
                        LogManager.writeAndPrint("Excluded IGM gnomAD sample: " + individualId);
                        continue;
                    }

                    if (!CohortLevelFilterCommand.isDisableCheckDuplicateSample) {
                        if (!sampleNameSet.contains(individualId.toUpperCase())) {
                            sampleNameSet.add(individualId.toUpperCase());
                        } else {
                            ErrorManager.print("\nDuplicate sample: " + individualId
                                    + " (line " + lineNum + " in sample file)", ErrorManager.INPUT_PARSING);
                        }
                    }

                    String paternalId = values[2];
                    String maternalId = values[3];

                    byte sex = Byte.valueOf(values[4]);
                    if (sex != 1 && sex != 2) {
                        ErrorManager.print("\nWrong Sex: " + sex
                                + " (line " + lineNum + " in sample file)", ErrorManager.INPUT_PARSING);
                    }

                    byte pheno = Byte.valueOf(values[5]);
                    if (pheno != 1 && pheno != 2) {
                        ErrorManager.print("\nWrong Phenotype: " + pheno
                                + " (line " + lineNum + " in sample file)", ErrorManager.INPUT_PARSING);
                    }

                    String sampleType = values[6];
                    String captureKit = values[7];

                    if (sampleType.equalsIgnoreCase("genome")) {
                        captureKit = "N/A";
                    }

                    TempSample tempSample = getTempSample(individualId, sampleType, captureKit);

                    if (CohortLevelFilterCommand.isExcludeLowQualitySample
                            && isLowQualitySample(tempSample.sampleId)) {
                        LogManager.writeAndPrint("Excluded low quality sample: " + individualId);
                        continue;
                    }

                    if (!isBroadPhenotypeValid(tempSample.broadPhenotype)) {
                        continue;
                    }

                    if (sampleMap.containsKey(tempSample.sampleId)) {
                        continue;
                    }

                    Sample sample = new Sample(tempSample.sampleId, familyId, individualId,
                            paternalId, maternalId, sex, pheno, sampleType, captureKit,
                            tempSample.experimentId, tempSample.ancestry, tempSample.broadPhenotype);

                    if (tempSample.sampleId == Data.INTEGER_NA) {
                        checkSampleList(sample);
                        continue;
                    }

                    if (sample.isCase()) {
                        caseIDSJ.add(String.valueOf(sample.getId()));
                    }

                    sampleList.add(sample);
                    sampleMap.put(tempSample.sampleId, sample);

                    countSampleNum(sample);

                    bwExistingSample.write(initStringLineForSampleFile(sample));
                    bwExistingSample.newLine();
                }
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in sample file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static String initStringLineForSampleFile(Sample sample) {
        StringJoiner sj = new StringJoiner("\t");
        sj.add(sample.getFamilyId());
        if (PedMapCommand.outputExperimentId) {
            sj.add(sample.getName() + "." + FormatManager.getInteger(sample.getExperimentId()));
        } else {
            sj.add(sample.getName());
        }
        sj.add(sample.getPaternalId());
        sj.add(sample.getMaternalId());
        sj.add(String.valueOf(sample.getSex()));
        sj.add(sample.getPhenoForSampleFile());
        sj.add(sample.getType());
        sj.add(sample.getCaptureKit());

        return sj.toString();
    }

    private static void initSampleFromDB(String sqlCode) {
        try {
            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sqlCode);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int sampleId = rs.getInt("sample_id");
                String familyId = rs.getString("family_id");
                String individualId = rs.getString("sample_name");
                familyId = familyId == null ? individualId : familyId;

                if (CohortLevelFilterCommand.isExcludeIGMGnomadSample
                        && excludeIGMGnomadSampleSet.contains((individualId))) {
                    LogManager.writeAndPrint("Excluded IGM gnomAD Sample: " + individualId);
                    continue;
                }

                String paternalId = "0";
                String maternalId = "0";

                String seqGender = FormatManager.getString(rs.getString("seq_gender"));
                byte sex = seqGender.equals("M") ? (byte) 1 : (byte) 2;

                byte pheno = 2; // case
                String sampleType = rs.getString("sample_type").trim();
                String captureKit = rs.getString("capture_kit").trim();
                int experimentId = rs.getInt("experiment_id");
                String ancestry = FormatManager.getString(rs.getString("ancestry"));
                String broadPhenotype = FormatManager.getString(rs.getString("broad_phenotype"));

                // skip duplicate samples
                if (!sampleNameSet.contains(individualId.toUpperCase())) {
                    sampleNameSet.add(individualId.toUpperCase());
                } else {
                    continue;
                }

                Sample sample = new Sample(sampleId, familyId, individualId,
                        paternalId, maternalId, sex, pheno, sampleType, captureKit,
                        experimentId, ancestry, broadPhenotype);

                if (!isBroadPhenotypeValid(broadPhenotype)) {
                    continue;
                }

                sampleList.add(sample);
                sampleMap.put(sampleId, sample);

                countSampleNum(sample);

                if (PedMapCommand.outputExperimentId) {
                    individualId += "." + experimentId;
                }

                bwAllSample.write(familyId + "\t"
                        + individualId + "\t"
                        + paternalId + "\t"
                        + maternalId + "\t"
                        + sex + "\t"
                        + pheno + "\t"
                        + sampleType + "\t"
                        + captureKit);
                bwAllSample.newLine();
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void checkSampleList(Sample sample) {
        try {
            String sql = "SELECT * FROM sample "
                    + "WHERE sample_name=? AND sample_type=? AND capture_kit=? AND sample_failure > 0 ";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, sample.getName());
            preparedStatement.setString(2, sample.getType());
            preparedStatement.setString(3, sample.getCaptureKit());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                failedSampleList.add(sample);
            } else {
                sql = "SELECT * FROM sample "
                        + "WHERE sample_name=? AND sample_finished = 1 AND sample_failure = 0";

                preparedStatement = DBManager.initPreparedStatement(sql);
                preparedStatement.setString(1, sample.getName());
                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    sample.setType(rs.getString("sample_type"));
                    sample.setCaptureKit(rs.getString("capture_kit"));
                    diffTypeSampleList.add(sample);
                } else {
                    notExistSampleList.add(sample);
                }
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void outputSampleListSummary() {
        LogManager.writeAndPrint("Total samples: "
                + sampleList.size() + " (" + caseNum + " cases and " + ctrlNum + " controls)");

        printSampleList("Permission denied samples:",
                restrictedSampleList);

        printSampleList("Failed samples:",
                failedSampleList);

        printSampleList("Samples with a different seqtype or capture kit:",
                diffTypeSampleList);

        printSampleList("Not exist samples:",
                notExistSampleList);

        if (!isSampleFileCorrect) {
            LogManager.writeAndPrint("Generated all existing samples:\n" + existingSampleFile);

            ErrorManager.print("Wrong values in sample file.", ErrorManager.INPUT_PARSING);
        }
    }

    private static void checkCaseCtrlOptions() {
        if (caseNum == 0) {
            if (CohortLevelFilterCommand.maxCaseAF != Data.NO_FILTER
                    || CohortLevelFilterCommand.maxCaseMAF != Data.NO_FILTER
                    || CohortLevelFilterCommand.minCaseCarrier != Data.NO_FILTER
                    || CohortLevelFilterCommand.minCoveredSamplePercentage[Index.CASE] != Data.NO_FILTER
                    || CohortLevelFilterCommand.isCaseOnly == true) {
                ErrorManager.print("You used case filters but your sample file has no cases.",
                        ErrorManager.INPUT_PARSING);
            }
        } else if (ctrlNum == 0) {
            if (CohortLevelFilterCommand.maxCtrlAF != Data.NO_FILTER
                    || CohortLevelFilterCommand.maxCtrlMAF != Data.NO_FILTER
                    || CohortLevelFilterCommand.minCoveredSamplePercentage[Index.CTRL] != Data.NO_FILTER) {
                ErrorManager.print("You used control filters but your sample file has no controls.",
                        ErrorManager.INPUT_PARSING);
            }
        }
    }

    private static void printSampleList(String startMessage,
            ArrayList<Sample> sampleList) {
        if (!sampleList.isEmpty()) {
            if (isSampleFileCorrect) {
                isSampleFileCorrect = false;
            }

            LogManager.writeAndPrintNoNewLine(startMessage);

            for (Sample sample : sampleList) {
                LogManager.writeAndPrintNoNewLine(
                        sample.getFamilyId() + "\t"
                        + sample.getName() + "\t"
                        + sample.getPaternalId() + "\t"
                        + sample.getMaternalId() + "\t"
                        + sample.getSex() + "\t"
                        + (sample.getPheno() + 1) + "\t"
                        + sample.getType() + "\t"
                        + sample.getCaptureKit());
            }

            LogManager.writeAndPrintNoNewLine(""); // hack to add new line
        }
    }

    private static void initCovariate() {
        if (StatisticsCommand.covariateFile.isEmpty()) {
            return;
        }

        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(StatisticsCommand.covariateFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                lineStr = lineStr.replaceAll("( )+", "");

                if (covariateFileHeader.isEmpty()) {
                    covariateFileHeader = lineStr;
                    continue;
                }

                String[] values = lineStr.split("\t");

                Sample sample = getSampleByName(values[1]);

                if (sample != null && sample.getCovariateList().isEmpty()) {
                    sample.initCovariate(values);

                    if (covariateNum == Data.INTEGER_NA) {
                        covariateNum = sample.getCovariateList().size();
                    }
                }
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in covariate file: " + lineStr);

            ErrorManager.send(e);
        }

        resetSampleListByCovariate();
    }

    public static int getCovariateNum() {
        return covariateNum;
    }

    private static void resetSampleListByCovariate() {
        Iterator<Sample> it = sampleList.iterator();
        while (it.hasNext()) {
            Sample sample = it.next();
            if (sample.getCovariateList().isEmpty()) {
                it.remove();
                sampleMap.remove(sample.getId());
                reduceSampleNum(sample);
            }
        }
    }

    private static void initQuantitative() {
        if (StatisticsCommand.quantitativeFile.isEmpty()) {
            return;
        }

        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(StatisticsCommand.quantitativeFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                lineStr = lineStr.toLowerCase();
                String[] values = lineStr.split("\t");
                String name = values[0];
                float value = Float.valueOf(values[1]);

                Sample sample = getSampleByName(name);

                if (sample != null) {
                    sample.setQuantitativeTrait(value);
                }
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in quantitative file: " + lineStr);

            ErrorManager.send(e);
        }

        resetSampleListByQuantitative();

        resetSamplePheno4Linear();
    }

    private static void resetSampleListByQuantitative() {
        Iterator<Sample> it = sampleList.iterator();
        while (it.hasNext()) {
            Sample sample = it.next();
            if (sample.getQuantitativeTrait() == Data.FLOAT_NA) {
                it.remove();
                sampleMap.remove(sample.getId());
                reduceSampleNum(sample);
            }
        }
    }

    public static void generateCovariateFile() {
        if (CollapsingCommand.isCollapsingDoLogistic
                || CollapsingCommand.isCollapsingDoLinear) {
            try {
                tempCovarFile = CommonCommand.outputPath + "covariate.txt";

                BufferedWriter bwCov = new BufferedWriter(new FileWriter(tempCovarFile));

                bwCov.write("Family" + "\t"
                        + "Sample" + "\t"
                        + "Pheno");

                String[] columns = covariateFileHeader.split("\t");

                for (int i = 2; i < columns.length; i++) {
                    bwCov.write("\t" + columns[i]);
                }

                bwCov.newLine();

                for (Sample sample : sampleList) {
                    bwCov.write(sample.getFamilyId() + "\t"
                            + sample.getName() + "\t");

                    if (CollapsingCommand.isCollapsingDoLogistic) {
                        bwCov.write(String.valueOf(sample.getPheno() + 1));
                    } else if (CollapsingCommand.isCollapsingDoLinear) {
                        bwCov.write(String.valueOf(sample.getQuantitativeTrait()));
                    }

                    for (Double covar : sample.getCovariateList()) {
                        bwCov.write("\t" + covar);
                    }

                    bwCov.newLine();
                }

                bwCov.flush();
                bwCov.close();
            } catch (Exception e) {
                ErrorManager.send(e);
            }
        }
    }

    public static String getTempCovarPath() {
        return tempCovarFile;
    }

    public static Sample getSampleByName(String name) {
        for (Sample sample : sampleList) {
            if (sample.getName().equalsIgnoreCase(name)) {
                return sample;
            }
        }

        return null;
    }

    private static void countSampleNum(Sample sample) {
        if (sample.isCase()) {
            caseNum++;
        } else {
            ctrlNum++;
        }
    }

    private static void reduceSampleNum(Sample sample) {
        if (sample.isCase()) {
            caseNum--;
        } else {
            ctrlNum--;
        }
    }

    public static int getCaseNum() {
        return caseNum;
    }

    public static int getCtrlNum() {
        return ctrlNum;
    }

    private static void initTempTables() {
        createTempTable(TMP_SAMPLE_ID_TABLE);

        StringJoiner allSampleIdSj = new StringJoiner(",");
        for (Sample sample : sampleList) {
            allSampleIdSj.add("(" + sample.getId() + ")");
        }

        insertId2Table(allSampleIdSj.toString(), TMP_SAMPLE_ID_TABLE);
    }

    private static void createTempTable(String sqlTable) {
        try {
            Statement stmt = DBManager.createStatement();

            String sqlQuery = "CREATE TEMPORARY TABLE "
                    + sqlTable
                    + "(input_sample_id mediumint, PRIMARY KEY (input_sample_id));";

            stmt.executeUpdate(StringEscapeUtils.escapeSql(sqlQuery));
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void insertId2Table(String ids, String table) {
        try {
            if (!ids.isEmpty()) {
                DBManager.executeUpdate("INSERT IGNORE INTO " + table + " VALUES " + ids);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static TempSample getTempSample(String sampleName, String sampleType,
            String captureKit) throws Exception {
        TempSample s = new TempSample();
        s.sampleId = Data.INTEGER_NA;

        try {
            String sql = "SELECT sample_id,experiment_id,ancestry,broad_phenotype FROM sample "
                    + "WHERE sample_name=? AND sample_type=? AND capture_kit=? "
                    + "AND sample_finished = 1 AND sample_failure = 0";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, sampleName);
            preparedStatement.setString(2, sampleType);
            preparedStatement.setString(3, captureKit);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                s.sampleId = rs.getInt("sample_id");
                s.experimentId = rs.getInt("experiment_id");
                s.ancestry = FormatManager.getString(rs.getString("ancestry"));
                s.broadPhenotype = FormatManager.getString(rs.getString("broad_phenotype"));
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return s;
    }

    private static ArrayList<Sample> getFamilySamples(String familyId) throws Exception {
        ArrayList<Sample> familySampleList = new ArrayList<>();

        try {
            String sql = "SELECT sample_name,sample_id,experiment_id,sample_type,capture_kit,seq_gender,"
                    + "ancestry,broad_phenotype,self_decl_gender,family_id FROM sample "
                    + "WHERE family_id=? AND sample_finished = 1 AND sample_failure = 0 order by family_id desc";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, familyId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                //Sample s = new Sample();
                String sampleName = FormatManager.getString(rs.getString("sample_name"));
                int sampleId = rs.getInt("sample_id");
                int experimentId = rs.getInt("experiment_id");
                String sampleType = FormatManager.getString(rs.getString("sample_type"));
                String captureKit = FormatManager.getString(rs.getString("capture_kit"));
                String seqGender = FormatManager.getString(rs.getString("seq_gender"));
                byte sex = seqGender.equals("M") ? (byte) 1 : (byte) 2;
                String selfDeclGender = FormatManager.getString(rs.getString("self_decl_gender"));
                String ancestry = FormatManager.getString(rs.getString("ancestry"));
                String broadPhenotype = FormatManager.getString(rs.getString("broad_phenotype"));

                if (selfDeclGender.equals("Unknown")) {
                    ErrorManager.print("Sample gender mismatch: " + sampleName, ErrorManager.INPUT_PARSING);
                }

                if (seqGender.equals("Ambiguous") || seqGender.equals(Data.STRING_NA)
                        || selfDeclGender.equals(Data.STRING_NA) || !seqGender.equals(selfDeclGender)) {
                    ErrorManager.print("Sample gender mismatch: " + sampleName, ErrorManager.INPUT_PARSING);
                }
                                
                byte pheno = 2;
                if (broadPhenotype.equals("healthy family member")) {
                    pheno = 1;
                }
                                
                Sample sample = new Sample(sampleId, familyId, sampleName,
                        "0", "0", sex, pheno, sampleType, captureKit,
                        experimentId, ancestry, broadPhenotype);
                                
                familySampleList.add(sample);
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
        return familySampleList;
    }

    private static TempSample getTempSample(String sampleName) throws Exception {
        TempSample s = new TempSample();
        s.sampleId = Data.INTEGER_NA;
        s.sampleName = sampleName;

        try {
            String sql = "SELECT sample_id,experiment_id,sample_type,capture_kit,seq_gender,self_decl_gender,"
                    + "ancestry,broad_phenotype,family_id,family_relation_proband FROM sample "
                    + "WHERE sample_name=? AND sample_finished = 1 AND sample_failure = 0 order by sample_id desc";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setString(1, sampleName);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                s.sampleId = rs.getInt("sample_id");
                s.experimentId = rs.getInt("experiment_id");
                s.sampleType = FormatManager.getString(rs.getString("sample_type"));
                s.captureKit = FormatManager.getString(rs.getString("capture_kit"));
                s.seqGender = FormatManager.getString(rs.getString("seq_gender"));
                s.sex = s.seqGender.equals("M") ? (byte) 1 : (byte) 2;
                s.selfDeclGender = FormatManager.getString(rs.getString("self_decl_gender"));
                s.ancestry = FormatManager.getString(rs.getString("ancestry"));
                s.broadPhenotype = FormatManager.getString(rs.getString("broad_phenotype"));
                s.familyId = FormatManager.getString(rs.getString("family_id"));
                s.familyRelationProband = FormatManager.getString(rs.getString("family_relation_proband"));
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return s;
    }

    private static boolean isLowQualitySample(int sampleId) {
        boolean isLowQualitySample = false;

        try {
            String sql = "SELECT low_quality FROM sample WHERE sample_id=?";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setInt(1, sampleId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                isLowQualitySample = rs.getInt("low_quality") == 1;
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return isLowQualitySample;
    }

    private static int getExperimentId(int sampleId) {
        int experimentId = Data.INTEGER_NA;

        try {
            String sql = "SELECT experiment_id FROM sample WHERE sample_id=?";

            PreparedStatement preparedStatement = DBManager.initPreparedStatement(sql);
            preparedStatement.setInt(1, sampleId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                experimentId = rs.getInt("experiment_id");
            }

            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return experimentId;
    }

    public static int getIdByName(String sampleName) {
        for (Sample sample : sampleList) {
            if (sample.getName().equals(sampleName)) {
                return sample.getId();
            }
        }

        return Data.INTEGER_NA;
    }

    public static int getIndexById(int sampleId) {
        Sample sample = sampleMap.get(sampleId);

        if (sample != null) {
            return sample.getIndex();
        } else {
            return Data.INTEGER_NA;
        }
    }

    public static ArrayList<Sample> getList() {
        return sampleList;
    }

    public static HashMap<Integer, Sample> getMap() {
        return sampleMap;
    }

    public static int getTotalSampleNum() {
        return totalSampleNum;
    }

    private static void resetSamplePheno4Linear() {
        for (Sample sample : sampleList) {
            sample.setPheno((byte) 0);
        }

        ctrlNum = sampleList.size();
        caseNum = 0;
    }

    public static StringJoiner getCaseIDSJ() {
        return caseIDSJ;
    }

    public static String getExistingSampleFile() {
        return existingSampleFile;
    }
}
