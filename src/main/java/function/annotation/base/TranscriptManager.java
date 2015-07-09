package function.annotation.base;

import global.Data;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author nick
 */
public class TranscriptManager {

    private static HashSet<String> currentTranscriptSet = new HashSet<String>();
    private static HashSet<String> ccdsTranscriptSet = new HashSet<String>();
    private static HashSet<String> canonicalTranscriptSet = new HashSet<String>();
    private static String ccdsTranscriptFile = "";
    private static String canonicalTranscriptFile = "";

    public static void init() {
        init(CommonCommand.transcriptFile, currentTranscriptSet);

        if (CommonCommand.isCcdsOnly) {
            init(ccdsTranscriptFile, ccdsTranscriptSet);

            resetTranscriptSet(ccdsTranscriptSet);
        }

        if (CommonCommand.isCanonicalOnly) {
            init(canonicalTranscriptFile, canonicalTranscriptSet);

            resetTranscriptSet(canonicalTranscriptSet);
        }

        clear();
    }

    public static void init(String path, HashSet<String> set) {
        String lineStr = "";
        int lineNum = 0;

        try {
            if (path.isEmpty()) {
                return;
            }

            File f = new File(path);
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while ((lineStr = br.readLine()) != null) {
                lineNum++;

                lineStr = lineStr.replaceAll("( )+", "");

                if (lineStr.isEmpty()) {
                    continue;
                }

                if (set.contains(lineStr)) {
                    continue;
                }

                set.add(lineStr);
            }
        } catch (Exception e) {
            LogManager.writeAndPrintNoNewLine("\nError line ("
                    + lineNum + ") in transcript file: " + lineStr);

            ErrorManager.send(e);
        }
    }

    public static boolean isValid(String id) {
        if (currentTranscriptSet.isEmpty()) {
            return true;
        } else {
            return currentTranscriptSet.contains(id);
        }
    }

    public static int getSize() {
        return currentTranscriptSet.size();
    }

    public static void initCCDSTranscriptPath() {
        ccdsTranscriptFile = Data.CCDS_TRANSCRIPT_PATH;

        if (CommonCommand.isDebug) {
            ccdsTranscriptFile = Data.RECOURCE_PATH + ccdsTranscriptFile;
        }
    }

    public static void initCanonicalTranscriptPath() {
        canonicalTranscriptFile = Data.CANONICAL_TRANSCRIPT_PATH;

        if (CommonCommand.isDebug) {
            canonicalTranscriptFile = Data.RECOURCE_PATH + canonicalTranscriptFile;
        }
    }

    public static void resetTranscriptSet(HashSet<String> set) {
        if (currentTranscriptSet.isEmpty()) {
            currentTranscriptSet = (HashSet<String>) set.clone();
        } else {
            Iterator<String> iterator = currentTranscriptSet.iterator();
            while (iterator.hasNext()) {
                String transcript = iterator.next();
                if (!set.contains(transcript)) {
                    iterator.remove();
                }
            }
        }
    }

    public static void clear() {
        ccdsTranscriptSet.clear();
        canonicalTranscriptSet.clear();
    }
}
