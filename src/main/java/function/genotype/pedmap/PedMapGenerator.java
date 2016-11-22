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

    final String pedFile = CommonCommand.outputPath + "output.ped";
    final String mapFile = CommonCommand.outputPath + "output.map";
    final String tmpPedFile = CommonCommand.outputPath + "output_tmp.ped";

    int qualifiedVariants = 0;
    
    // --eigenstrat
    private static final String RUN_EIGENSTRAT_PATH = "/nfs/goldstein/software/atav_home/lib/run_eigenstrat.py";

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
        String cmd = ThirdPartyToolManager.PYTHON
                + " " + RUN_EIGENSTRAT_PATH
                + " --genotypefile " + pedFile
                + " --snpfile " + mapFile
                + " --indivfile " + pedFile
                + " --outlieroutname eigenstrat_outlier_removed.txt "
                + " --evecoutname eigenstrat_outlier_removed.evec "
                + " --evaloutname eigenstrat_outlier_removed.eval "
                + " --numoutevec 10 "
                + " --numoutlieriter 5 "
                + " --logfile eigenstrat_outlier_removed.log "
                + " --outputdir " + CommonCommand.realOutputPath;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
