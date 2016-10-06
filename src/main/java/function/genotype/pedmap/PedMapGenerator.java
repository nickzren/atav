package function.genotype.pedmap;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.variant.base.Output;
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

    private static final String PLINK_HOME = "/nfs/goldstein/goldsteinlab/software/sh/plink";
    private static final String CHIP2PCA2_HOME = "/nfs/goldstein/goldsteinlab/software/sh/chip2pca2";
    private static final String EXAMPLE_OPT_PATH = "lib/example.opt";

    final String pedFile = CommonCommand.outputPath + "output.ped";
    final String mapFile = CommonCommand.outputPath + "output.map";
    final String tmpPedFile = CommonCommand.outputPath + "output_tmp.ped";

    final String chip2pcaDir = CommonCommand.realOutputPath
            + File.separator + "chip2pca";
    final String crDir = chip2pcaDir + File.separator
            + CommonCommand.outputDirName + "-cr";

    final String outputOpt = chip2pcaDir + File.separator
            + CommonCommand.outputDirName + ".opt";

    int qualifiedVariants = 0;

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
        } else if (PedMapCommand.isEigenstratFixed) {
            doEigesntratFixed();
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
            Output output = new Output(calledVar);
            output.countSampleGeno();
            output.calculate();

            if (output.isValid()) {
                doOutput(calledVar);
            }
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

            long rowLen = 2 * SampleManager.getListSize() + 1L;

            for (int s = 0; s < SampleManager.getListSize(); s++) {
                Sample sample = SampleManager.getList().get(s);

                String name = sample.getName();

                if (PedMapCommand.isEigenstrat) {
                    name = String.valueOf(sample.getPrepId());
                }

                int pheno = (int) sample.getPheno() + 1;

                bwPed.write(sample.getFamilyId() + " "
                        + name + " "
                        + sample.getPaternalId() + " "
                        + sample.getMaternalId() + " "
                        + sample.getSex() + " "
                        + pheno);

                for (int i = 0; i < qualifiedVariants; i++) {
                    for (int j = 0; j < 2; j++) {
                        long pos = i * rowLen + 2 * s + j;
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
            int geno = calledVar.getGenotype(sample.getIndex());
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
                case Data.NA:
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
        initDir(chip2pcaDir);

        File dir = initDir(crDir);

        String cmd = "cp " + EXAMPLE_OPT_PATH + " " + outputOpt;

        ThirdPartyToolManager.systemCall(new String[]{cmd});

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --recode12 "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --make-bed "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = "cd " + chip2pcaDir + "; "
                + CHIP2PCA2_HOME + " " + CommonCommand.outputDirName + " snppca";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    public void doEigesntratFixed() {
        initDir(chip2pcaDir);

        File dir = initDir(crDir);

        String cmd = "cp " + EXAMPLE_OPT_PATH + " " + outputOpt;

        ThirdPartyToolManager.systemCall(new String[]{cmd});

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --recode12 "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = PLINK_HOME + " --file " + CommonCommand.outputPath + "output --make-bed "
                + "--out " + crDir + File.separator + dir.getName();

        ThirdPartyToolManager.systemCall((new String[]{cmd}));

        cmd = "cd " + chip2pcaDir + "; "
                + CHIP2PCA2_HOME + " " + CommonCommand.outputDirName + " snppca.phenotype_fixed.pl";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    private File initDir(String path) {
        File dir = new File(path);

        if (dir.exists()) {
            purgeDirectory(dir);
        } else {
            dir.mkdir();
        }

        return dir;
    }

    private void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                purgeDirectory(file);
            }

            file.delete();
        }
    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
