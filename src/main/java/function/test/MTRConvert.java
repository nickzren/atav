package function.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.StringJoiner;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class MTRConvert {

    public static void run() throws Exception {
        String input = "/nfs/goldstein/datasets/MTR/mtrflatfile_2.0.txt";
        String output = "/nfs/goldstein/datasets/MTR/mtrflatfile_2.0_filtered.txt";
        
        BufferedReader br = new BufferedReader(new FileReader(new File(input)));
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        
        String lineStr = "";
        String processedChr = "";
        HashSet<String> idSet = new HashSet<>();
        boolean isHeader = true;

        while ((lineStr = br.readLine()) != null) {
            try {
                String[] tmp = lineStr.split("\t");
                
                if (isHeader) {
                    isHeader = false;
                } else if (tmp.length != 13) {
                    continue;
                }
                
                String chr = tmp[0];
                String id = chr + "-" + tmp[1];
                
                if(!processedChr.equals(chr)) {
                    LogManager.writeAndPrint("Reach to chr: " + chr);
                    idSet.clear();
                }
                
                if (!idSet.contains(id)) {
                    StringJoiner sj = new StringJoiner("\t");
                    sj.add(tmp[0]); // Chromosome_name
                    sj.add(tmp[1]); // Genomic_position
                    sj.add(tmp[10]); // MTR
                    sj.add(tmp[11]); // FDR
                    sj.add(tmp[12]); // MTR_centile

                    bw.write(sj.toString());
                    bw.newLine();
                    
                    idSet.add(id);
                }

                processedChr = chr;
            } catch (Exception e) {
                LogManager.writeAndPrint(lineStr);
                System.exit(1);
            }
        }

        bw.flush();
        bw.close();
    }
}
