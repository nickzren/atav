package atav.manager.data;

import atav.global.Data;
import atav.manager.utils.CommandValue;
import atav.manager.utils.ErrorManager;
import atav.manager.utils.LogManager;
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
        init(CommandValue.transcriptFile, currentTranscriptSet);

        if (CommandValue.isCcdsOnly) {
            init(ccdsTranscriptFile, ccdsTranscriptSet);

            resetTranscriptSet(ccdsTranscriptSet);
        }

        if (CommandValue.isCanonicalOnly) {
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
        ccdsTranscriptFile = "data" + File.separator + "ccds_transcript.txt";

        if (CommandValue.isDebug) {
            ccdsTranscriptFile = Data.CCDS_TRANSCRIPT_PATH;
        }
    }

    public static void initCanonicalTranscriptPath() {
        canonicalTranscriptFile = "data" + File.separator + "canonical_transcript.txt";

        if (CommandValue.isDebug) {
            canonicalTranscriptFile = Data.CANONICAL_TRANSCRIPT_PATH;
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
