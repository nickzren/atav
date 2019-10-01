package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class TraPGeneMapping {
    
    private static final String inputDir = "/nfs/goldsteindata/zr14/data/trap_v3/";
    private static final String outputDir = "/nfs/goldstein/datasets/TraP/v3/";
    private static final File ENSG_HGNC_FILE = new File(inputDir + "ensg_hgnc_gene.txt");
    
    private static HashMap<String, BufferedReader> brMap = new HashMap();
    private static HashMap<String, BufferedWriter> bwMap = new HashMap();
    private static HashMap<String, String> ensgHGNCMap = new HashMap();
    
    private static final String TSV = ".tsv";
    
    public static void init() throws Exception {
        for (String chr : RegionManager.ALL_CHR) {
            brMap.put(chr, new BufferedReader(new FileReader(new File(inputDir + chr + TSV))));
        }
        
        for (String chr : RegionManager.ALL_CHR) {
            bwMap.put(chr, new BufferedWriter(new FileWriter(new File(outputDir + chr + TSV))));
        }
        
        Stream<String> stream = Files.lines(Paths.get(ENSG_HGNC_FILE.getAbsolutePath()));
        
        ensgHGNCMap = (HashMap<String, String>) stream
                .map(elem -> elem.split("\t"))
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    }
    
    public static void run() {
        try {
            init();
            
            for (String chr : RegionManager.ALL_CHR) {
                LogManager.writeAndPrint("Start processing " + chr + "...");
                
                String lineStr = "";
                
                BufferedReader br = brMap.get(chr);
                BufferedWriter bw = bwMap.get(chr);
                
                while ((lineStr = br.readLine()) != null) {
                    String[] tmp = lineStr.split("\t");
                    
                    StringJoiner sj = new StringJoiner("\t");
                    sj.add(tmp[0]);
                    sj.add(tmp[2]);
                    sj.add(ensgHGNCMap.get(tmp[3]));
                    sj.add(tmp[4]);
                    
                    bw.write(sj.toString());
                    bw.newLine();
                }
                
                bw.flush();
                bw.close();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
}
