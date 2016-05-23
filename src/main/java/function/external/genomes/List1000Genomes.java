package function.external.genomes;

import function.AnalysisBase;
import function.variant.base.VariantManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.CommonCommand;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class List1000Genomes extends AnalysisBase {

    BufferedWriter bw1000Genomes = null;
    final String genomesFilePath = CommonCommand.outputPath + "1000_genomes.csv";

    int analyzedRecords = 0;

    @Override
    public void initOutput() {
        try {
            bw1000Genomes = new BufferedWriter(new FileWriter(genomesFilePath));
            bw1000Genomes.write(GenomesOutput.title);
            bw1000Genomes.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bw1000Genomes.flush();
            bw1000Genomes.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {
        try {
            for (String variantId : VariantManager.getIncludeVariantList()) {
                GenomesOutput output = new GenomesOutput(variantId);

                if (output.isValid()) {
                    bw1000Genomes.write(variantId + ",");
                    bw1000Genomes.write(output.toString());
                    bw1000Genomes.newLine();
                }

                countVariant();
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    protected void countVariant() {
        analyzedRecords++;
        System.out.print("Processing variant "
                + analyzedRecords + "                     \r");
    }

    @Override
    public String toString() {
        return "It is running list 1000 genomes function...\n\n"
                + "snv table: " + GenomesManager.snvTable + "\n\n"
                + "indel table: " + GenomesManager.indelTable;
    }
}
