package function.genotype.pedmap;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import function.genotype.base.Sample;
import function.genotype.base.SampleManager;
import global.Index;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author macrina
 */
public class FlashPCAManager {

    public static void runFlashPCA(String inputName, String outExt, String logName) {
        LogManager.writeAndPrint("flashpca for eigenvalues, vectors, pcs and percent variance explained by each pc. #dimenions = " + PedMapCommand.numEvec);

        try {
            if (PedMapCommand.numEvec <= 0) {
                throw new IllegalArgumentException("Number of eigenvectors as input to flashpca cant be 0");
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        }

        String cmd = ThirdPartyToolManager.FLASHPCA
                + " --bfile " + CommonCommand.outputPath + inputName
                + " --ndim " + PedMapCommand.numEvec
                + " --outpc " + CommonCommand.outputPath + "pcs" + outExt
                + " --outvec " + CommonCommand.outputPath + "eigenvectors" + outExt
                + " --outval " + CommonCommand.outputPath + "eigenvalues" + outExt
                + " --outpve " + CommonCommand.outputPath + "pve" + outExt
                + " 2>&1 >> " + CommonCommand.outputPath + logName;
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        LogManager.writeAndPrint("Verify flashpca mean square errors");
        cmd = ThirdPartyToolManager.FLASHPCA
                + " -v" 
                + " --bfile " + CommonCommand.outputPath + inputName
                + " --check"
                + " --outvec " + CommonCommand.outputPath + "eigenvectors" + outExt
                + " --outval " + CommonCommand.outputPath + "eigenvalues" + outExt
                + " 2>&1 >> " + CommonCommand.outputPath + logName;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});

        //if condition on rms ; print a  warning
        try {
            Charset charset = Charset.defaultCharset();
            List<String> log_lines = Files.readAllLines(Paths.get(CommonCommand.outputPath + logName), charset);
            String text = log_lines.get(log_lines.size() - 2);
            String[] rmsStr;
            if (text.contains("Root mean squared error")) {
                rmsStr = text.split(":");
            } else {
                throw new NoSuchFieldException("root mean squared error could not be read from flashpca.log file");
            }
            double rmsVal = Double.parseDouble(rmsStr[rmsStr.length - 1].split("\\(")[0].trim());
            if (rmsVal > 0.0001) {
                ErrorManager.print("The root mean sq error of flashpca is very high " + Double.toString(rmsVal), 1);
            }
        } catch (IOException | NoSuchFieldException | NumberFormatException e) {
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

    public static void plot2DData(Map<String, SamplePCAInfo> sampleMap, int nDim, boolean includeOutlier, String pdfName) {
        int numCharts = 2;
        if (nDim == 3) {
            numCharts = 6;
        }

        List<JFreeChart> charts = new ArrayList<>(numCharts);
        switch (nDim) {
            case 1:
                charts.add(buildChartPcs(sampleMap, includeOutlier, -1, 0));
                charts.add(buildChartEvec(sampleMap, includeOutlier, -1, 0));
                break;
            case 2:
                //numCharts = 2;//4 or 6 2D series
                charts.add(buildChartPcs(sampleMap, includeOutlier, 0, 1));
                charts.add(buildChartEvec(sampleMap, includeOutlier, 0, 1));
                break;
            default:
                //numCharts = 6;//12 or 18 2D series
                charts.add(buildChartPcs(sampleMap, includeOutlier, 0, 1));
                charts.add(buildChartEvec(sampleMap, includeOutlier, 0, 1));
                charts.add(buildChartPcs(sampleMap, includeOutlier, 1, 2));
                charts.add(buildChartEvec(sampleMap, includeOutlier, 1, 2));
                charts.add(buildChartPcs(sampleMap, includeOutlier, 0, 2));
                charts.add(buildChartEvec(sampleMap, includeOutlier, 0, 2));
                break;
        }
        
        saveChartAsPDF(pdfName, charts, 630, 1100);
    }

    private static JFreeChart buildChartPcs(Map<String, SamplePCAInfo> sampleMap, boolean includeOutlier, int dim1, int dim2) {        
        XYSeries outlierSeries = new XYSeries("outlier");
        XYSeries caseSeries = new XYSeries("case");
        XYSeries ctrlSeries = new XYSeries("control");
        String plotName, xLabel, yLabel;
        int countCase, countCtrl, countOut;
        countOut = countCtrl = countCase = 0;

        if (dim1 == -1) {
            plotName = "principal components (pc) 1D scatter plot";

            xLabel = "sample number";
            yLabel = "pc in 1D";
            for (SamplePCAInfo samplePCAInfo : sampleMap.values()) {
                if (includeOutlier && samplePCAInfo.isOutlier()) {
                    outlierSeries.add(countOut++, samplePCAInfo.getPCAInfoPcs(dim2));
                } else {
                    if (samplePCAInfo.getPheno() == Index.CTRL) {
                        ctrlSeries.add(countCtrl++, samplePCAInfo.getPCAInfoPcs(dim2));
                    } else {
                        caseSeries.add(countCase++, samplePCAInfo.getPCAInfoPcs(dim2));
                    }
                }
            }
        } else {
            plotName = "principal components (pc) 2D scatter plot";
            xLabel = "pc dim " + Integer.toString(dim1 + 1);
            yLabel = "pc dim " + Integer.toString(dim2 + 1);
            for (SamplePCAInfo samplePCAInfo : sampleMap.values()) {
                double[] pcsData = samplePCAInfo.getPCAInfoPcs(dim1, dim2);
                if (includeOutlier && samplePCAInfo.isOutlier()) {
                    outlierSeries.add(pcsData[0], pcsData[1]);
                    countOut++;
                } else {
                    if (samplePCAInfo.getPheno() == Index.CTRL) {
                        ctrlSeries.add(pcsData[0], pcsData[1]);
                        countCtrl++;
                    } else {
                        caseSeries.add(pcsData[0], pcsData[1]);
                        countCase++;
                    }
                }
            }
        }

        System.out.println("original number of cases :" + SampleManager.getCaseNum());
        System.out.println("original number of controls :" + SampleManager.getCtrlNum());
        System.out.println("number of cases :" + countCase);
        System.out.println("number of controls :" + countCtrl);
        System.out.println("number of outliers :" + countOut);

        XYSeriesCollection dataCollection = new XYSeriesCollection();
        dataCollection.addSeries(ctrlSeries);
        dataCollection.addSeries(caseSeries);
        if (includeOutlier) {
            dataCollection.addSeries(outlierSeries);
        }

        JFreeChart chartOp = ChartFactory.createScatterPlot(plotName, xLabel, yLabel, dataCollection, PlotOrientation.HORIZONTAL, true, true, false);

        //setting series colors - blue = case, green = control, red = outliers if they exist
        XYPlot plotOp = (XYPlot) chartOp.getPlot();
        plotOp.getRenderer().setSeriesPaint(0, new Color(0x00, 0xFF, 0x00)); //ctrl = green
        plotOp.getRenderer().setSeriesPaint(1, new Color(0x00, 0x00, 0xFF)); //case = blue
        if (includeOutlier) {
            plotOp.getRenderer().setSeriesPaint(2, new Color(0xFF, 0x00, 0x00)); //outlier = red
        }
        plotOp.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        return chartOp;
    }

    public static JFreeChart buildChartEvec(Map<String, SamplePCAInfo> sample_map, boolean includeOutlier, int dim1, int dim2) {
        XYSeries outlierSeries = new XYSeries("outlier");
        XYSeries caseSeries = new XYSeries("case");
        XYSeries ctrlSeries = new XYSeries("control");

        String plotName, xLabel, yLabel;

        if (dim1 == -1) {
            plotName = "eigenvector (ev) 1D scatter plot";
            int countOut = 0;
            int countCase = 0;
            int countCtrl = 0;
            xLabel = "sample number";
            yLabel = "ev in 1D";
            for (SamplePCAInfo samplePCAInfo : sample_map.values()) {
                if (includeOutlier && samplePCAInfo.isOutlier()) {
                    outlierSeries.add(countOut++, samplePCAInfo.getPCAInfoEvec(dim2));
                } else {
                    if (samplePCAInfo.getPheno() == Index.CTRL) {
                        ctrlSeries.add(countCtrl++, samplePCAInfo.getPCAInfoEvec(dim2));
                    } else {
                        caseSeries.add(countCase++, samplePCAInfo.getPCAInfoEvec(dim2));
                    }
                }
            }
        } else {
            plotName = "eigenvector (ev) 2D scatter plot";
            xLabel = "ev dim " + Integer.toString(dim1 + 1);
            yLabel = "ev dim " + Integer.toString(dim2 + 1);
            for (SamplePCAInfo samplePCAInfo : sample_map.values()) {
                double[] evecData = samplePCAInfo.getPCAInfoEvec(dim1, dim2);
                if (includeOutlier && samplePCAInfo.isOutlier()) {
                    outlierSeries.add(evecData[0], evecData[1]);
                } else {
                    if (samplePCAInfo.getPheno() == Index.CTRL) {
                        ctrlSeries.add(evecData[0], evecData[1]);
                    } else {
                        caseSeries.add(evecData[0], evecData[1]);
                    }
                }
            }
        }
        XYSeriesCollection dataCollection = new XYSeriesCollection();
        dataCollection.addSeries(ctrlSeries);
        dataCollection.addSeries(caseSeries);
        if (includeOutlier) {
            dataCollection.addSeries(outlierSeries);
        }

        JFreeChart chart_op = ChartFactory.createScatterPlot(plotName, xLabel, yLabel, dataCollection, PlotOrientation.HORIZONTAL, true, true, false);

        //setting series colors - blue = case, green = control, red = outliers if they exist
        XYPlot plot_op = (XYPlot) chart_op.getPlot();
        plot_op.getRenderer().setSeriesPaint(0, new Color(0x00, 0xFF, 0x00)); //ctrl = green
        plot_op.getRenderer().setSeriesPaint(1, new Color(0x00, 0x00, 0xFF)); //case = blue
        if (includeOutlier) {
            plot_op.getRenderer().setSeriesPaint(2, new Color(0xFF, 0x00, 0x00)); //outlier = red
        }
        plot_op.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        return chart_op;
    }

    public static void generateNewSampleFile(HashSet<String> outlierSet, String sampleFile, String outputFile) {
        try {
            FileWriter fw = new FileWriter(CommonCommand.outputPath + outputFile);
            Files.lines(Paths.get(sampleFile))
                    .map(line -> line.split("\\t"))
                    .filter(line -> !outlierSet.contains(line[1]))
                    .forEach(line -> printLine(fw, line));
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    private static void printLine(FileWriter fw, String[] line) {
        try {
            fw.write(String.join("\\t", line) + "\n");
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    public static HashSet<String> getOutliers(String fileName, String outlierFile) {
        HashSet<String> outlierSet = new HashSet<>();
        float zThreshSum = PedMapCommand.zThresh * PedMapCommand.numNeighbor;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outlierFile))) {
            String[] header = br.readLine().replaceAll("^\\s+", "").split(" +");

            if (!"Z".equals(header[4])) {
                throw new IllegalArgumentException("Column names in nearest neighbor file " + fileName + " are incorrect");
            }
            writer.write(header[0] + "\t" + header[1] + "\n");
            String line;
            int countLine = 1;
            String iidCurr = "";
            String fidCurr = "";
            float sumZ = 0.0f;
            while ((line = br.readLine()) != null) {
                String[] dataStrLine = line.replaceAll("^\\s+", "").split(" +");

                if (!dataStrLine[0].equals(fidCurr) || !dataStrLine[1].equals(iidCurr) || countLine == 1) {

                    if (sumZ < zThreshSum) {
                        System.out.println(dataStrLine[0] + "\t" + dataStrLine[1]);
                        writer.write(dataStrLine[0] + "\t" + dataStrLine[1] + "\n");
                        outlierSet.add(dataStrLine[1]);
                    }
                    countLine = PedMapCommand.numNeighbor;
                    fidCurr = dataStrLine[0];
                    iidCurr = dataStrLine[1];
                    sumZ = Float.valueOf(dataStrLine[4]);
                } else {
                    countLine = countLine - 1;
                    sumZ = sumZ + Float.valueOf(dataStrLine[4]);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        System.out.println("Size of outlier set: " + outlierSet.size());
        return outlierSet;
    }

    public static Map<String, SamplePCAInfo> getSampleMap(int ndim, String evecFileName, 
            String pcsFileName, ArrayList<Sample> sampleList) {
        Map<String, SamplePCAInfo> sampleMap = sampleList.stream().collect(Collectors.toMap(Sample::getName, s -> new SamplePCAInfo(s, ndim)));
        System.out.println("Files from which we're reading; evec: " + evecFileName + " pcs: " + pcsFileName);

        try {
            BufferedReader brEvec = new BufferedReader(new FileReader(evecFileName));
            BufferedReader brPcs = new BufferedReader(new FileReader(pcsFileName));
            brEvec.readLine();
            brPcs.readLine();
            String evecLine;
            String pcsLine;

            while ((evecLine = brEvec.readLine()) != null && (pcsLine = brPcs.readLine()) != null) {
                String[] evecStrLine = evecLine.split("\\t");
                String[] pcsStrLine = pcsLine.split("\\t");
                if (sampleMap.containsKey(evecStrLine[1])) {
                    sampleMap.get(evecStrLine[1]).setEvec(ndim, evecStrLine);
                    sampleMap.get(evecStrLine[1]).setPcs(ndim, pcsStrLine);
                }
            }
        } catch (IOException e) {
            ErrorManager.send(e);
        }
        
        return sampleMap;
    }

    private static void saveChartAsPDF(String fileName, List<JFreeChart> charts, float ht, float wth) {
        try {
            Document document = new Document(new Rectangle(wth, ht), 0, 0, 0, 0);
            //Document document = new Document(PageSize.A0);
            document.addAuthor("atav");
            document.addSubject("flashpca_plot");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            //writeChartAsPDF(cb, chart, PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight(),true);
            float indWidth = wth;
            float indHt = ht;
            int numChartsX = 1;
            int numChartsY = 1;
            if (charts.size() > 2) {
                indWidth = wth / 3;
                numChartsX = 3;
            }
            if (charts.size() > 1) {
                indHt = ht / 2;
                numChartsY = 2;
            }

            int counter = 0;
            float currHt = 0;
            float currWth = 0;
            for (int i = 0; i < numChartsX; i++) {
                for (int j = 0; j < numChartsY; j++) {
                    PdfTemplate tp = cb.createTemplate(indWidth, indHt);
                    Graphics2D g2 = new PdfGraphics2D(tp, indWidth, indHt);
                    Rectangle2D r2D = new Rectangle2D.Double(0, 0, indWidth, indHt);
                    charts.get(counter).draw(g2, r2D, null);
                    g2.dispose();
                    cb.addTemplate(tp, currWth, currHt);
                    //curr_wth = currWth +  indWidth;
                    currHt = currHt + indHt;
                    counter = counter + 1;
                }
                currHt = 0;
                currWth = currWth + indWidth;
            }
            document.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static void getevecDatafor1DPlot(String inputFileName, String pdfFileName, 
            String plotName, String plotTitle, String xName, String yName) {
        Charset charset = Charset.defaultCharset();
        //double[] xdata = new double[ndim];  
        XYSeries xyData = new XYSeries(plotName);
        try {
            List<String> fileLines;
            fileLines = Files.readAllLines(Paths.get(inputFileName), charset);
            if (fileLines.size() != PedMapCommand.numEvec) {
                String message;
                message = String.format("File %s has too many lines. #lines must equal #pcs!", inputFileName);
                throw new IllegalArgumentException(message);
            }

            for (int i = 0; i < fileLines.size(); i++) {
                xyData.add(i + 1, Double.parseDouble(fileLines.get(i)));
            }

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(xyData);
            List<JFreeChart> charts = new ArrayList<>(1);
            charts.add(ChartFactory.createXYLineChart(plotTitle, xName, yName, (XYDataset) dataset, PlotOrientation.VERTICAL, true, true, false));

            saveChartAsPDF(pdfFileName, charts, 630, 1100);

        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
}
