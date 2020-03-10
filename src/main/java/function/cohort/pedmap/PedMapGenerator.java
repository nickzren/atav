package function.cohort.pedmap;

import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CalledVariant;
import function.cohort.base.Sample;
import global.Data;
import function.cohort.base.SampleManager;
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
    String outputName = "output";
    String pedFile = CommonCommand.outputPath + outputName + ".ped";
    String mapFile = CommonCommand.outputPath + outputName + ".map";
    final String tmpPedFile = CommonCommand.outputPath + "output_tmp.ped";
    final String kinshipPrunedSampleFile = CommonCommand.outputPath + "kinship_pruned_sample.txt";
    int qualifiedVariants = 0;
    // --eigenstrat
    private static final String EIGENSTRAT_SCRIPT_PATH = Data.ATAV_HOME + "lib/run_eigenstrat.py";
    // --kinship
    private static final String KINSHIP_SCRIPT_PATH = Data.ATAV_HOME + "lib/run_kinship.py";

    // flashpca
    Map<String, SamplePCAInfo> sampleMap4FlashPCA;

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
        String sampleFile = SampleManager.getExistingSampleFile();

        if (PedMapCommand.isKinship) {
            doKinship();

            String newOutputName = "kinship_plink_pruned_sample";
            runPlinkToPrunePed(outputName, kinshipPrunedSampleFile, newOutputName);

            outputName = newOutputName;
            pedFile = CommonCommand.outputPath + outputName + ".ped";
            mapFile = CommonCommand.outputPath + outputName + ".map";
            sampleFile = kinshipPrunedSampleFile;
        }

        if (PedMapCommand.isFlashPCA) {
            doFlashPCA(outputName, sampleFile);
        }

        if (PedMapCommand.isEigenstrat) {
            doEigesntrat(pedFile, mapFile, sampleFile);
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

    private void doEigesntrat(String pedFile, String mapFile, String sampleFile) {
        String cmd = ThirdPartyToolManager.PYTHON
                + " " + EIGENSTRAT_SCRIPT_PATH
                + " --sample " + sampleFile
                + " --prune-sample"
                + " --genotypefile " + pedFile
                + " --snpfile " + mapFile
                + " --indivfile " + pedFile
                + " --numoutevec 10"
                + " --numoutlieriter 5"
                + " --outputdir " + CommonCommand.realOutputPath;
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    private void doKinship() {
        runPlinkPedToBed("output", "kinship_plink", "");

        // Run KING to get kinship
        String cmd = ThirdPartyToolManager.KING
                + " -b " + CommonCommand.outputPath + "kinship_plink.bed"
                + " --kinship"
                + " --related"
                + " --degree 3"
                + " --prefix " + CommonCommand.outputPath + "king";
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        // Run kinship pruning script
        cmd = ThirdPartyToolManager.PYTHON
                + " " + KINSHIP_SCRIPT_PATH
                + " " + SampleManager.getExistingSampleFile()
                + " " + CommonCommand.outputPath + "king.kin0"
                + " " + CommonCommand.outputPath + "king.kin"
                + " --relatedness_threshold " + PedMapCommand.kinshipRelatednessThreshold
                + " --seed " + PedMapCommand.kinshipSeed
                + " --output " + kinshipPrunedSampleFile
                + " --verbose";
        if (!PedMapCommand.sampleCoverageSummaryPath.isEmpty()) {
            cmd += " --sample_coverage_summary " + PedMapCommand.sampleCoverageSummaryPath;
        }
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    private void doFlashPCA(String inputName, String sampleFile) {
        runPlinkPedToBed(inputName, "flashpca_plink", "");

        String label = "flashpca_";
        FlashPCAManager.runFlashPCA("flashpca_plink", label, "flashpca.log");
        FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + label + "eigenvalues",
                CommonCommand.outputPath + label + "plot_eigenvalues.pdf",
                "eigenvalues",
                "plot of eigenvalues",
                "eigenvalue number",
                "eigenvalue");

        FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + label + "pve",
                CommonCommand.outputPath + label + "plot_pve.pdf",
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
        sampleMap4FlashPCA = FlashPCAManager.getSampleMap(nDim,
                CommonCommand.outputPath + label + "eigenvectors",
                CommonCommand.outputPath + label + "pcs",
                sampleList);

        sampleMap4FlashPCA.entrySet().removeIf(entry -> (entry.getValue().getToFilter()));

        FlashPCAManager.plot2DData(sampleMap4FlashPCA, nDim, false, CommonCommand.outputPath + "plot_eigenvectors_flashpca.pdf");
        
        if (PedMapCommand.flashPCAPlinkPruning) {
            LogManager.writeAndPrint("Finding outliers using plink ibs clustering");
            
            FlashPCAManager.findOutliers();

            //read each line of outlier nearest file and filter based on Z-score
            HashSet<String> outlierSet = FlashPCAManager.getOutliers(
                    CommonCommand.outputPath + label + "plink_outlier.nearest",
                    CommonCommand.outputPath + label + "outlier_file.txt");

            LogManager.writeAndPrint("Redo flashpca with outliers removed");

            String remove_cmd = " --remove " + CommonCommand.outputPath + label + "outlier_file.txt";

            sampleMap4FlashPCA.entrySet().stream().forEach(e -> e.getValue().setOutlier(outlierSet));
            FlashPCAManager.plot2DData(sampleMap4FlashPCA,
                    nDim, true,
                    CommonCommand.outputPath + "plot_eigenvectors_flashpca_color_outliers.pdf");//cases,controls.outliers - 3 colors

            runPlinkPedToBed(inputName, "flashpca_plink_outlier_removed", remove_cmd);
            label = "flashpca_outliers_removed_";
            FlashPCAManager.runFlashPCA("flashpca_plink_outlier_removed", label, "flashpca.log");

            //making plots with / without outlier
            FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + label + "eigenvalues",
                    CommonCommand.outputPath + label + "eigenvalues_outliers_removed.pdf",
                    "eigenvalues no outliers", "plot of eigenvalues",
                    "eigenvalue number",
                    "eigenvalue");

            FlashPCAManager.getevecDatafor1DPlot(CommonCommand.outputPath + label + "pve",
                    CommonCommand.outputPath + label + "pve_outliers_removed.pdf",
                    "percent variance no outliers",
                    "percent variance explained by eigval",
                    "eigenvalue number",
                    "per_var_explained");

            sampleList = sampleMap4FlashPCA.values().stream()
                    .filter(s -> !s.isOutlier())
                    .map(SamplePCAInfo::getSample)
                    .collect(Collectors.toCollection(ArrayList::new));

            sampleMap4FlashPCA = FlashPCAManager.getSampleMap(nDim,
                    CommonCommand.outputPath + label + "eigenvectors",
                    CommonCommand.outputPath + label + "pcs",
                    sampleList);

            FlashPCAManager.plot2DData(sampleMap4FlashPCA, nDim, false, CommonCommand.outputPath + label + "plot_eigenvectors.pdf");
        }

        FlashPCAManager.generateNewSampleFile(sampleMap4FlashPCA,
                sampleFile,
                "flashpca_pruned_sample_file.txt");//generate new sample file, can't simly change fam file
    }

    private void runPlinkPedToBed(String inputName, String outputName, String remove_cmd) {
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

    private void runPlinkToPrunePed(String inputName, String prunedSampleFile, String outputName) {
        LogManager.writeAndPrint("Pruning ped file with plink");
        // Convert PED & MAP to BED format with PLINK
        String cmd = ThirdPartyToolManager.PLINK
                + " --file " + CommonCommand.outputPath + inputName
                + " --keep " + prunedSampleFile
                + " --recode"
                + " --out " + CommonCommand.outputPath + outputName;

        //plink output is automatically stored in <outputPath+plink>.log
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
