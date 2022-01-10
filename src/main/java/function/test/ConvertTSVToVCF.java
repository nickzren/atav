package function.test;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nick
 */
public class ConvertTSVToVCF {

    public static void run() {
        String chr = "21";

        String tsv = "/nfs/goldstein/datasets/TraP/v3/raw/" + chr + ".tsv.gz";
        String vcf = "/home/zr2180/trap/" + chr + ".vcf";

        String lineStr = "";
        try {
            File f = new File(tsv);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            BufferedWriter bw = new BufferedWriter(new FileWriter(vcf));

            StringBuilder sb = new StringBuilder();
            sb.append("##fileformat=VCFv4.2\n");
            sb.append("##INFO=<ID=ENSG,Number=.,Type=String,Description=\"ENSG ID\">\n");
            sb.append("##INFO=<ID=TraP,Number=.,Type=String,Description=\"TraP score\">\n");
            StringJoiner sj = new StringJoiner("\t");
            sj.add("#CHROM");
            sj.add("POS");
            sj.add("ID");
            sj.add("REF");
            sj.add("ALT");
            sj.add("QUAL");
            sj.add("FILTER");
            sj.add("INFO");
            sb.append(sj.toString());
            bw.write(sb.toString());
            bw.newLine();

            HashSet<String> set = new HashSet<>();

            while ((lineStr = br.readLine()) != null) {
                String[] tmp = lineStr.split("\t");
                String pos = tmp[0];
                String ref = tmp[1];
                String alt = tmp[2];
                String ensg = tmp[3];
                String trap = tmp[4];

                sj = new StringJoiner("\t");
                sj.add(chr);
                sj.add(pos);
                sj.add(".");
                sj.add(ref);
                sj.add(alt);
                sj.add(".");
                sj.add(".");
                sj.add("ENSG=" + ensg + ";" + "TraP=" + trap);

                bw.write(sj.toString());
                bw.newLine();
            }

            HashMap<Integer, Integer> map = new HashMap<>();

            br.close();
            decoder.close();
            in.close();
            bw.flush();
            bw.close();
        } catch (Exception e) {
            System.out.println(lineStr);
            e.printStackTrace();
        }
    }
}
