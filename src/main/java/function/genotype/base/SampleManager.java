package function.genotype.base;

import function.genotype.collapsing.CollapsingCommand;
import function.genotype.statistics.StatisticsCommand;
import global.Data;
import utils.CommonCommand;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class SampleManager {

    private static final String SAMPLE_GROUP_RESTRICTION_PATH = "config/sample.group.restriction.txt";
    private static final String USER_GROUP_RESTRICTION_PATH = "config/user.group.restriction.txt";

    public static final String[] SAMPLE_TYPE = {"genome", "exome"};
    public static final String ALL_SAMPLE_ID_TABLE = "all_sample_id";
    public static final String GENOME_SAMPLE_ID_TABLE = "genome_sample_id";
    public static final String EXOME_SAMPLE_ID_TABLE = "exome_sample_id";

    // sample permission
    private static HashMap<String, String> sampleGroupMap = new HashMap<>();// sample_name, group_name
    private static HashMap<String, HashSet<String>> userGroupMap = new HashMap<>();// group_name, user set

    private static ArrayList<Sample> sampleList = new ArrayList<>();
    private static HashMap<Integer, Sample> sampleMap = new HashMap<>();

    private static int listSize; // case + ctrl
    private static int caseNum = 0;
    private static int ctrlNum = 0;

    // sample id StringBuilder is just temp used for creating temp tables
    private static StringBuilder allSampleIdSb = new StringBuilder();
    private static StringBuilder exomeSampleIdSb = new StringBuilder();
    private static StringBuilder genomeSampleIdSb = new StringBuilder();

    private static ArrayList<Sample> failedSampleList = new ArrayList<>();
    private static ArrayList<Sample> diffTypeSampleList = new ArrayList<>();
    private static ArrayList<Sample> notExistSampleList = new ArrayList<>();

    private static ArrayList<Sample> restrictedSampleList = new ArrayList<>();

    private static String tempCovarFile;
    private static String covariateFileTitle = "";
    private static int covariateNum = Data.NA;

    public static void init() {
        if (CommonCommand.isNonSampleAnalysis) {
            return;
        }

        initSamplePermission();

        checkSampleFile();

        if (!GenotypeLevelFilterCommand.sampleFile.isEmpty()) {
            initFromSampleFile();
        } else if (GenotypeLevelFilterCommand.isAllSample) {
            initAllSampleFromAnnoDB();
        }

        initCovariate();

        initQuantitative();

        initSampleIndexAndSize();

        initTempTables();

        outputSampleListSummary();
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
                && !GenotypeLevelFilterCommand.isAllSample) {
            ErrorManager.print("Please specify your sample file: --sample $PATH");
        }
    }

    private static void initSampleIndexAndSize() {
        int index = 0;

        for (Sample sample : sampleList) {
            sample.setIndex(index++);
        }

        listSize = sampleList.size();
    }

    private static void initAllSampleFromAnnoDB() {
        String sqlCode = "SELECT * FROM sample WHERE sample_finished = 1 and sample_failure = 0";

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

                lineStr = lineStr.replaceAll("( )+", "");

                String[] values = lineStr.split("\t");

                String familyId = values[0];
                String individualId = values[1];
                String paternalId = values[2];
                String maternalId = values[3];

                int sex = Integer.valueOf(values[4]);
                if (sex != 1 && sex != 2) {
                    ErrorManager.print("\nWrong Sex value: " + sex
                            + " (line " + lineNum + " in sample file)");
                }

                int pheno = Integer.valueOf(values[5]);
                if (pheno != 1 && pheno != 2) {
                    ErrorManager.print("\nWrong Phenotype value: " + pheno
                            + " (line " + lineNum + " in sample file)");
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

                if (sampleId == Data.NA) {
                    checkSampleList(sample);
                    continue;
                }

                sampleList.add(sample);
                sampleMap.put(sampleId, sample);

                countSampleNum(sample);
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
                String paternalId = "0";
                String maternalId = "0";
                int sex = 1; // male
                int pheno = 1; // control
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
    }

    private static void printSampleList(String startMessage,
            ArrayList<Sample> sampleList) {
        if (!sampleList.isEmpty()) {
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

                    if (covariateNum == Data.NA) {
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
                double value = Double.valueOf(values[1]);

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
            if (sample.getQuantitativeTrait() == Data.NA) {
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
                        bwCov.write(String.valueOf((int) (sample.getPheno() + 1)));
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
        createTempTables();

        initSampleIdSbs();

        insertSampleId2Tables();

        clearSampleIdSbs();
    }

    private static void createTempTables() {
        createTempTable(ALL_SAMPLE_ID_TABLE);

        createTempTable(GENOME_SAMPLE_ID_TABLE);

        createTempTable(EXOME_SAMPLE_ID_TABLE);
    }

    private static void createTempTable(String sqlTable) {
        try {
            Statement stmt = DBManager.createStatement();

            String sqlQuery = "CREATE TEMPORARY TABLE "
                    + sqlTable
                    + "(id int, PRIMARY KEY (id)) ENGINE=TokuDB";

            stmt.executeUpdate(StringEscapeUtils.escapeSql(sqlQuery));
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initSampleIdSbs() {
        for (Sample sample : sampleList) {
            addToSampleIdSb(allSampleIdSb, sample.getId());

            if (sample.getType().equalsIgnoreCase("genome")) {
                addToSampleIdSb(genomeSampleIdSb, sample.getId());
            } else {
                addToSampleIdSb(exomeSampleIdSb, sample.getId());
            }
        }

        FormatManager.deleteLastComma(allSampleIdSb);
        FormatManager.deleteLastComma(genomeSampleIdSb);
        FormatManager.deleteLastComma(exomeSampleIdSb);
    }

    private static void addToSampleIdSb(StringBuilder sb, int id) {
        sb.append("(").append(id).append(")").append(",");
    }

    private static void insertSampleId2Tables() {
        insertId2Table(allSampleIdSb.toString(), ALL_SAMPLE_ID_TABLE);
        insertId2Table(genomeSampleIdSb.toString(), GENOME_SAMPLE_ID_TABLE);
        insertId2Table(exomeSampleIdSb.toString(), EXOME_SAMPLE_ID_TABLE);
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

    private static void clearSampleIdSbs() {
        allSampleIdSb.setLength(0); // free memory
        genomeSampleIdSb.setLength(0);
        exomeSampleIdSb.setLength(0);
    }

    private static int getSampleId(String sampleName, String sampleType,
            String captureKit) throws Exception {
        int sampleId = Data.NA;

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

    public static int getSamplePrepId(int sampleId) {
        int prepId = Data.NA;

        try {
            String sqlCode = "SELECT prep_id FROM sample WHERE sample_id = " + sampleId;

            ResultSet rs = DBManager.executeReadOnlyQuery(sqlCode);
            if (rs.next()) {
                prepId = rs.getInt("prep_id");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return prepId;
    }

    public static int getIdByName(String sampleName) {
        for (Sample sample : sampleList) {
            if (sample.getName().equals(sampleName)) {
                return sample.getId();
            }
        }

        return Data.NA;
    }

    public static int getIndexById(int sampleId) {
        Sample sample = sampleMap.get(sampleId);

        if (sample != null) {
            return sample.getIndex();
        } else {
            return Data.NA;
        }
    }

    public static ArrayList<Sample> getList() {
        return sampleList;
    }

    public static HashMap<Integer, Sample> getMap() {
        return sampleMap;
    }

    public static int getListSize() {
        return listSize;
    }

    public static boolean isMale(int sampleId) {
        return sampleMap.get(sampleId).isMale();
    }

    private static void resetSamplePheno4Linear() {
        for (Sample sample : sampleList) {
            sample.setPheno(0);
        }

        ctrlNum = sampleList.size();
        caseNum = 0;
    }
}
