package function.genotype.pedmap;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.CohortLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;
import global.Index;
import utils.CommonCommand;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.stream.Collectors;
import utils.ErrorManager;

/**
 *
 * @author nick, macrina
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

    // flashpca
    Map<String, SamplePCAInfo> sampleMap;

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
        if (PedMapCommand.isFlashPCA) {
            doFlashPCA();
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
            // ignore MNV
            if (calledVar.isMNV()) {
                return;
            }
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
                byte pheno = (byte) (sample.getPheno() + 1);
                bwPed.write(sample.getFamilyId() + " "
                        + sample.getName() + " "
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
                + " --sample " + CohortLevelFilterCommand.sampleFile
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
        runPlinkPedToBed("output", "plink", "");

        // Run KING to get kinship
        String cmd = ThirdPartyToolManager.KING
                + " -b " + CommonCommand.outputPath + "plink.bed"
                + " --kinship"
                + " --related"
                + " --degree 3"
                + " --prefix " + CommonCommand.outputPath + "king";
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        // Run kinship pruning script
        cmd = ThirdPartyToolManager.PYTHON
                + " " + KINSHIP_SCRIPT_PATH
                + " " + CohortLevelFilterCommand.sampleFile
                + " " + CommonCommand.outputPath + "king.kin0"
                + " " + CommonCommand.outputPath + "king.kin"
                + " --relatedness_threshold " + PedMapCommand.kinshipRelatednessThreshold
                + " --seed " + PedMapCommand.kinshipSeed
                + " --output " + CommonCommand.outputPath + "kinship_pruned_sample.txt"
                + " --verbose";
        if (!PedMapCommand.sampleCoverageSummaryPath.isEmpty()) {
            cmd += " --sample_coverage_summary " + PedMapCommand.sampleCoverageSummaryPath;
        }
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    public void doFlashPCA() {
        runPlinkPedToBed("output", "plink", "");

        String outExt = "_flashpca";
        FlashPCAManager.runFlashPCA("plink", outExt, "flashpca.log");
        FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + "eigenvalues" + outExt,
                CommonCommand.outputPath + "plot_eigenvalues_flashpca.pdf",
                "eigenvalues",
                "plot of eigenvalues",
                "eigenvalue number",
                "eigenvalue");

        FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + "pve" + outExt,
                CommonCommand.outputPath + "plot_pve_flashpca.pdf",
                "percent variance",
                "percent_variance explained by eigval",
                "eigenvalue number",
                "per_var_explained");

        int nDim = 3;
        if (PedMapCommand.flashPCANumEvec < 3) {
            LogManager.writeAndPrint("Number of dimensions to plot can't be greater than total number of eigenvectors");
            nDim = PedMapCommand.flashPCANumEvec;
        }
        ArrayList<Sample> sampleList = SampleManager.getList();
        sampleMap = FlashPCAManager.getSampleMap(nDim,
                CommonCommand.outputPath + "eigenvectors" + outExt,
                CommonCommand.outputPath + "pcs" + outExt,
                sampleList);

        sampleMap.entrySet().removeIf(entry -> (entry.getValue().getToFilter()));
        
        FlashPCAManager.plot2DData(sampleMap, nDim, false, CommonCommand.outputPath + "plot_eigenvectors_flashpca.pdf");

        LogManager.writeAndPrint("Finding outliers using plink ibs clustering");

        if (!PedMapCommand.isFlashPCAKeepOutliers) {
            FlashPCAManager.findOutliers();

            //read each line of outlier nearest file and filter based on Z-score
            HashSet<String> outlierSet = FlashPCAManager.getOutliers(
                    CommonCommand.outputPath + "plink_outlier.nearest",
                    CommonCommand.outputPath + "outlier_file.txt");

            LogManager.writeAndPrint("Redo flashpca with outliers removed");

            String remove_cmd = " --remove " + CommonCommand.outputPath + "outlier_file.txt";

            sampleMap.entrySet().stream().forEach(e -> e.getValue().setOutlier(outlierSet));
            FlashPCAManager.plot2DData(sampleMap,
                    nDim, true,
                    CommonCommand.outputPath + "plot_eigenvectors_flashpca_color_outliers.pdf");//cases,controls.outliers - 3 colors

            runPlinkPedToBed("output", "plink_outlier_removed", remove_cmd);
            outExt = "_flashpca_outliers_removed";
            FlashPCAManager.runFlashPCA("plink_outlier_removed", outExt, "flashpca.log");

            //making plots with / without outlier
            FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + "eigenvalues" + outExt,
                    CommonCommand.outputPath + "eigenvalues_flashpca_outliers_removed.pdf",
                    "eigenvalues no outliers", "plot of eigenvalues",
                    "eigenvalue number",
                    "eigenvalue");

            FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + "pve" + outExt,
                    CommonCommand.outputPath + "pve_flashpca_outliers_removed.pdf",
                    "percent variance no outliers",
                    "percent variance explained by eigval",
                    "eigenvalue number",
                    "per_var_explained");

            sampleList = sampleMap.values().stream()
                    .filter(s -> !s.isOutlier())
                    .map(SamplePCAInfo::getSample)
                    .collect(Collectors.toCollection(ArrayList::new));
            
            sampleMap = FlashPCAManager.getSampleMap(nDim,
                    CommonCommand.outputPath + "eigenvectors" + outExt,
                    CommonCommand.outputPath + "pcs" + outExt,
                    sampleList);
            
            FlashPCAManager.plot2DData(sampleMap, nDim, false, CommonCommand.outputPath + "plot_eigenvectors_flashpca_outliers_removed.pdf");
        }
        
        FlashPCAManager.generateNewSampleFile(
                    sampleMap,
                    //outlierSet,
                    CohortLevelFilterCommand.sampleFile,
                    "pruned_sample_file.txt");//generate new sample file, can't simly change fam file
    }

    private static void runPlinkPedToBed(String inputName, String outputName, String remove_cmd) {
        LogManager.writeAndPrint("Creating bed file with plink for flashpca");
        // Convert PED & MAP to BED format with PLINK
        String cmd = ThirdPartyToolManager.PLINK
                + " --file " + CommonCommand.outputPath + inputName
                + " --mind 0.99"
                + " --make-bed"
                + " --out " + CommonCommand.outputPath + outputName
                + remove_cmd;

        //plink output is automatically stored in <outputPath+plink>.log
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
