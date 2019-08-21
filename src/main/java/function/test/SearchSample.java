package function.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author nick
 */
public class SearchSample {

    public static void run() {
        HashSet<String> sampleSet = new HashSet<String>();

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get("/nfs/seqscratch09/exome1.7samples.txt"))) {
            list = stream
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        File dir = new File("log/sample");
        File[] directoryListing = dir.listFiles();

        for (String sample : list) {
            for (File file : directoryListing) {
                if (file.getName().contains("etc2131")) {

                    if (isContainedInFile(sample, file)) {
                        sampleSet.add(sample);
                        break;
                    }
                }
            }
        }

        for (String sample : sampleSet) {
            System.out.println(sample);
        }
    }

    private static boolean isContainedInFile(String sample, File file) {
        boolean isValid = false;

        try (Stream<String> stream = Files.lines(file.toPath())) {
            isValid = stream.map(line -> line.split("\t"))
                    .filter(line -> line.length == 8)
                    .anyMatch(line -> line[1].equals(sample) && !line[7].equals("Roche"));

        } catch (IOException e) {
            System.out.println(file.getAbsolutePath());
            e.printStackTrace();
        }

        return isValid;
    }
}
