package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author nick
 */
public class ConvertDBNSFP {

    private static String[] headers = {
        "hg19_pos(1-based)",
        "alt",
        "Ensembl_transcriptid",
        "SIFT_pred",
        "Polyphen2_HDIV_pred",
        "Polyphen2_HVAR_pred",
        "LRT_pred",
        "MutationTaster_pred"
    };

    private static String SQ_NULL = "\\N";
    private static int nullCount = 0;
    private static final int MAX_NULL_COUNT = 5;
    static HashSet<String> set = new HashSet<>();

    private static int count = 0;

    public static void main(String[] args) throws Exception {
        run();
    }

    public static void run() throws Exception {
        for (String chr : RegionManager.ALL_CHR) {
            Iterable<CSVRecord> records = getRecords(TestCommand.testInput + "/dbNSFP4.1a_variant_subset_hg19.chr" + chr + ".tsv.gz");

            BufferedWriter bw = new BufferedWriter(new FileWriter(TestCommand.testOutput + "/dbNSFP4.1a_variant_subset_hg19_db_load.chr" + chr + ".tsv"));

            String prevID = "";
            ArrayList<CSVRecord> list = new ArrayList<>();

            for (CSVRecord record : records) {
                String pos = getSQNull(record.get("hg19_pos(1-based)"));
                String alt = getSQNull(record.get("alt"));
                String id = pos + alt;

                if (prevID.isEmpty()) {
                    prevID = id;
                    list.add(record);
                    continue;
                }

                if (!prevID.equals(id)) {
                    // process previous variant records
                    processVariantByRecords(list, bw);

                    prevID = id;
                    list.clear();
                }

                list.add(record);
            }

            // process the last record
            processVariantByRecords(list, bw);

            bw.flush();
            bw.close();
        }
    }

    private static void processVariantByRecords(ArrayList<CSVRecord> list, BufferedWriter bw) throws IOException {
        nullCount = 0;

        StringJoiner lineSJ = new StringJoiner("\t");
        String pos = getSQNull(list.get(0).get("hg19_pos(1-based)"));
        if (pos.equals(SQ_NULL)) {
            return;
        }
        lineSJ.add(pos);

        String alt = getSQNull(list.get(0).get("alt"));
        if (alt.equals(SQ_NULL)) {
            return;
        }
        lineSJ.add(alt);

        StringJoiner transcriptSJ = new StringJoiner(";");
        StringJoiner siftPredSJ = new StringJoiner(";");
        StringJoiner polyphenHDIVSJ = new StringJoiner(";");
        StringJoiner polyphenHVARSJ = new StringJoiner(";");
        StringJoiner lrtPredSJ = new StringJoiner(";");
        StringJoiner mutationTasterPredSJ = new StringJoiner(";");

        for (CSVRecord record : list) {
            transcriptSJ.add(record.get("Ensembl_transcriptid"));
            siftPredSJ.add(record.get("SIFT_pred"));
            polyphenHDIVSJ.add(record.get("Polyphen2_HDIV_pred"));
            polyphenHVARSJ.add(record.get("Polyphen2_HVAR_pred"));
            lrtPredSJ.add(record.get("LRT_pred"));
            mutationTasterPredSJ.add(record.get("MutationTaster_pred"));
        }

        String transcript = transcriptSJ.toString().replaceAll("ENST", "");
        transcript = removeFrontZero(transcript);
        int transcriptNum = transcript.split(";").length;
        lineSJ.add(getSQNull(transcript));

        lineSJ.add(getSQNull(siftPredSJ.toString()));
        lineSJ.add(getSQNull(polyphenHDIVSJ.toString()));
        lineSJ.add(getSQNull(polyphenHVARSJ.toString()));

        String lrtPred = getSQNull(lrtPredSJ.toString());
        int lrtPredNum = lrtPred.split(";").length;
        if (transcriptNum != lrtPredNum && !lrtPred.equals(SQ_NULL)) {
            lrtPred = getMostDamagingValue(lrtPred);
        }
        lineSJ.add(lrtPred);

        String mutationTasterPred = getSQNull(mutationTasterPredSJ.toString());
        int mutationTasterPredNum = mutationTasterPred.split(";").length;
        if (transcriptNum != mutationTasterPredNum && !mutationTasterPred.equals(SQ_NULL)) {
            mutationTasterPred = getMostDamagingValue(mutationTasterPred);
        }
        lineSJ.add(mutationTasterPred);

        if (nullCount == MAX_NULL_COUNT) {
            return;
        }

        bw.write(lineSJ.toString());
        bw.newLine();
    }

    private static String getMostDamagingValue(String value) {
        // pick the most damaging one: A to Z only
        int min = Integer.MAX_VALUE;
        for (String s : value.split(";")) {
            if (s.equals(".")) {
                continue;
            }

            min = Math.min(min, s.charAt(0));
        }

        return getSQNull(Character.toString((char) min));
    }

    private static String removeFrontZero(String transcript) {
        StringJoiner sj = new StringJoiner(";");
        for (String t : transcript.split(";")) {
            if (t.equals(".")) {
                continue;
            }

            sj.add(Integer.valueOf(t).toString());
        }

        return sj.toString();
    }

    private static String getSQNull(String value) {
        if (value.replaceAll("\\.", "").replaceAll(";", "").isEmpty()) {
            nullCount++;
            return SQ_NULL;
        }

        return value;
    }

    public static Iterable<CSVRecord> getRecords(String filename) throws FileNotFoundException, IOException {
        Reader decoder;
        
        if (filename.endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(filename);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            decoder = new InputStreamReader(gzipStream);
        } else {
            decoder = new FileReader(filename);
        }

        Iterable<CSVRecord> records = CSVFormat.TDF
                .withHeader(headers)
                .withFirstRecordAsHeader()
                .parse(decoder);

        return records;
    }
}
