package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nick
 */
public class ConvertVCFToTSV {

    public static void run() {
        for (String chr : RegionManager.ALL_CHR) {
            String vcf = "/nfs/informatics/data/zr2180/trap_hg38/hg38_vcf/" + chr + ".hg38.vcf";
            String tsv = "/nfs/informatics/data/zr2180/trap_hg38/hg38_tsv/" + chr + ".hg38.tsv";

            String lineStr = "";
            try {
                File f = new File(vcf);
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);

                BufferedWriter bw = new BufferedWriter(new FileWriter(tsv));

//                StringJoiner sj = new StringJoiner("\t");
//                sj.add("#CHROM"); 0 
//                sj.add("POS"); 1
//                sj.add("ID"); 2
//                sj.add("REF"); 3
//                sj.add("ALT"); 4
//                sj.add("QUAL"); 5
//                sj.add("FILTER"); 6
//                sj.add("INFO"); 7

                while ((lineStr = br.readLine()) != null) {
                    if(lineStr.startsWith("#")) {
                        continue;
                    }
                    
                    String[] tmp = lineStr.split("\t");
                    String pos = tmp[1];
                    String ref = tmp[3];
                    String alt = tmp[4];
                    String[] info = tmp[7].split(";");
                    String ensg = info[0].split("=")[1];
                    String trap = info[1].split("=")[1];

                    StringJoiner sj = new StringJoiner("\t");
                    sj.add(pos);
                    sj.add(ref);
                    sj.add(alt);
                    sj.add(ensg);
                    sj.add(trap);

                    bw.write(sj.toString());
                    bw.newLine();
                }

                br.close();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                System.out.println(lineStr);
                e.printStackTrace();
            }
        }
    }
}
