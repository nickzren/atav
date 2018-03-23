package function.genotype.pedmap;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;

/**
 *
 * @author nick
 */
public class PedMapGenerator extends AnalysisBase4CalledVar {

    BufferedWriter bwPed = null;
    BufferedWriter bwMap = null;
    BufferedWriter bwTmpPed = null;

    final String pedFile = CommonCommand.outputPath + "output.ped";
    final String mapFile = CommonCommand.outputPath + "output.map";
    final String tmpPedFile = CommonCommand.outputPath + "output_tmp.ped";

    int qualifiedVariants = 0;

    // --eigenstrat
    private static final String EIGENSTRAT_SCRIPT_PATH = Data.ATAV_HOME + "lib/run_eigenstrat.py";

    // --kinship
    private static final String KINSHIP_SCRIPT_PATH = Data.ATAV_HOME + "lib/run_kinship.py";

    @Override
    public void initOutput() {
        try {
            bwPed = new BufferedWriter(new FileWriter(pedFile));
            bwMap = new BufferedWriter(new FileWriter(mapFile));
            bwTmpPed = new BufferedWriter(new FileWriter(tmpPedFile));
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
    }

    @Override
    public void closeOutput() {
        try {
            bwPed.flush();
            bwPed.close();
            bwMap.flush();
            bwMap.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (PedMapCommand.isEigenstrat) {
            doEigesntrat();
        }

        if (PedMapCommand.isKinship) {
            doKinship();
        }
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
        generatePedFile();
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            doOutput(calledVar);
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void doOutput(CalledVariant calledVar) {
        try {
            qualifiedVariants++;

            bwMap.write(calledVar.getChrStr() + "\t"
                    + calledVar.getVariantIdStr() + "\t"
                    + "0\t"
                    + calledVar.getStartPosition());
            bwMap.newLine();

            outputTempGeno(calledVar);
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void generatePedFile() {
        try {
            LogManager.writeAndPrint("Output the data to ped file now");

            bwTmpPed.flush();
            bwTmpPed.close();
            File tmpFile = new File(tmpPedFile);
            RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");

            long rowLen = 2 * SampleManager.getTotalSampleNum() + 1L;

            for (Sample sample : SampleManager.getList()) {
                String name = sample.getName();

                byte pheno = (byte) (sample.getPheno() + 1);

                bwPed.write(sample.getFamilyId() + " "
                        + name + " "
                        + sample.getPaternalId() + " "
                        + sample.getMaternalId() + " "
                        + sample.getSex() + " "
                        + pheno);

                for (int i = 0; i < qualifiedVariants; i++) {
                    for (int j = 0; j < 2; j++) {
                        long pos = i * rowLen + 2 * sample.getIndex() + j;
                        raf.seek(pos);
                        byte allele = raf.readByte();
                        bwPed.write(" " + String.valueOf((char) allele));
                    }
                }

                bwPed.newLine();
            }

            tmpFile.delete();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    private void outputTempGeno(CalledVariant calledVar) throws Exception {
        for (Sample sample : SampleManager.getList()) {
            byte geno = calledVar.getGT(sample.getIndex());
            switch (geno) {
                case Index.HOM:
                    if (calledVar.isSnv()) {
                        bwTmpPed.write(calledVar.getAllele() + calledVar.getAllele());
                    } else if (calledVar.isDel()) {
                        bwTmpPed.write("DD");
                    } else {
                        bwTmpPed.write("II");
                    }
                    break;
                case Index.HET:
                    if (calledVar.isSnv()) {
                        bwTmpPed.write(calledVar.getRefAllele() + calledVar.getAllele());
                    } else {
                        bwTmpPed.write("ID");
                    }
                    break;
                case Index.REF:
                    if (calledVar.isSnv()) {
                        bwTmpPed.write(calledVar.getRefAllele() + calledVar.getRefAllele());
                    } else if (calledVar.isDel()) {
                        bwTmpPed.write("II");
                    } else {
                        bwTmpPed.write("DD");
                    }
                    break;
                case Data.BYTE_NA:
                    bwTmpPed.write("00");
                    break;
                default:
                    bwTmpPed.write("00");
                    LogManager.writeAndPrint("Invalid genotype: " + geno
                            + " (Variant ID: " + calledVar.getVariantIdStr() + ")");
                    break;
            }
        }

        bwTmpPed.newLine();
    }

    public void doEigesntrat() {
        String cmd = ThirdPartyToolManager.PYTHON
                + " " + EIGENSTRAT_SCRIPT_PATH
                + " --sample " + GenotypeLevelFilterCommand.sampleFile
                + " --prune-sample"
                + " --genotypefile " + pedFile
                + " --snpfile " + mapFile
                + " --indivfile " + pedFile
                + " --numoutevec 10"
                + " --numoutlieriter 5"
                + " --outputdir " + CommonCommand.realOutputPath;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    public void doKinship() {
        // Convert PED & MAP to BED format with PLINK
        String cmd = ThirdPartyToolManager.PLINK
                + " --file " + CommonCommand.outputPath + "output"
                + " --make-bed"
                + " --out " + CommonCommand.outputPath + "plink";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        // Run KING to get kinship
        cmd = ThirdPartyToolManager.KING
                + " -b " + CommonCommand.outputPath + "plink.bed"
                + " --kinship --related --degree 3"
                + " --prefix " + CommonCommand.outputPath + "king";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        // Run kinship pruning script
        cmd = ThirdPartyToolManager.PYTHON
                + " " + KINSHIP_SCRIPT_PATH
                + " " + GenotypeLevelFilterCommand.sampleFile
                + " " + CommonCommand.outputPath + "king.kin0"
                + " --seed " + PedMapCommand.seed
                + " --output " + CommonCommand.outputPath + "kinship_pruned_sample.txt"
                + " --verbose";

        if (!PedMapCommand.sampleCoverageSummaryPath.isEmpty()) {
            cmd += " --sample_coverage_summary " + PedMapCommand.sampleCoverageSummaryPath;
        }

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
