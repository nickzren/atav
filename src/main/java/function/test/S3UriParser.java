package function.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utils.CommonCommand;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class S3UriParser {

//    private static final String sourceDir = "/nfs/informatics/data/zr2180/rm_8108/";
    private static final String sourceDir = "/Users/nick/Desktop/rm_8108/";
    private static final String inputDir = sourceDir + "aws/";
    private static final String outputDir = CommonCommand.realOutputPath + File.separator;
//    private static final String outputDir = sourceDir + "aws/";

    private static final String WES_IN_ATAV = sourceDir + "wes_in_atav_102522.txt";
    private static final String WGS_IN_ATAV = sourceDir + "wgs_in_atav_102522.txt";
    private static final String CC_IN_ATAV = sourceDir + "cc_in_atav_102522.txt";
    private static HashSet<String> wesSet;
    private static HashSet<String> wgsSet;
    private static HashSet<String> ccSet;
    
    private static final String ALL_BAM = sourceDir + "all_bam_s3.txt";
    private static final String ALL_FASTQ = sourceDir + "all_fastq_s3.txt";

    // s3 bucket: igm-fastq-archive
    private static final String IN_igm_fastq_archive_FASTQ = inputDir + "igm-fastq-archive-FASTQ.csv";
    private static final String OUT_igm_fastq_archive_FASTQ = outputDir + "igm-fastq-archive-FASTQ.tsv";
    private static final String IN_igm_fastq_archive_BAM = inputDir + "igm-fastq-archive-BAM.csv";
    private static final String OUT_igm_fastq_archive_BAM = outputDir + "igm-fastq-archive-BAM.tsv";

    // s3 bucket: aws-fastq16
    private static final String IN_aws_fastq16_FASTQ = inputDir + "aws-fastq16-FASTQ.csv";
    private static final String OUT_aws_fastq16_FASTQ = outputDir + "aws-fastq16-FASTQ.tsv";

    // s3 bucket: seq-archive
    private static final String IN_seq_archive_FASTQ = inputDir + "seq-archive-FASTQ.csv";
    private static final String OUT_seq_archive_FASTQ = outputDir + "seq-archive-FASTQ.tsv";
    private static final String IN_seq_archive_BAM = inputDir + "seq-archive-BAM.csv";
    private static final String OUT_seq_archive_BAM = outputDir + "seq-archive-BAM.tsv";

    // s3 bucket: igm-projects-archive
    private static final String IN_igm_projects_archive_FASTQ = inputDir + "igm-projects-archive-FASTQ.csv";
    private static final String OUT_igm_projects_archive_FASTQ = outputDir + "igm-projects-archive-FASTQ.tsv";
    private static final String IN_igm_projects_archive_BAM = inputDir + "igm-projects-archive-BAM.csv";
    private static final String OUT_igm_projects_archive_BAM = outputDir + "igm-projects-archive-BAM.tsv";

    // s3 bucket: igm-annodb
    private static final String IN_igm_annodb_BAM = inputDir + "igm-annodb-BAM.csv";
    private static final String OUT_igm_annodb_BAM = outputDir + "igm-annodb-BAM.tsv";
    
    public static void main(String[] args) {
//        run();
        markDuplicate(ALL_BAM);
        markDuplicate(ALL_FASTQ);
    }
    
    public static void run() {
//        initSampleSet();
//
//        parse(IN_aws_fastq16_FASTQ, OUT_aws_fastq16_FASTQ);
//        parse(IN_igm_fastq_archive_FASTQ, OUT_igm_fastq_archive_FASTQ);
//        parse(IN_seq_archive_FASTQ, OUT_seq_archive_FASTQ);
//        parse(IN_igm_projects_archive_FASTQ, OUT_igm_projects_archive_FASTQ);
//
//        parse(IN_igm_fastq_archive_BAM, OUT_igm_fastq_archive_BAM);
//        parse(IN_seq_archive_BAM, OUT_seq_archive_BAM);
//        parse(IN_igm_projects_archive_BAM, OUT_igm_projects_archive_BAM);
//        parse(IN_igm_annodb_BAM, OUT_igm_annodb_BAM);

        markDuplicate(ALL_FASTQ);
        markDuplicate(ALL_FASTQ);
    }
    
    private static void initSampleSet() {
        try {
            wesSet = Files.readAllLines(Paths.get(WES_IN_ATAV))
                    .stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(HashSet::new));
            
            wgsSet = Files.readAllLines(Paths.get(WGS_IN_ATAV))
                    .stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(HashSet::new));
            
            ccSet = Files.readAllLines(Paths.get(CC_IN_ATAV))
                    .stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void parse(String input, String output) {
        String problemKey = "";
        try {
            LogManager.writeAndPrint("Start parsing: " + input);
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(output));
            
            Reader decoder = new FileReader(input);
            
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(new String[]{"bucket", "key", "size"})
                    .withFirstRecordAsHeader()
                    .parse(decoder);
            
            for (CSVRecord record : records) {
                StringJoiner sj = new StringJoiner("\t");
                
                String bucket = record.get("bucket");
                String key = record.get("key");
                
                if (bucket.equals("igm-projects-archive")
                        && (key.startsWith("SV/") || key.startsWith("upenn/"))) {
                    continue;
                }
                
                problemKey = key;
                String size = record.get("size");
                String e_tag = record.get("e_tag");
                
                String sampleName = "Unknown";
                String sampleType = "Unknown";
                String[] array = key.split("/");
                for (String str : array) {
                    String upperStr = str.toUpperCase();
                    if (upperStr.contains("EXOME")) {
                        sampleType = "WES";
                    } else if (upperStr.contains("GENOME")) {
                        sampleType = "WGS";
                    } else if (upperStr.contains("CUSTOM")) {
                        sampleType = "CC";
                    }
                    
                    if (sampleType.equals("WES")
                            && wesSet.contains(upperStr)) {
                        sampleName = str;
                        break;
                    } else if (sampleType.equals("WGS")
                            && wgsSet.contains(upperStr)) {
                        sampleName = str;
                        break;
                    } else if (sampleType.equals("CC")
                            && ccSet.contains(upperStr)) {
                        sampleName = str;
                        break;
                    }
                }
                
                if (sampleName.equals("Unknown")) {
                    String last = key.substring(key.lastIndexOf('/') + 1);
                    
                    if (key.contains("|")) {
                        last = key.substring(key.lastIndexOf('|') + 1);
                    }
                    
                    if (last.contains("_")) {
                        sampleName = last.substring(0, last.indexOf("_"));
                    } else if (last.contains(".")) {
                        sampleName = last.substring(0, last.indexOf("."));
                    }
                }
                
                if (sampleName.equals("accepted") || sampleName.equals("combined")) {
                    sampleName = "Unknown";
                }
                
                sj.add(sampleName);
                sj.add(sampleType);
                sj.add(bucket);
                sj.add(key);
                sj.add(size);
                sj.add(e_tag);
                
                bw.write(sj.toString());
                bw.newLine();
            }
            
            bw.flush();
            bw.close();
            
            LogManager.writeAndPrint("Complete parsing: " + input);
        } catch (Exception e) {
            LogManager.writeAndPrint("Problem key: " + problemKey);
            e.printStackTrace();
        }
    }
    
    public static void markDuplicate(String input) {
        try {
            LogManager.writeAndPrint("Start parsing: " + input);
            
            BufferedWriter bwKeep = new BufferedWriter(new FileWriter(input.replace(".txt", "_keep.txt")));
            BufferedWriter bwDelete = new BufferedWriter(new FileWriter(input.replace(".txt", "_delete.txt")));
            
            Reader decoder = new FileReader(input);
            
            Iterable<CSVRecord> records = CSVFormat.TDF
                    .withHeader(new String[]{"sample_internal_name", "e_tag", "size", "bucket", "key"})
                    .withFirstRecordAsHeader()
                    .parse(decoder);
            
            int totalKeepCount = 0;
            int totalDeleteCount = 0;
            
            long totalSizeToDelete = 0;
            
            HashSet<String> etagSet = new HashSet<>();
            HashSet<String> sizeSet = new HashSet<>();
            HashSet<String> fileNameSet = new HashSet<>();
            HashSet<String> sampleSet = new HashSet<>();
            
            for (CSVRecord record : records) {
                String sample_internal_name = record.get("sample_internal_name");
                String e_tag = record.get("e_tag");
                String size = record.get("size");
                String bucket = record.get("bucket");
                String key = record.get("key");
                String fileName = key.substring(key.lastIndexOf("/") + 1);
                
                StringJoiner sj = new StringJoiner("\t");
                sj.add(e_tag);
                sj.add(size);
                sj.add(bucket);
                sj.add(key);
                
                if (etagSet.contains(e_tag)
                        || (sizeSet.contains(size)
                        && (fileNameSet.contains(fileName) || sampleSet.contains(sample_internal_name)))) {
                    bwDelete.write(sj.toString());
                    bwDelete.newLine();
                    totalDeleteCount++;
                    
                    totalSizeToDelete += Long.parseLong(size);
                } else {
                    bwKeep.write(sj.toString());
                    bwKeep.newLine();
                    totalKeepCount++;
                }
                
                sampleSet.add(sample_internal_name);
                etagSet.add(e_tag);
                sizeSet.add(size);
                fileNameSet.add(fileName);
            }
            
            bwKeep.flush();
            bwKeep.close();
            bwDelete.flush();
            bwDelete.close();
            
            LogManager.writeAndPrint("Total number of files to keep: " + totalKeepCount);
            LogManager.writeAndPrint("Total number of files to delete: " + totalDeleteCount);
            LogManager.writeAndPrint("Total file size to delete: " + totalSizeToDelete);
            
            LogManager.writeAndPrint("Complete parsing: " + input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void reorderDuplicate(String input) {
        try {
            LogManager.writeAndPrint("Start parsing: " + input);
            
            BufferedWriter bwReorder = new BufferedWriter(new FileWriter(input.replace(".txt", "_reorder.txt")));
            
            Reader decoder = new FileReader(input);
            
            Iterable<CSVRecord> records = CSVFormat.TDF
                    .withHeader(new String[]{"e_tag", "size", "bucket", "key"})
                    .withFirstRecordAsHeader()
                    .parse(decoder);
            
            String previousETAG = "";
            
            List<String> etagList = new ArrayList<>();
            HashMap<String, ArrayList<S3Uri>> etagMap = new HashMap<>();
            
            for (CSVRecord record : records) {
                String e_tag = record.get("e_tag");
                String size = record.get("size");
                String bucket = record.get("bucket");
                String key = record.get("key");
                
                if (!etagList.contains(e_tag)) {
                    etagList.add(e_tag);
                }
                
                S3Uri s3Uri = new S3Uri(e_tag, size, bucket, key);
                
                ArrayList<S3Uri> list = etagMap.getOrDefault(e_tag, new ArrayList<>());
                
                if (list.isEmpty()) {
                    etagMap.put(e_tag, list);
                }
                
                list.add(s3Uri);
            }
            
            for (String etag : etagList) {
                ArrayList<S3Uri> list = etagMap.get(etag);
                
                while (!list.isEmpty()) {
                    
                }
            }
            
            bwReorder.flush();
            bwReorder.close();
            
            LogManager.writeAndPrint("Complete parsing: " + input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
