package function.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ConvertIranome {
    
    public static void main(String[] args) throws Exception {
        run();
    }

    public static void run() throws Exception {
        // header - Chr:Pos	Ref/Alt	Identifier	Allele Counts	Allele Frequencies	# Alleles	# Het	# HomoVar
        String input = "/Users/nick/Desktop/Iranome/Iranome_Variants_Frequency_b37.txt";
        String output = "/Users/nick/Desktop/Iranome/Iranome_Variants_Frequency_b37.csv";

        BufferedReader br = new BufferedReader(new FileReader(new File(input)));
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));

        String lineStr = "";

        while ((lineStr = br.readLine()) != null) {
            String[] tmp = lineStr.split("\t");
            
            String chrPos = tmp[0];
            String refAlt = tmp[1];
            
            if (refAlt.contains("-")) {
                continue;
            }
            
            String af = tmp[4];
            
            tmp = chrPos.split(":");
            String chr = tmp[0];
            String pos = tmp[1];
            
            tmp = refAlt.split("/");
            
            String ref = tmp[0];
            String alt = tmp[1];
            
            String variantId = chr + "-" + pos + "-" + ref + "-" + alt;
            
            bw.write(variantId);
            bw.write(",");
            bw.write(af);
            bw.newLine();            
        }

        bw.flush();
        bw.close();
    }
}
