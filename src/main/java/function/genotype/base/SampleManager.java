package function.genotype.base;

import function.genotype.base.Carrier;
import function.genotype.base.NonCarrier;
import function.genotype.base.Sample;
import function.variant.base.Variant;
import global.Data;
import utils.CommandValue;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * @author nick
 */
public class SampleManager {

    public static final int[] Region_Length = {511, 255};

    private static ArrayList<Sample> sampleList = new ArrayList<Sample>();
    private static Hashtable<Integer, Sample> sampleTable = new Hashtable<Integer, Sample>();
    private static HashSet<Integer> chgvSampleIdSet = new HashSet<Integer>();

    private static int listSize;
    private static StringBuilder allSampleId = new StringBuilder();
    private static int caseNum = 0;
    private static int ctrlNum = 0;

    private static HashSet<Integer> evsSampleIdSet = new HashSet<Integer>();
    private static HashSet<Integer> evsEaSampleIdSet = new HashSet<Integer>();
    private static HashSet<Integer> evsAaSampleIdSet = new HashSet<Integer>();

    private static HashSet<Integer> evsIndelSampleIdSet = new HashSet<Integer>();
    private static HashSet<Integer> evsIndelEaSampleIdSet = new HashSet<Integer>();
    private static HashSet<Integer> evsIndelAaSampleIdSet = new HashSet<Integer>();

    private static ArrayList<Sample> failedSampleList = new ArrayList<Sample>();
    private static ArrayList<Sample> diffTypeSampleList = new ArrayList<Sample>();
    private static ArrayList<Sample> notExistSampleList = new ArrayList<Sample>();

    private static ArrayList<Sample> deletedSampleList = new ArrayList<Sample>();
    private static ArrayList<Sample> replacedSampleList = new ArrayList<Sample>();

    private static String tempCovarFile;
    private static String covariateFileTitle = "";

    // temp hack solution - phs000473 coverage restriction
    public static HashSet<Integer> phs000473SampleIdSet = new HashSet<Integer>();

    public static void init() {
        if (!CommandValue.isNonSampleAnalysis) {
            checkSampleFile();

            initAllTempTable();

            initEvsIndelSampleIdSetFromAnnoDB();

            if (CommandValue.isAllSample) {
                initAllSampleFromAnnoDB();
            } else {
                if (!CommandValue.sampleFile.isEmpty()) {
                    initFromSampleFile();
                }

                if (!CommandValue.evsSample.isEmpty()) {
                    initEvsSampleFromAnnoDB();
                }
            }

            initCovariate();

            initQuantitative();

            initSampleIndexAndSize();

            for (Sample sample : sampleList) {
                insertId2AllTable(sample);
            }

            outputSampleList();

            evsIndelSampleIdSet = null;
        }
    }

    private static void checkSampleFile() {
        if (CommandValue.sampleFile.isEmpty()
                && !CommandValue.isNonSampleAnalysis
                && CommandValue.evsSample.isEmpty()) {
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

    private static void initEvsIndelSampleIdSetFromAnnoDB() {
        try {
            String sqlCode = "SELECT distinct s.sample_name, s.sample_id "
                    + "FROM sample s, sample_attrib sa, sample_pipeline_step p "
                    + "WHERE s.sample_id = p.sample_id "
                    + "AND s.sample_id = sa.sample_id "
                    + "AND sample_attrib_type_id = 2 "
                    + "AND value_int > 0 "
                    + "AND pipeline_step_id = 10 "
                    + "AND step_status = 'completed' "
                    + "AND sample_name like 'evs_%' ";

            ResultSet rs = DBManager.executeQuery(sqlCode);

            while (rs.next()) {
                evsIndelSampleIdSet.add(rs.getInt("sample_id"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initAllSampleFromAnnoDB() {
        String sqlCode = "SELECT * FROM sample s, sample_pipeline_step p "
                + "WHERE s.sample_id = p.sample_id "
                + "AND p.pipeline_step_id = 10 "
                + "AND p.step_status = 'completed'";

        initSampleFromAnnoDB(sqlCode);
    }

    private static void initFromSampleFile() {
        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(CommandValue.sampleFile);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                String[] values = lineStr.split("\t");

                String familyId = values[0].trim();
                String individualId = values[1].trim();
                String paternalId = values[2].trim();
                String maternalId = values[3].trim();
                int sex = Integer.valueOf(values[4].trim());
                double pheno = Double.valueOf(values[5].trim());
                String sampleType = values[6].trim();
                String captureKit = values[7].trim();

                if (sampleType.equalsIgnoreCase("genome")) {
                    captureKit = "N/A";
                }

                int sampleId = getSampleId(individualId, sampleType, captureKit);

                if (sampleTable.containsKey(sampleId)) {
                    continue;
                }

                Sample sample = new Sample(sampleId, familyId, individualId,
                        paternalId, maternalId, sex, pheno, sampleType, captureKit);

                if (sampleId == Data.NA) {
                    checkSampleList(sample);
                    continue;
                }

                if (sample.getName().startsWith("phs000473_")) {
                    phs000473SampleIdSet.add(sample.getId());
                }

                sampleList.add(sample);
                sampleTable.put(sampleId, sample);

                if (sample.getName().startsWith("evs_ea")) {
                    evsEaSampleIdSet.add(sampleId);

                    if (evsIndelSampleIdSet.contains(sampleId)) {
                        evsIndelEaSampleIdSet.add(sampleId);
                    }
                } else if (sample.getName().startsWith("evs_aa")) {
                    evsAaSampleIdSet.add(sampleId);

                    if (evsIndelSampleIdSet.contains(sampleId)) {
                        evsIndelAaSampleIdSet.add(sampleId);
                    }
                } else {
                    chgvSampleIdSet.add(sampleId);
                }

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

    private static void initEvsSampleFromAnnoDB() {
        if (!evsEaSampleIdSet.isEmpty()
                || !evsAaSampleIdSet.isEmpty()) {
            ErrorManager.print("evs samples in the sample file is not compatable with "
                    + "--include-evs-sample option");
        }

        String evsPop = ""; // all

        if (!CommandValue.evsSample.equals("all")) {
            evsPop = CommandValue.evsSample;
        }

        String sqlCode = "SELECT * FROM sample s, sample_pipeline_step p "
                + "WHERE s.sample_id = p.sample_id "
                + "AND p.pipeline_step_id = 10 "
                + "AND p.step_status = 'completed' "
                + "AND s.sample_name like 'evs_" + evsPop + "%'";

        initSampleFromAnnoDB(sqlCode);
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
                String gender = rs.getString("gender").trim();

                int sex = 1; // M
                if (gender.equals("F")) {
                    sex = 2;
                }

                double pheno = 1;
                String sampleType = rs.getString("sample_type").trim();
                String captureKit = rs.getString("capture_kit").trim();

                Sample sample = new Sample(sampleId, familyId, individualId,
                        paternalId, maternalId, sex, pheno, sampleType, captureKit);

                if (sample.getName().startsWith("phs000473_")) {
                    phs000473SampleIdSet.add(sample.getId());
                }

                sampleList.add(sample);
                sampleTable.put(sampleId, sample);

                if (sample.getName().startsWith("evs_ea")) {
                    evsEaSampleIdSet.add(sampleId);

                    if (evsIndelSampleIdSet.contains(sampleId)) {
                        evsIndelEaSampleIdSet.add(sampleId);
                    }
                } else if (sample.getName().startsWith("evs_aa")) {
                    evsAaSampleIdSet.add(sampleId);

                    if (evsIndelSampleIdSet.contains(sampleId)) {
                        evsIndelAaSampleIdSet.add(sampleId);
                    }
                } else {
                    chgvSampleIdSet.add(sampleId);
                }

                countSampleNum(sample);
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void checkSampleList(Sample sample) {
        try {
            String sqlCode = "SELECT * FROM sample "
                    + "WHERE sample_name = '" + sample.getName() + "' "
                    + "AND sample_type = '" + sample.getType() + "' "
                    + "AND capture_kit = '" + sample.getCaptureKit() + "' "
                    + "AND sample_id IN (SELECT sample_id FROM sample_pipeline_step AS b "
                    + "WHERE pipeline_step_id = 10 AND step_status != 'completed')";

            ResultSet rs = DBManager.executeQuery(sqlCode);
            if (rs.next()) {
                failedSampleList.add(sample);
            } else {
                sqlCode = "SELECT * FROM sample "
                        + "WHERE sample_name = '" + sample.getName() + "' "
                        + "AND sample_id IN (SELECT sample_id FROM sample_pipeline_step AS b "
                        + "WHERE pipeline_step_id = 10 AND step_status = 'completed')";

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

    private static void outputSampleList() {
        printSampleList("The following samples are included in the analysis:",
                sampleList,
                "\nThe number of samples included in the analysis is "
                + sampleList.size() + " (" + caseNum + " cases and " + ctrlNum + " controls).\n\n");

        printSampleList("The following samples are labeled as failed in annodb:",
                failedSampleList,
                "\n");

        printSampleList("The following samples are in annodb but with a different seqtype or capture kit:",
                diffTypeSampleList,
                "\n");

        printSampleList("The following samples are not exist in annodb:",
                notExistSampleList,
                "\n");
    }

    private static void printSampleList(String startMessage,
            ArrayList<Sample> sampleList, String endMessage) {
        if (!sampleList.isEmpty()) {
            LogManager.writeLog(startMessage);

            for (Sample sample : sampleList) {
                LogManager.writeLog(
                        sample.getPrepId()
                        + "\t" + sample.getName()
                        + "\t" + sample.getType()
                        + "\t" + sample.getCaptureKit());
            }

            LogManager.writeLog(endMessage);
        }
    }

    private static void initCovariate() {
        if (CommandValue.covariateFile.isEmpty()) {
            return;
        }

        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(CommandValue.covariateFile);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                if (lineStr.isEmpty()) {
                    continue;
                }

                if (covariateFileTitle.isEmpty()) {
                    covariateFileTitle = lineStr;
                }

                lineStr = lineStr.toLowerCase();
                String[] values = lineStr.split("\t");

                Sample sample = getSampleByName(values[1]);

                if (sample != null) {
                    sample.initCovariate(values);
                }
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in covariate file: " + lineStr);

            ErrorManager.send(e);
        }

        resetSampleListByCovariate();
    }

    private static void resetSampleListByCovariate() {
        Iterator<Sample> it = sampleList.iterator();
        while (it.hasNext()) {
            Sample sample = it.next();
            if (sample.getCovariateList().isEmpty()) {
                it.remove();
                sampleTable.remove(sample.getId());
                chgvSampleIdSet.remove(sample.getId());
            }
        }
    }

    private static void initQuantitative() {
        if (CommandValue.quantitativeFile.isEmpty()) {
            return;
        }

        String lineStr = "";
        int lineNum = 0;

        try {
            File f = new File(CommandValue.quantitativeFile);
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
                sampleTable.remove(sample.getId());
                chgvSampleIdSet.remove(sample.getId());
            }
        }
    }

    public static void generateCovariateFile() {
        if (CommandValue.isCollapsingDoLogistic
                || CommandValue.isCollapsingDoLinear) {
            try {
                tempCovarFile = CommandValue.outputPath + "covariate.txt";

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

                    if (CommandValue.isCollapsingDoLogistic) {
                        bwCov.write(String.valueOf((int) (sample.getPheno() + 1)));
                    } else if (CommandValue.isCollapsingDoLinear) {
                        bwCov.write(String.valueOf(sample.getQuantitativeTrait()));
                    }

                    for (String covar : sample.getCovariateList()) {
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

    public static void recheckSampleList() {
        initDeletedAndReplacedSampleList();

        outputOutofDateSampleList(deletedSampleList, "Deleted");

        outputOutofDateSampleList(replacedSampleList, "Replaced");

        if (!deletedSampleList.isEmpty() || !replacedSampleList.isEmpty()) {
            LogManager.writeAndPrint("\nAlert: the data for the deleted/replaced "
                    + "sample used in the analysis is BAD data.");
        }
    }

    private static void initDeletedAndReplacedSampleList() {
        try {
            for (Sample sample : sampleList) {
                if (!sample.getName().startsWith("evs")) {
                    String time = getSampleFinishTime(sample.getId());

                    if (time.isEmpty()) {
                        deletedSampleList.add(sample);
                    } else if (!time.equals(sample.getFinishTime())) {
                        replacedSampleList.add(sample);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void outputOutofDateSampleList(ArrayList<Sample> list, String name) {
        if (!list.isEmpty()) {
            LogManager.writeAndPrintNoNewLine("\n" + name
                    + " sample list from Annotation database during the analysis:\n");

            for (Sample sample : list) {
                LogManager.writeAndPrintNoNewLine(
                        sample.getName() + "\t"
                        + sample.getType() + "\t"
                        + sample.getCaptureKit());
            }
        }
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

    private static void initAllTempTable() {
        createSqlTempTable(Data.ALL_SAMPLE_ID_TABLE);

        createSqlTempTable(Data.GENOME_SAMPLE_ID_TABLE);

        createSqlTempTable(Data.EXOME_SAMPLE_ID_TABLE);
    }

    private static void createSqlTempTable(String sqlTable) {
        try {
            Statement stmt = DBManager.createStatement();

            String sqlQuery = "CREATE TEMPORARY TABLE "
                    + sqlTable
                    + "(id int, PRIMARY KEY (id)) ENGINE=TokuDB";

            stmt.executeUpdate(sqlQuery);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void insertId2AllTable(Sample sample) {
        insertId2Table(sample.getId(), Data.ALL_SAMPLE_ID_TABLE);

        if (chgvSampleIdSet.contains(sample.getId())) {
            if (sample.getType().equalsIgnoreCase("genome")) {
                insertId2Table(sample.getId(), Data.GENOME_SAMPLE_ID_TABLE);
            } else {
                insertId2Table(sample.getId(), Data.EXOME_SAMPLE_ID_TABLE);
            }
        }
    }

    private static void insertId2Table(int id, String table) {
        try {
            DBManager.executeUpdate("INSERT IGNORE INTO " + table + " VALUES (" + id + ")");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static int getSampleId(String sampleName, String sampleType,
            String captureKit) throws Exception {
        int sampleId = Data.NA;

        try {
            String sqlCode = "SELECT sample_id FROM sample "
                    + "WHERE sample_name = '" + sampleName + "' "
                    + "AND sample_type = '" + sampleType + "' "
                    + "AND capture_kit = '" + captureKit + "' "
                    + "AND sample_id IN (SELECT sample_id FROM sample_pipeline_step AS b "
                    + "WHERE pipeline_step_id = 10 AND step_status = 'completed')";

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

    public static String getSampleFinishTime(int sampleId) {
        String time = "";

        try {
            String sqlCode = "SELECT exec_finish_time FROM sample_pipeline_step "
                    + "WHERE pipeline_step_id = 10 AND step_status = 'completed' "
                    + "AND sample_id = " + sampleId;

            ResultSet rs = DBManager.executeReadOnlyQuery(sqlCode);
            if (rs.next()) {
                time = rs.getString("exec_finish_time");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return time;
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
        Sample sample = sampleTable.get(sampleId);

        if (sample != null) {
            return sample.getIndex();
        } else {
            return Data.NA;
        }
    }

    public static ArrayList<Sample> getList() {
        return sampleList;
    }

    public static Hashtable<Integer, Sample> getTable() {
        return sampleTable;
    }

    public static int getListSize() {
        return listSize;
    }

    public static void initNonCarrierMap(Variant var,
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, NonCarrier> noncarrierMap) {
        ResultSet rs = null;
        String sql = "";

        int posIndex = var.getRegion().getStartPosition() % Data.COVERAGE_BLOCK_SIZE; // coverage data block size is 1024

        if (posIndex == 0) {
            posIndex = Data.COVERAGE_BLOCK_SIZE; // block boundary is ( ] 
        }

        int endPos = var.getRegion().getStartPosition() - posIndex + Data.COVERAGE_BLOCK_SIZE;

        try {
            for (int i = 0; i < Data.SAMPLE_TYPE.length; i++) {
                sql = "SELECT sample_id, min_coverage "
                        + "FROM " + Data.SAMPLE_TYPE[i]
                        + "_read_coverage_" + Data.COVERAGE_BLOCK_SIZE + "_chr"
                        + var.getRegion().getChrStr() + " c,"
                        + Data.SAMPLE_TYPE[i] + "_sample_id t "
                        + "WHERE c.position = " + endPos
                        + " AND c.sample_id = t.id";

                rs = DBManager.executeQuery(sql);
                while (rs.next()) {
                    NonCarrier noncarrier = new NonCarrier();

                    noncarrier.init(rs, posIndex);

                    if (!carrierMap.containsKey(noncarrier.getSampleId())) {

                        noncarrier.checkCoverageFilter(CommandValue.minCaseCoverageNoCall,
                                CommandValue.minCtrlCoverageNoCall);

                        noncarrier.checkValidOnXY(var);

                        if (noncarrier.getGenotype() != Data.NA) {
                            noncarrierMap.put(noncarrier.getSampleId(), noncarrier);
                        }
                    }
                }
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initCarrierMap(Variant var,
            HashMap<Integer, Carrier> carrierMap,
            HashMap<Integer, Carrier> evsEaCarrierMap,
            HashMap<Integer, Carrier> evsAaCarrierMap) {
        String sqlCarrier = "SELECT * "
                + "FROM called_" + var.getType() + " va,"
                + Data.ALL_SAMPLE_ID_TABLE + " t "
                + "WHERE va." + var.getType() + "_id = " + var.getVariantId()
                + " AND va.sample_id = t.id";

        ResultSet rs = null;
        try {
            rs = DBManager.executeQuery(sqlCarrier);

            while (rs.next()) {
                Carrier carrier = new Carrier();
                carrier.init(rs);

                carrier.checkCoverageFilter(CommandValue.minCaseCoverageCall,
                        CommandValue.minCtrlCoverageCall);

                carrier.checkQualityFilter();

                carrier.checkValidOnXY(var);

                if (evsEaSampleIdSet.contains(carrier.getSampleId())) {
                    evsEaCarrierMap.put(carrier.getSampleId(), carrier);
                } else if (evsAaSampleIdSet.contains(carrier.getSampleId())) {
                    evsAaCarrierMap.put(carrier.getSampleId(), carrier);
                }

                carrierMap.put(carrier.getSampleId(), carrier);
            }
            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getAllSampleId() {
        if (allSampleId.length() == 0) {
            boolean isFirst = true;
            int sampleId;
            for (Sample sample : sampleList) {
                sampleId = sample.getId();
                if (isFirst) {
                    allSampleId.append("(").append(sampleId);
                    isFirst = false;
                } else {
                    allSampleId.append(",").append(sampleId);
                }
            }

            allSampleId.append(")");
        }

        return allSampleId.toString();
    }

    public static boolean isEvsEaSampleId(int id, boolean isIndel) {
        if (isIndel) {
            return evsIndelEaSampleIdSet.contains(id);
        }

        return evsEaSampleIdSet.contains(id);
    }

    public static boolean isEvsAaSampleId(int id, boolean isIndel) {
        if (isIndel) {
            return evsIndelAaSampleIdSet.contains(id);
        }

        return evsAaSampleIdSet.contains(id);
    }

    public static int getEvsSampleNum(String evsSample, boolean isIndel) {
        if (evsSample.equals("ea")) {
            return getEvsEaSampleNum(isIndel);
        } else if (evsSample.equals("aa")) {
            return getEvsAaSampleNum(isIndel);
        }

        return Data.NA;
    }

    public static int getEvsEaSampleNum(boolean isIndel) {
        if (isIndel) {
            return evsIndelEaSampleIdSet.size();
        }

        return evsEaSampleIdSet.size();
    }

    public static int getEvsAaSampleNum(boolean isIndel) {
        if (isIndel) {
            return evsIndelAaSampleIdSet.size();
        }

        return evsAaSampleIdSet.size();
    }

    public static boolean isMale(int sampleId) {
        return sampleTable.get(sampleId).isMale();
    }

    public static HashSet<Integer> getEvsSampleIdSet() {
        if (evsSampleIdSet.isEmpty()) {
            evsSampleIdSet.addAll(evsEaSampleIdSet);
            evsSampleIdSet.addAll(evsAaSampleIdSet);
        }

        return evsSampleIdSet;
    }

    private static void resetSamplePheno4Linear() {
        for (Sample sample : sampleList) {
            sample.setPheno(0);
        }

        ctrlNum = sampleList.size();
        caseNum = 0;
    }
}
