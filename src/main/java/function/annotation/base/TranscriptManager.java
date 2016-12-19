package function.annotation.base;

import global.Data;
import utils.CommonCommand;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class TranscriptManager {

    private static final String CCDS_TRANSCRIPT_PATH = "data/transcript/ccds_ensemble_v74.txt";
    private static final String CANONICAL_TRANSCRIPT_PATH = "data/transcript/canonical_ensemble_v74.txt";

    private static HashMap<String, HashSet<Integer>> transcriptMap = new HashMap<>();
    private static HashMap<String, HashSet<Integer>> ccdsTranscriptMap = new HashMap<>();

    private static String ccdsTranscriptFile = "";
    private static String canonicalTranscriptFile = "";

    public static final int TRANSCRIPT_LENGTH = 15;

    public static void init() {
        // init ccds transcript
        TranscriptManager.initCCDSTranscriptPath();
        initFromTranscriptFile(ccdsTranscriptFile, ccdsTranscriptMap);
        if (AnnotationLevelFilterCommand.isCcdsOnly) {
            resetTranscriptSet(ccdsTranscriptMap);
        }

        // init canonical transcript
        if (AnnotationLevelFilterCommand.isCanonicalOnly) {
            HashMap<String, HashSet<Integer>> canonicalTranscriptMap = new HashMap<>();
            initCanonicalTranscriptPath();
            initFromTranscriptFile(canonicalTranscriptFile, canonicalTranscriptMap);
            resetTranscriptSet(canonicalTranscriptMap);
        }
    }

    public static void initFromTranscriptFile(String path, HashMap<String, HashSet<Integer>> map) {
        if (path.isEmpty()) {
            return;
        }

        try {
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("( )+", "");

                if (!line.isEmpty()) {
                    String[] tmp = line.split("\t"); // chr & transcript id

                    HashSet<Integer> idSet = map.get(tmp[0]);

                    if (idSet == null) {
                        idSet = new HashSet<>();
                        map.put(tmp[0], idSet);
                    }

                    idSet.add(Integer.valueOf(tmp[1]));
                }
            }
            br.close();
            fr.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public static boolean isCCDSTranscript(String chr, int id) {
        HashSet<Integer> idSet = ccdsTranscriptMap.get(chr);

        if (idSet == null) {
            return false;
        }

        return idSet.contains(id);
    }

    private static void initCCDSTranscriptPath() {
        ccdsTranscriptFile = CCDS_TRANSCRIPT_PATH;

        if (CommonCommand.isDebug) {
            ccdsTranscriptFile = Data.RECOURCE_PATH + ccdsTranscriptFile;
        }
    }

    private static void initCanonicalTranscriptPath() {
        canonicalTranscriptFile = CANONICAL_TRANSCRIPT_PATH;

        if (CommonCommand.isDebug) {
            canonicalTranscriptFile = Data.RECOURCE_PATH + canonicalTranscriptFile;
        }
    }

    public static void resetTranscriptSet(HashMap<String, HashSet<Integer>> map) {
        if (transcriptMap.isEmpty()) {
            transcriptMap = (HashMap<String, HashSet<Integer>>) map.clone();
        } else {
            for (Entry<String, HashSet<Integer>> entry : map.entrySet()) {
                HashSet<Integer> idSet = transcriptMap.get(entry.getKey());

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
}
