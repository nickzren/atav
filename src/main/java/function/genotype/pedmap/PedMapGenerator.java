package function.genotype.pedmap;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.Sample;
import global.Data;
import function.genotype.base.SampleManager;
import global.Index;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import utils.CommonCommand;
import utils.LogManager;
import utils.ThirdPartyToolManager;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.stream.Collectors;
import utils.ErrorManager;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;

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

    //flashpca
    Map<String, SamplePCAInfo> sample_map;

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
                + " --kinship"
                + " --related"
                + " --degree 3"
                + " --prefix " + CommonCommand.outputPath + "king";
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
        // Run kinship pruning script
        cmd = ThirdPartyToolManager.PYTHON
                + " " + KINSHIP_SCRIPT_PATH
                + " " + GenotypeLevelFilterCommand.sampleFile
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

    public static void runPlinkPedToBed(String ip_name, String output_name, String remove_cmd) {
        LogManager.writeAndPrint("Creating bed file with plink for flashpca");
        // Convert PED & MAP to BED format with PLINK
        String cmd = ThirdPartyToolManager.PLINK
                + " --noweb "
                + " --file " + CommonCommand.outputPath + ip_name
                + " --make-bed"
                + " --out " + CommonCommand.outputPath + output_name
                + remove_cmd;

        //plink output is automatically stored in <outputPath+plink>.log
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

    }

    public void runFlashPCA(String ip_name, String out_ext, String logname) {
        LogManager.writeAndPrint("flashpca for eigenvalues, vectors, pcs and percent variance explained by each pc. #dimenions = " + PedMapCommand.numEvec);

        try {
            if (PedMapCommand.numEvec <= 0) {
                throw new IllegalArgumentException("number of eigenvectors as input to flashpca cant be 0");
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        String cmd = ThirdPartyToolManager.FLASHPCA
                + " --bfile " + CommonCommand.outputPath + ip_name
                + " --ndim " + PedMapCommand.numEvec
                + " --outpc " + CommonCommand.outputPath + "pcs" + out_ext
                + " --outvec " + CommonCommand.outputPath + "eigenvectors" + out_ext
                + " --outval " + CommonCommand.outputPath + "eigenvalues" + out_ext
                + " --outpve " + CommonCommand.outputPath + "pve" + out_ext
                + " 2>&1 >> " + CommonCommand.outputPath + logname;
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        LogManager.writeAndPrint("verify flashpca mean square errors");
        cmd = ThirdPartyToolManager.FLASHPCA
                + " -v" + " --bfile " + CommonCommand.outputPath + ip_name
                + " --check "
                + " --outvec " + CommonCommand.outputPath + "eigenvectors" + out_ext
                + " --outval " + CommonCommand.outputPath + "eigenvalues" + out_ext
                + " 2>&1 >> " + CommonCommand.outputPath + logname;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        //if condition on rms ; print a  warning
        try {
            Charset charset = Charset.defaultCharset();
            List<String> log_lines = Files.readAllLines(Paths.get(CommonCommand.outputPath + logname), charset);
            String text = log_lines.get(log_lines.size() - 2);
            String[] rms_str;
            if (text.contains("Root mean squared error")) {
                rms_str = text.split(":");
            } else {
                throw new NoSuchFieldException("root mean squared error could not be read from flashpca.log file");
            }
            double rms_val = Double.parseDouble(rms_str[rms_str.length - 1].split("\\(")[0].trim());
            if (rms_val > 0.0001) {
                ErrorManager.print("The root mean sq error of flashpca is very high " + Double.toString(rms_val), 1);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void findOutliers() {
        String cmd = ThirdPartyToolManager.PLINK
                + " --noweb "
                + " --bfile " + CommonCommand.outputPath + "plink"
                + " --neighbour 1 " + String.valueOf(PedMapCommand.numNeighbor)
                + " --out " + CommonCommand.outputPath + "plink_outlier"
                + " 2>&1 >> " + CommonCommand.outputPath + "flashpca.log";

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    public void doFlashPCA() {
        //i wanted to test for existence of bed file but can't say if the bed is current or of old run
        runPlinkPedToBed("output", "plink", "");

        String out_ext = "_flashpca";
        runFlashPCA("plink", out_ext, "flashpca.log");
        getevecDatafor1DPlot(CommonCommand.outputPath + "eigenvalues" + out_ext, CommonCommand.outputPath + "plot_eigenvalues_flashpca.pdf", PedMapCommand.numEvec, "eigenvalues", "plot of eigenvalues", "eigenvalue number", "eigenvalue");

        getevecDatafor1DPlot(CommonCommand.outputPath + "pve" + out_ext, CommonCommand.outputPath + "plot_pve_flashpca.pdf", PedMapCommand.numEvec, "percent variance", "percent_variance explained by eigval", "eigenvalue number", "per_var_explained");

        int ndim = 3;
        if (PedMapCommand.numEvec < 3) {
            LogManager.writeAndPrint("number of dimensions to plot can't be greater than total number of eigenvectors");
            ndim = PedMapCommand.numEvec;
        }
        ArrayList<Sample> sample_info = SampleManager.getList();
        getdata_dim123(ndim, CommonCommand.outputPath + "eigenvectors" + out_ext, CommonCommand.outputPath + "pcs" + out_ext, sample_info);
        Plot2DData(ndim, false, CommonCommand.outputPath + "plot_eigenvectors_flashpca.pdf");

        LogManager.writeAndPrint("finding outliers using plink ibs clustering");

        if (!PedMapCommand.isKeepOutliers) {
            findOutliers();

            //read each line of outlier nearest file and filter based on Z-score, prop_diff parameters 
            HashSet<String> outlierSet = getOutliers(CommonCommand.outputPath + "plink_outlier.nearest", CommonCommand.outputPath + "outlier_file.txt");

            generateNewSampleFile(outlierSet, GenotypeLevelFilterCommand.sampleFile, "pruned_sample_file.txt");//generate new sample file, can't simly change fam file

            LogManager.writeAndPrint("redo flashpca with outliers removed");

            String remove_cmd = " --remove " + CommonCommand.outputPath + "outlier_file.txt";

            sample_map.entrySet().stream().forEach(e -> e.getValue().setOutlier(outlierSet));
            Plot2DData(ndim, true, CommonCommand.outputPath + "plot_eigenvectors_flashpca_color_outliers.pdf");//cases,controls.outliers - 3 colors

            runPlinkPedToBed("output", "plink_outlier_removed", remove_cmd);
            out_ext = "_flashpca_outliers_removed";
            runFlashPCA("plink_outlier_removed", out_ext, "flashpca.log");

            //making plots with / without outlier
            getevecDatafor1DPlot(CommonCommand.outputPath + "eigenvalues" + out_ext, CommonCommand.outputPath + "eigenvalues_flashpca_outliers_removed.pdf", PedMapCommand.numEvec, "eigenvalues no outliers", "plot of eigenvalues", "eigenvalue number", "eigenvalue");

            getevecDatafor1DPlot(CommonCommand.outputPath + "pve" + out_ext, CommonCommand.outputPath + "pve_flashpca_outliers_removed.pdf", PedMapCommand.numEvec, "percent variance no outliers", "percent variance explained by eigval", "eigenvalue number", "per_var_explained");

            sample_info = sample_map.values().stream().filter(s -> !s.isOutlier()).map(SamplePCAInfo::getSample).collect(Collectors.toCollection(ArrayList::new));

            getdata_dim123(ndim, CommonCommand.outputPath + "eigenvectors" + out_ext, CommonCommand.outputPath + "pcs" + out_ext, sample_info);
            Plot2DData(ndim, false, CommonCommand.outputPath + "plot_eigenvectors_flashpca_outliers_removed.pdf");

        }
    }

    public void Plot2DData(int ndim, boolean with_out, String pdf_name) {
        int numCharts = 2;
        if (ndim == 3) {
            numCharts = 6;
        }

        List<JFreeChart> charts = new ArrayList<>(numCharts);
        if (ndim == 1) {
            charts.add(buildChartPcs(with_out, -1, 0));
            charts.add(buildChartEvec(with_out, -1, 0));
        } else if (ndim == 2) {
            //numCharts = 2;//4 or 6 2D series
            charts.add(buildChartPcs(with_out, 0, 1));
            charts.add(buildChartEvec(with_out, 0, 1));
        } else {
            //numCharts = 6;//12 or 18 2D series
            charts.add(buildChartPcs(with_out, 0, 1));
            charts.add(buildChartEvec(with_out, 0, 1));
            charts.add(buildChartPcs(with_out, 1, 2));
            charts.add(buildChartEvec(with_out, 1, 2));
            charts.add(buildChartPcs(with_out, 0, 2));
            charts.add(buildChartEvec(with_out, 0, 2));
        }
        saveChartAsPDF(pdf_name, charts, 630, 1100);
    }

    public JFreeChart buildChartPcs(boolean with_out, int dim1, int dim2) {
        XYSeries outlier_series = new XYSeries("outlier");
        XYSeries case_series = new XYSeries("case");
        XYSeries ctrl_series = new XYSeries("control");
        String plotName, xlabel, ylabel;
        int count_case, count_ctrl, count_out;
        count_out = count_ctrl = count_case = 0;

        if (dim1 == -1) {
            plotName = "principal components (pc) 1D scatter plot";

            xlabel = "sample number";
            ylabel = "pc in 1D";
            for (SamplePCAInfo map_val : sample_map.values()) {
                if (with_out && map_val.isOutlier()) {
                    outlier_series.add(count_out++, map_val.getPCAInfoPcs(dim2));
                } else {
                    if (map_val.getPheno() == 0) {
                        ctrl_series.add(count_ctrl++, map_val.getPCAInfoPcs(dim2));
                    } else {
                        case_series.add(count_case++, map_val.getPCAInfoPcs(dim2));
                    }
                }
            }
        } else {
            plotName = "principal components (pc) 2D scatter plot";
            xlabel = "pc dim " + Integer.toString(dim1 + 1);
            ylabel = "pc dim " + Integer.toString(dim2 + 1);
            for (SamplePCAInfo map_val : sample_map.values()) {
                double[] pcs_data = map_val.getPCAInfoPcs(dim1, dim2);
                if (with_out && map_val.isOutlier()) {
                    outlier_series.add(pcs_data[0], pcs_data[1]);
                    count_out++;
                } else {
                    if (map_val.getPheno() == 0) {
                        ctrl_series.add(pcs_data[0], pcs_data[1]);
                        count_ctrl++;
                    } else {
                        case_series.add(pcs_data[0], pcs_data[1]);
                        count_case++;
                    }
                }
            }
        }

        System.out.println("original number of cases :" + SampleManager.getCaseNum());
        System.out.println("original number of controls :" + SampleManager.getCtrlNum());
        System.out.println("number of cases :" + count_case);
        System.out.println("number of controls :" + count_ctrl);
        System.out.println("number of outliers :" + count_out);

        XYSeriesCollection data_collection = new XYSeriesCollection();
        data_collection.addSeries(ctrl_series);
        data_collection.addSeries(case_series);
        if (with_out) {
            data_collection.addSeries(outlier_series);
        }

        JFreeChart chart_op = ChartFactory.createScatterPlot(plotName, xlabel, ylabel, data_collection, PlotOrientation.HORIZONTAL, true, true, false);

        //setting series colors - blue = case, green = control, red = outliers if they exist
        XYPlot plot_op = (XYPlot) chart_op.getPlot();
        plot_op.getRenderer().setSeriesPaint(0, new Color(0x00, 0xFF, 0x00)); //ctrl = green
        plot_op.getRenderer().setSeriesPaint(1, new Color(0x00, 0x00, 0xFF)); //case = blue
        if (with_out) {
            plot_op.getRenderer().setSeriesPaint(2, new Color(0xFF, 0x00, 0x00)); //outlier = red
        }
        plot_op.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        return chart_op;
    }

    public JFreeChart buildChartEvec(boolean with_out, int dim1, int dim2) {
        XYSeries outlier_series = new XYSeries("outlier");
        XYSeries case_series = new XYSeries("case");
        XYSeries ctrl_series = new XYSeries("control");

        String plotName, xlabel, ylabel;

        if (dim1 == -1) {
            plotName = "eigenvector (ev) 1D scatter plot";
            int count_out = 0;
            int count_case = 0;
            int count_ctrl = 0;
            xlabel = "sample number";
            ylabel = "ev in 1D";
            for (SamplePCAInfo map_val : sample_map.values()) {
                if (with_out && map_val.isOutlier()) {
                    outlier_series.add(count_out++, map_val.getPCAInfoEvec(dim2));
                } else {
                    if (map_val.getPheno() == 0) {
                        ctrl_series.add(count_ctrl++, map_val.getPCAInfoEvec(dim2));
                    } else {
                        case_series.add(count_case++, map_val.getPCAInfoEvec(dim2));
                    }
                }
            }
        } else {
            plotName = "eigenvector (ev) 2D scatter plot";
            xlabel = "ev dim " + Integer.toString(dim1 + 1);
            ylabel = "ev dim " + Integer.toString(dim2 + 1);
            for (SamplePCAInfo map_val : sample_map.values()) {
                double[] evec_data = map_val.getPCAInfoEvec(dim1, dim2);
                if (with_out && map_val.isOutlier()) {
                    outlier_series.add(evec_data[0], evec_data[1]);
                } else {
                    if (map_val.getPheno() == 0) {
                        ctrl_series.add(evec_data[0], evec_data[1]);
                    } else {
                        case_series.add(evec_data[0], evec_data[1]);
                    }
                }
            }
        }
        XYSeriesCollection data_collection = new XYSeriesCollection();
        data_collection.addSeries(ctrl_series);
        data_collection.addSeries(case_series);
        if (with_out) {
            data_collection.addSeries(outlier_series);
        }

        JFreeChart chart_op = ChartFactory.createScatterPlot(plotName, xlabel, ylabel, data_collection, PlotOrientation.HORIZONTAL, true, true, false);

        //setting series colors - blue = case, green = control, red = outliers if they exist
        XYPlot plot_op = (XYPlot) chart_op.getPlot();
        plot_op.getRenderer().setSeriesPaint(0, new Color(0x00, 0xFF, 0x00)); //ctrl = green
        plot_op.getRenderer().setSeriesPaint(1, new Color(0x00, 0x00, 0xFF)); //case = blue
        if (with_out) {
            plot_op.getRenderer().setSeriesPaint(2, new Color(0xFF, 0x00, 0x00)); //outlier = red
        }
        plot_op.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        return chart_op;
    }

    public static void generateNewSampleFile(HashSet<String> outlierSet, String sample_file, String output_file) {
        try {
            FileWriter fw = new FileWriter(CommonCommand.outputPath + output_file);
            Files.lines(Paths.get(sample_file))
                    .map(line -> line.split("\\t"))
                    .filter(line -> !outlierSet.contains(line[1]))
                    .forEach(line -> printLine(fw, line));
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void printLine(FileWriter fw, String[] line) {
        try {
            fw.write(String.join("\\t", line) + "\n");
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static HashSet<String> getOutliers(String filename, String outlierFile) {
        HashSet<String> outlierSet = new HashSet<>();
        float z_thresh_sum = PedMapCommand.z_thresh * PedMapCommand.numNeighbor;
        try (BufferedReader br = new BufferedReader(new FileReader(filename));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outlierFile))) {
            String[] header = br.readLine().replaceAll("^\\s+", "").split(" +");

            if (!"Z".equals(header[4])) {
                throw new IllegalArgumentException("column names in nearest neighbor file " + filename + " are incorrect");
            }
            writer.write(header[0] + "\t" + header[1] + "\n");
            String line;
            int count_line = 1;
            String iid_curr = "";
            String fid_curr = "";
            float sum_z = 0.0f;
            while ((line = br.readLine()) != null) {
                String[] data_str_line = line.replaceAll("^\\s+", "").split(" +");

                if (!data_str_line[0].equals(fid_curr) || !data_str_line[1].equals(iid_curr) || count_line == 1) {

                    if (sum_z < z_thresh_sum) {

                        System.out.println(data_str_line[0] + "\t" + data_str_line[1]);
                        writer.write(data_str_line[0] + "\t" + data_str_line[1] + "\n");
                        outlierSet.add(data_str_line[1]);
                    }
                    count_line = PedMapCommand.numNeighbor;
                    fid_curr = data_str_line[0];
                    iid_curr = data_str_line[1];
                    sum_z = Float.valueOf(data_str_line[4]);
                } else {
                    count_line = count_line - 1;
                    sum_z = sum_z + Float.valueOf(data_str_line[4]);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        System.out.println("size of outlier set: " + outlierSet.size());
        return outlierSet;
    }

    public void getdata_dim123(int ndim, String evec_fileName, String pcs_fileName, ArrayList<Sample> sample_info) {

        sample_map = sample_info.stream().collect(Collectors.toMap(Sample::getName, s -> new SamplePCAInfo(s, ndim)));
        System.out.println("files from which we're reading; evec: " + evec_fileName + " pcs: " + pcs_fileName);

        try {

            BufferedReader br_evec = new BufferedReader(new FileReader(evec_fileName));
            BufferedReader br_pcs = new BufferedReader(new FileReader(pcs_fileName));
            br_evec.readLine();
            br_pcs.readLine();
            String evec_line;
            String pcs_line;

            while ((evec_line = br_evec.readLine()) != null && (pcs_line = br_pcs.readLine()) != null) {
                String[] evec_str_line = evec_line.split("\\t");
                String[] pcs_str_line = pcs_line.split("\\t");
                if (sample_map.containsKey(evec_str_line[1])) {
                    sample_map.get(evec_str_line[1]).setEvec(ndim, evec_str_line);
                    sample_map.get(evec_str_line[1]).setPcs(ndim, pcs_str_line);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void saveChartAsPDF(String filename, List<JFreeChart> charts, float ht, float wth) {
        try {
            Document document = new Document(new Rectangle(wth, ht), 0, 0, 0, 0);
            //Document document = new Document(PageSize.A0);
            document.addAuthor("atav");
            document.addSubject("flashpca_plot");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            //writeChartAsPDF(cb, chart, PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight(),true);
            float ind_width = wth;
            float ind_ht = ht;
            int num_charts_x = 1;
            int num_charts_y = 1;
            if (charts.size() > 2) {
                ind_width = wth / 3;
                num_charts_x = 3;
            }
            if (charts.size() > 1) {
                ind_ht = ht / 2;
                num_charts_y = 2;
            }

            int counter = 0;
            float curr_ht = 0;
            float curr_wth = 0;
            for (int i = 0; i < num_charts_x; i++) {
                for (int j = 0; j < num_charts_y; j++) {
                    PdfTemplate tp = cb.createTemplate(ind_width, ind_ht);
                    Graphics2D g2 = new PdfGraphics2D(tp, ind_width, ind_ht);
                    Rectangle2D r2D = new Rectangle2D.Double(0, 0, ind_width, ind_ht);
                    charts.get(counter).draw(g2, r2D, null);
                    g2.dispose();
                    cb.addTemplate(tp, curr_wth, curr_ht);
                    //curr_wth = curr_wth +  ind_width;
                    curr_ht = curr_ht + ind_ht;
                    counter = counter + 1;
                }
                curr_ht = 0;
                curr_wth = curr_wth + ind_width;
            }
            document.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public void getevecDatafor1DPlot(String input_filename, String pdf_filename, int ndim, String plotname, String plot_title, String x_name, String y_name) {
        Charset charset = Charset.defaultCharset();
        //double[] xdata = new double[ndim];  
        XYSeries xydata = new XYSeries(plotname);
        try {
            List<String> fileLines;
            fileLines = Files.readAllLines(Paths.get(input_filename), charset);
            if (fileLines.size() != ndim) {
                String message;
                message = String.format("File %s has too many lines. #lines must equal #pcs!", input_filename);
                throw new IllegalArgumentException(message);
            }

            for (int i = 0; i < fileLines.size(); i++) {
                xydata.add(i + 1, Double.parseDouble(fileLines.get(i)));
            }

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(xydata);
            List<JFreeChart> charts = new ArrayList<>(1);
            charts.add(ChartFactory.createXYLineChart(plot_title, x_name, y_name, (XYDataset) dataset, PlotOrientation.VERTICAL, true, true, false));

            saveChartAsPDF(pdf_filename, charts, 630, 1100);

        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start generating ped/map files";
    }
}
