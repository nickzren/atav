package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author nick
 */
public class ConvertTSVToVCF {

    public static void run() {
        for (String chr : RegionManager.ALL_CHR) {
            String tsv = "/nfs/goldstein/datasets/TraP/v3/raw/" + chr + ".tsv.gz";
            String vcf = "/nfs/informatics/data/zr2180/trap_hg38/" + chr + ".vcf";

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

                // MT failed to perform liftover
                if (chr.equals("MT")) {
                    chr = "M";
                }

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
}
