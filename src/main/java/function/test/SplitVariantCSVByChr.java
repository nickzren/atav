package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class SplitVariantCSVByChr {

    private static final String input = "/Users/nick/Desktop/TOPMed/bravo-dbsnp-all.csv";
    private static final String output = "/Users/nick/Desktop/TOPMed_split_by_chr";
    private static HashMap<String, BufferedWriter> bwMap = new HashMap();

    private static final String[] headers = {
        "CHROM",
        "POS",
        "REF",
        "ALT_1",
        "ALT_2",
        "ALT_3",
        "AF_1",
        "AF_2",
        "AF_3"
    };

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        String line = "";

        try {
            File file = new File(input);
            String fileName = file.getName();

            for (String chr : RegionManager.ALL_CHR) {
                String outputFileName = output + File.separator + chr + "_" + fileName;
                bwMap.put(chr, new BufferedWriter(new FileWriter(new File(outputFileName))));
            }

            Reader in = new FileReader(input);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(headers)
                    .withFirstRecordAsHeader()
                    .parse(in);

            for (CSVRecord record : records) {
                String chr = record.get("CHROM");
                String pos = record.get("POS");
                String ref = record.get("REF");
                String alt_1 = record.get("ALT_1");
                String alt_2 = record.get("ALT_2");
                String alt_3 = record.get("ALT_3");
                String af_1 = record.get("AF_1");
                String af_2 = record.get("AF_2");
                String af_3 = record.get("AF_3");

                output(chr, pos, ref, alt_1, af_1);
                output(chr, pos, ref, alt_2, af_2);
                output(chr, pos, ref, alt_3, af_3);
            }

            close();
        } catch (IOException ex) {
            System.out.println(line);
            ErrorManager.send(ex);
        }
    }

    private static void output(String chr, String pos, String ref, String alt, String af) throws IOException {
        if (alt.isEmpty()) {
            return;
        }

        StringJoiner sj = new StringJoiner("\t");
        sj.add(pos);
        sj.add(ref);
        sj.add(alt);
        sj.add(af);

        bwMap.get(chr).write(sj.toString());
        bwMap.get(chr).newLine();
    }

    private static void close() throws IOException {
        for (String chr : RegionManager.ALL_CHR) {
            bwMap.get(chr).flush();
            bwMap.get(chr).close();
        }
    }
}
