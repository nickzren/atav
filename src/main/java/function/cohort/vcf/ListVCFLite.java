package function.cohort.vcf;

import function.cohort.base.GenotypeLevelFilterCommand;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ListVCFLite {

    public static BufferedWriter bwVCF = null;
    public static final String vcfFilePath = CommonCommand.outputPath + "variants.vcf";
    public static final String vcfBGZFilePath = vcfFilePath + ".gz";

    public void initOutput() {
        try {
            bwVCF = new BufferedWriter(new FileWriter(vcfFilePath));
            bwVCF.write(VCFOutput.getHeader());
            bwVCF.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void closeOutput() {
        try {
            bwVCF.flush();
            bwVCF.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void run() {
        try {
            LogManager.writeAndPrint("Start running list vcf lite function");

            initOutput();

            File f = new File(GenotypeLevelFilterCommand.vcfFile);
            Reader decoder;
            if (f.getName().endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(f);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                decoder = new InputStreamReader(gzipStream);
            } else {
                decoder = new FileReader(f);
            }
            BufferedReader br = new BufferedReader(decoder);

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] values = lineStr.split("\t");
                VariantVCFLite variantLite = new VariantVCFLite(values);

                // output qualifed record to vcf file
                if (variantLite.isValid()) {
                    bwVCF.write(variantLite.toString());
                    bwVCF.newLine();
                }
            }

            br.close();
            decoder.close();

            closeOutput();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
}
