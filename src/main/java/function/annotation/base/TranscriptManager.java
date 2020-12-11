package function.annotation.base;

import function.variant.base.RegionManager;
import global.Data;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.DBManager;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class TranscriptManager {

    public static final String TMP_TRANSCRIPT_TABLE = "tmp_transcript_chr"; // need to append chr in real time

    private static final String CCDS_TRANSCRIPT_PATH = Data.ATAV_HOME + "data/transcript/ccds_transcripts_ens87.txt.gz";
    private static final String CANONICAL_TRANSCRIPT_PATH = Data.ATAV_HOME + "data/transcript/canonical_transcripts_ens87.txt.gz";

    private static HashMap<String, HashSet<Integer>> allTranscriptIdMap = new HashMap<>();
    private static HashMap<String, HashSet<Integer>> ccdsTranscriptIdMap = new HashMap<>();
    private static HashMap<String, HashSet<Integer>> canonicalTranscriptIdMap = new HashMap<>();
    private static HashMap<String, HashSet<Integer>> transcriptBoundaryIdMap = new HashMap<>();

    private static HashMap<Integer, TranscriptBoundary> transcriptBoundaryMap = new HashMap<>();

    private static final HashMap<String, PreparedStatement> preparedStatement4TranscriptCheckMap = new HashMap<>();

    public static final int TRANSCRIPT_LENGTH = 15;

    // --ccds-only, --canonical-only or --transcript-boundary set to true
    private static boolean isUsed = false;

    public static void init() {
        // init ccds transcript
        initFromTranscriptFile(CCDS_TRANSCRIPT_PATH, ccdsTranscriptIdMap);
        if (AnnotationLevelFilterCommand.isCcdsOnly) {
            isUsed = true;
            resetTranscriptSet(ccdsTranscriptIdMap);
        }

        // init canonical transcript
        initFromTranscriptFile(CANONICAL_TRANSCRIPT_PATH, canonicalTranscriptIdMap);
        if (AnnotationLevelFilterCommand.isCanonicalOnly) {
            isUsed = true;
            resetTranscriptSet(canonicalTranscriptIdMap);
        }

        // init transcript boundary
        if (!AnnotationLevelFilterCommand.transcriptBoundaryFile.isEmpty()) {
            isUsed = true;
            initTranscriptBoundary();
            resetTranscriptSet(transcriptBoundaryIdMap);
        }

        // init temp table
        if (isUsed) {
            initTempTable();
        }
    }

    private static void initPreparedStatement4TranscriptCheck() {
        for (String chr : RegionManager.getChrList()) {
            String sql = "SELECT transcript_stable_id FROM variant_chr" + chr + " WHERE transcript_stable_id=? limit 1";
            preparedStatement4TranscriptCheckMap.put(chr, DBManager.initPreparedStatement(sql));
        }
    }

    private static boolean isTranscriptStableIdValid(String chr, int id) {
        try {
            preparedStatement4TranscriptCheckMap.get(chr).setInt(1, id);
            ResultSet rs = preparedStatement4TranscriptCheckMap.get(chr).executeQuery();
            boolean isValid = rs.next();
            rs.close();

            return isValid;
        } catch (Exception e) {
            ErrorManager.send(e);
            return false;
        }
    }

    private static void initTranscriptBoundary() {
        initPreparedStatement4TranscriptCheck();

        try {
            File f = new File(AnnotationLevelFilterCommand.transcriptBoundaryFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    TranscriptBoundary transcriptBoundary = new TranscriptBoundary(line);

                    String chr = transcriptBoundary.getChr();

                    // if used --region
                    if (!RegionManager.isChrContained(chr)) {
                        continue;
                    }

                    int id = transcriptBoundary.getId();

                    // if used --ccds-only or --canonical-only
                    if (!isCCDSORCanonicalValid(chr, id)) {
                        continue;
                    }

                    if (!isTranscriptStableIdValid(chr, id)) {
                        LogManager.writeAndPrint("Invalid transcript: " + line.split("\\s+")[0]);
                        continue;
                    }

                    transcriptBoundaryMap.put(id, transcriptBoundary);

                    transcriptBoundaryIdMap.putIfAbsent(chr, new HashSet<>());
                    transcriptBoundaryIdMap.get(chr).add(id);
                }
            }

            br.close();
            fr.close();

            if (transcriptBoundaryMap.isEmpty()) {
                ErrorManager.print("--transcript-boundary input does not have any valid data.", ErrorManager.INPUT_PARSING);
            }

            // reset chr
            RegionManager.clear();
            RegionManager.initChrRegionList(transcriptBoundaryIdMap.keySet().toArray(new String[transcriptBoundaryIdMap.keySet().size()]));
            RegionManager.sortRegionList();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private static void initTempTable() {
        try {
            HashMap<String, StringJoiner> chrAllTranscriptMap = new HashMap<>();

            for (String chr : RegionManager.getChrList()) {
                for (int id : allTranscriptIdMap.getOrDefault(chr, new HashSet<>())) {
                    chrAllTranscriptMap.putIfAbsent(chr, new StringJoiner(","));
                    chrAllTranscriptMap.get(chr).add("('" + id + "')");
                }

                if (chrAllTranscriptMap.getOrDefault(chr, new StringJoiner(",")).length() > 0) {
                    Statement stmt = DBManager.createStatementByConcurReadOnlyConn();

                    // create table
                    stmt.executeUpdate("CREATE TEMPORARY TABLE " + TMP_TRANSCRIPT_TABLE + chr + "("
                            + "input_transcript_stable_id int(11) NOT NULL, "
                            + "PRIMARY KEY (input_transcript_stable_id)) ENGINE=TokuDB;");

                    // insert values
                    stmt.executeUpdate("INSERT IGNORE INTO " + TMP_TRANSCRIPT_TABLE + chr
                            + " values " + chrAllTranscriptMap.get(chr).toString());

                    stmt.closeOnCompletion();
                }
            }

            // free memory
            allTranscriptIdMap.clear();
            transcriptBoundaryIdMap.clear();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void initFromTranscriptFile(String path, HashMap<String, HashSet<Integer>> map) {
        if (path.isEmpty()) {
            return;
        }

        try {
            File f = new File(path);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("( )+", "");

                if (!line.isEmpty()) {
                    String[] tmp = line.split("\t"); // chr & transcript id

                    map.putIfAbsent(tmp[0], new HashSet<>());
                    map.get(tmp[0]).add(Integer.valueOf(tmp[1]));
                }
            }
            br.close();
            decoder.close();
            in.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public static boolean isCCDSTranscript(String chr, int id) {
        HashSet<Integer> idSet = ccdsTranscriptIdMap.get(chr);

        if (idSet == null) {
            return false;
        }

        return idSet.contains(id);
    }

    public static boolean isCanonicalTranscript(String chr, int id) {
        HashSet<Integer> idSet = canonicalTranscriptIdMap.get(chr);

        if (idSet == null) {
            return false;
        }

        return idSet.contains(id);
    }
    
    public static void resetTranscriptSet(HashMap<String, HashSet<Integer>> map) {
        if (allTranscriptIdMap.isEmpty()) {
            allTranscriptIdMap = (HashMap<String, HashSet<Integer>>) map.clone();
        } else {
            for (String chr : RegionManager.ALL_CHR) {
                if (!map.containsKey(chr)) {
                    allTranscriptIdMap.remove(chr);
                }
            }

            for (Entry<String, HashSet<Integer>> entry : map.entrySet()) {
                HashSet<Integer> idSet = allTranscriptIdMap.get(entry.getKey());

                if (idSet == null) {
                    continue;
                }

                Iterator<Integer> iterator = idSet.iterator();
                while (iterator.hasNext()) {
                    int id = iterator.next();
                    if (!entry.getValue().contains(id)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static boolean isCCDSORCanonicalValid(String chr, int id) {
        if (allTranscriptIdMap.isEmpty()) {
            return true;
        } else {
            HashSet<Integer> idSet = allTranscriptIdMap.get(chr);

            if (idSet == null) {
                return false;
            }

            return idSet.contains(id);
        }
    }

    public static boolean isTranscriptBoundaryValid(int id, int pos) {
        if (transcriptBoundaryMap.isEmpty()) {
            return true;
        } else {
            TranscriptBoundary t = transcriptBoundaryMap.get(id);

            if (t == null) {
                return false;
            }

            return t.isContained(pos);
        }
    }

    public static HashMap<Integer, TranscriptBoundary> getTranscriptBoundaryMap() {
        return transcriptBoundaryMap;
    }

    public static boolean isUsed() {
        return isUsed;
    }

    public static String getId(String id) {
        StringBuilder idSB = new StringBuilder(id);

        int zeroStringLength = TRANSCRIPT_LENGTH - idSB.length() - 4;

        for (int i = 0; i < zeroStringLength; i++) {
            idSB.insert(0, 0);
        }

        idSB.insert(0, "ENST");

        return idSB.toString();
    }
}
