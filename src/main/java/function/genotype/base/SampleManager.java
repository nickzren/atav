package function.genotype.base;

import function.genotype.collapsing.CollapsingCommand;
import function.genotype.statistics.StatisticsCommand;
import global.Data;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author nick
 */
public class SampleManager {

    private static final String SAMPLE_GROUP_RESTRICTION_PATH = Data.ATAV_HOME + "config/sample.group.restriction.txt";
    private static final String USER_GROUP_RESTRICTION_PATH = Data.ATAV_HOME + "config/user.group.restriction.txt";

    public static final String TMP_SAMPLE_ID_TABLE = "tmp_sample_id";

    // sample permission
    private static HashMap<String, String> sampleGroupMap = new HashMap<>();// sample_name, group_name
    private static HashMap<String, HashSet<String>> userGroupMap = new HashMap<>();// group_name, user set

    private static ArrayList<Sample> sampleList = new ArrayList<>();
    private static HashMap<Integer, Sample> sampleMap = new HashMap<>();
    private static HashSet<String> sampleNameSet = new HashSet<>();

    private static int totalSampleNum; // case + ctrl
    private static int caseNum = 0;
    private static int ctrlNum = 0;

    private static ArrayList<Sample> failedSampleList = new ArrayList<>();
    private static ArrayList<Sample> diffTypeSampleList = new ArrayList<>();
    private static ArrayList<Sample> notExistSampleList = new ArrayList<>();

    private static boolean isSampleFileCorrect = true;

    private static ArrayList<Sample> restrictedSampleList = new ArrayList<>();

    private static String tempCovarFile;
    private static String covariateFileTitle = "";
    private static int covariateNum = Data.INTEGER_NA;

    // output existing / qualifed samples
    private static BufferedWriter bwExistingSample = null;
    private final static String existingSampleFile = CommonCommand.outputPath + "existing.sample.txt";

    // output all existing samples
    private static BufferedWriter bwAllSample = null;
    private final static String allSampleFile = CommonCommand.outputPath + "all.sample.txt";

    private static StringJoiner caseIDSJ = new StringJoiner(",");

    // igm gnomad sample
    private static final String IGM_GNOMAD_SAMPLE_PATH = Data.ATAV_HOME + "data/sample/igm_gnomad_sample.txt";
    private static Set<String> excludeIGMGnomadSampleSet = new HashSet<>();

    public static void init() {
        if (CommonCommand.isNonSampleAnalysis) {
            return;
        }

        initSamplePermission();

        checkSampleFile();

        initExcludeIGMGnomADSample();

        if (!GenotypeLevelFilterCommand.sampleFile.isEmpty()) {
            initExistingSampleFile();
            initFromSampleFile();
            closeExistingSampleFile();
        } else if (GenotypeLevelFilterCommand.isAllSample
                || GenotypeLevelFilterCommand.isAllExome) {
            initAllSampleFile();
            initAllSampleFromAnnoDB();
            closeAllSampleFile();
            GenotypeLevelFilterCommand.sampleFile = allSampleFile;
        }

        initCovariate();

        initQuantitative();

        initSampleIndexAndSize();

        initTempTables();

        outputSampleListSummary();
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

    private static void initSamplePermission() {
        initSampleGroup();

        initUserGroup();
    }

    private static void initSampleGroup() {
        try {
            File f = new File(SAMPLE_GROUP_RESTRICTION_PATH);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (!lineStr.isEmpty()) {
                    String[] tmp = lineStr.trim().split("\t");

                    sampleGroupMap.put(tmp[0], tmp[1]);
                }
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initUserGroup() {
        try {
            File f = new File(USER_GROUP_RESTRICTION_PATH);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (!lineStr.isEmpty()) {
                    String[] tmp = lineStr.trim().split("\t");

                    String groupName = tmp[0];
                    String[] users = tmp[1].split(",");

                    HashSet<String> userSet = userGroupMap.get(groupName);

                    if (userSet == null) {
                        userSet = new HashSet<>();
                        userGroupMap.put(groupName, userSet);
                    }

                    for (String user : users) {
                        userSet.add(user);
                    }
                }
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void checkSampleFile() {
        if (GenotypeLevelFilterCommand.sampleFile.isEmpty()
                && !GenotypeLevelFilterCommand.isAllSample
                && !GenotypeLevelFilterCommand.isAllExome) {
            ErrorManager.print("Please specify your sample file: --sample $PATH", ErrorManager.INPUT_PARSING);
        }
    }

    private static void initExcludeIGMGnomADSample() {
        if (GenotypeLevelFilterCommand.isExcludeIGMGnomadSample) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(IGM_GNOMAD_SAMPLE_PATH))) {
                excludeIGMGnomadSampleSet = br.lines().collect(Collectors.toSet());
            } catch (IOException e) {
                ErrorManager.print("Error: parsing IGM GnomAD Sample file", ErrorManager.INPUT_PARSING);
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

    private static void initAllSampleFromAnnoDB() {
        String sqlCode = "SELECT * FROM sample "
                + "WHERE sample_type != 'custom_capture' "
                + "and sample_finished = 1 "
                + "and sample_failure = 0 ";

        if (GenotypeLevelFilterCommand.isAllExome) {
            sqlCode += " and sample_type = 'Exome' and sample_name not like 'SRR%'";
        }

        initSampleFromAnnoDB(sqlCode);
    }

    private static void initFromSampleFile() {
        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(GenotypeLevelFilterCommand.sampleFile);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                String[] values = lineStr.replaceAll("( )+", "").split("\t");

                String familyId = values[0];

                if (familyId.equals("N/A")) {
                    ErrorManager.print("\nWrong FamilyID: " + familyId
                            + " (line " + lineNum + " in sample file)", ErrorManager.INPUT_PARSING);
                }

                String individualId = values[1];

                if (GenotypeLevelFilterCommand.isExcludeIGMGnomadSample
                        && excludeIGMGnomadSampleSet.contains((individualId))) {
                    LogManager.writeAndPrint("Excluded IGM gnomAD Sample: " + individualId);
                    continue;
                }

                if (!GenotypeLevelFilterCommand.isDisableCheckDuplicateSample) {
                    if (!sampleNameSet.contains(individualId)) {
                        sampleNameSet.add(individualId);
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

                int sampleId = getSampleId(individualId, sampleType, captureKit);

                if (sampleMap.containsKey(sampleId)) {
                    continue;
                }

                Sample sample = new Sample(sampleId, familyId, individualId,
                        paternalId, maternalId, sex, pheno, sampleType, captureKit);

                if (!checkSamplePermission(sample)) {
                    restrictedSampleList.add(sample);
                    continue;
                }

                if (sampleId == Data.INTEGER_NA) {
                    checkSampleList(sample);
                    continue;
                }

                if (sample.isCase()) {
                    caseIDSJ.add(String.valueOf(sample.getId()));
                }

                sampleList.add(sample);
                sampleMap.put(sampleId, sample);

                countSampleNum(sample);

                bwExistingSample.write(lineStr);
                bwExistingSample.newLine();
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in sample file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    private static void initSampleFromAnnoDB(String sqlCode) {
        try {
            ResultSet rs = DBManager.executeQuery(sqlCode);

            while (rs.next()) {
                int sampleId = rs.getInt("sample_id");
                String familyId = rs.getString("sample_name").trim();
                String individualId = rs.getString("sample_name").trim();
                
                if (GenotypeLevelFilterCommand.isExcludeIGMGnomadSample
                        && excludeIGMGnomadSampleSet.contains((individualId))) {
                    LogManager.writeAndPrint("Excluded IGM gnomAD Sample: " + individualId);
                    continue;
                }
                
                String paternalId = "0";
                String maternalId = "0";
                byte sex = 1; // male
                byte pheno = 1; // control
                String sampleType = rs.getString("sample_type").trim();
                String captureKit = rs.getString("capture_kit").trim();

                Sample sample = new Sample(sampleId, familyId, individualId,
                        paternalId, maternalId, sex, pheno, sampleType, captureKit);

                if (!checkSamplePermission(sample)) {
                    restrictedSampleList.add(sample);
                    continue;
                }

                sampleList.add(sample);
                sampleMap.put(sampleId, sample);

                countSampleNum(sample);

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

    private static boolean checkSamplePermission(Sample sample) {
        if (sampleGroupMap.containsKey(sample.getName())) {
            String groupName = sampleGroupMap.get(sample.getName());

            HashSet<String> userSet = userGroupMap.get(groupName);

            if (userSet.contains(Data.userName)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true; // not in sample restricted list
        }

    }

    private static void checkSampleList(Sample sample) {
        try {
            String sqlCode = "SELECT * FROM sample "
                    + "WHERE sample_name = '" + sample.getName() + "' "
                    + "AND sample_type = '" + sample.getType() + "' "
                    + "AND capture_kit = '" + sample.getCaptureKit() + "' "
                    + "AND sample_failure = 1 ";

            ResultSet rs = DBManager.executeQuery(sqlCode);
            if (rs.next()) {
                failedSampleList.add(sample);
            } else {
                sqlCode = "SELECT * FROM sample "
                        + "WHERE sample_name = '" + sample.getName() + "' "
                        + "AND sample_finished = 1";

                rs = DBManager.executeQuery(sqlCode);

                if (rs.next()) {
                    diffTypeSampleList.add(sample);
                } else {
                    notExistSampleList.add(sample);
                }
            }

            rs.close();
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
        } else {
            // no need to keep existing samples file since there are no problems
            File file = new File(existingSampleFile);
            file.delete();
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
                        sample.getName()
                        + "\t" + sample.getType()
                        + "\t" + sample.getCaptureKit());
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
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                lineStr = lineStr.replaceAll("( )+", "");

                if (covariateFileTitle.isEmpty()) {
                    covariateFileTitle = lineStr;
                    continue;
                }

                String[] values = lineStr.split("\t");

                Sample sample = getSampleByName(values[1]);

                if (sample != null) {
                    sample.initCovariate(values);

                    if (covariateNum == Data.INTEGER_NA) {
                        covariateNum = sample.getCovariateList().size();
                    }
                }
            }
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
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

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

                String[] titles = covariateFileTitle.split("\t");

                for (int i = 2; i < titles.length; i++) {
                    bwCov.write("\t" + titles[i]);
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

    private static Sample getSampleByName(String name) {
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
                    + "(input_sample_id mediumint, PRIMARY KEY (input_sample_id)) ENGINE=TokuDB";

            stmt.executeUpdate(StringEscapeUtils.escapeSql(sqlQuery));
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void insertId2Table(String ids, String table) {
        try {
            if (!ids.isEmpty()) {
                DBManager.executeUpdate("INSERT INTO " + table + " VALUES " + ids);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static int getSampleId(String sampleName, String sampleType,
            String captureKit) throws Exception {
        int sampleId = Data.INTEGER_NA;

        try {
            String sqlCode = "SELECT sample_id FROM sample "
                    + "WHERE sample_name = '" + sampleName + "' "
                    + "AND sample_type = '" + sampleType + "' "
                    + "AND capture_kit = '" + captureKit + "' "
                    + "AND sample_finished = 1 "
                    + "AND sample_failure = 0";

            ResultSet rs = DBManager.executeQuery(sqlCode);
            if (rs.next()) {
                sampleId = rs.getInt("sample_id");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return sampleId;
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
}
