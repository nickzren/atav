package function.annotation.base;

import global.Data;
import utils.CommonCommand;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 *
 * @author nick
 */
public class TranscriptManager {

    private static final String CCDS_TRANSCRIPT_PATH = "data/ccds_transcript.txt";
    private static final String CANONICAL_TRANSCRIPT_PATH = "data/canonical_transcript.txt";

    private static HashSet<String> currentTranscriptSet = new HashSet<>();
    private static HashSet<String> ccdsTranscriptSet = new HashSet<>();
    private static HashSet<String> canonicalTranscriptSet = new HashSet<>();
    private static String ccdsTranscriptFile = "";
    private static String canonicalTranscriptFile = "";

    public static final int TRANSCRIPT_LENGTH = 15;

    public static void init() {
        // init transcript set from --transcript input file
        initFromTranscriptFile(AnnotationLevelFilterCommand.transcriptFile, currentTranscriptSet);

        // init ccds transcript
        TranscriptManager.initCCDSTranscriptPath();
        initFromTranscriptFile(ccdsTranscriptFile, ccdsTranscriptSet);
        if (AnnotationLevelFilterCommand.isCcdsOnly) {
            resetTranscriptSet(ccdsTranscriptSet);
        }

        // init canonical transcript
        if (AnnotationLevelFilterCommand.isCanonicalOnly) {
            initCanonicalTranscriptPath();
            initFromTranscriptFile(canonicalTranscriptFile, canonicalTranscriptSet);
            resetTranscriptSet(canonicalTranscriptSet);
        }

        canonicalTranscriptSet.clear(); // free memory
    }

    public static void initFromTranscriptFile(String path, HashSet<String> set) {
        if (path.isEmpty()) {
            return;
        }

        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.map(line -> line.replaceAll("( )+", ""))
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !set.contains(line))
                    .map(line -> set.add(line))
                    .count(); // to trigger stream operation
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValid(String id) {
        if (currentTranscriptSet.isEmpty()) {
            return true;
        } else {
            return currentTranscriptSet.contains(id);
        }
    }

    public static boolean isCCDSTranscript(String id) {
        return ccdsTranscriptSet.contains(id);
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
}
