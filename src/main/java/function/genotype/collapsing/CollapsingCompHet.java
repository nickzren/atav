package function.genotype.collapsing;

import function.genotype.vargeno.SampleVariantCount;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Index;
import function.annotation.base.GeneManager;
import function.genotype.base.SampleManager;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class CollapsingCompHet extends CollapsingBase {

    ArrayList<CompHetOutput> outputList = new ArrayList<>();
    ArrayList<ArrayList<CompHetOutput>> geneListVector = new ArrayList<>();
    HashSet<Integer> variantIdSet = new HashSet<>();
    HashSet<String> currentGeneList = new HashSet<>();
    BufferedWriter bwCompHet = null;
    final String comphetFilePath = CommonCommand.outputPath + "comphet.csv";

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCompHet = new BufferedWriter(new FileWriter(comphetFilePath));
            bwCompHet.write(CompHetOutput.title);
            bwCompHet.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
        listCompHets();

        clearList();
    }

    @Override
    public void closeOutput() {
        try {
            super.closeOutput();

            bwCompHet.flush();
            bwCompHet.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CompHetOutput output = new CompHetOutput(calledVar);

            output.countSampleGenoCov();

            output.calculate();

            if (output.isValid()) {

                for (String geneName : calledVar.getGeneSet()) {
                    if (!geneName.equals("NA")) {
                        output.geneName = geneName;
                        outputList.add((CompHetOutput) output.clone());
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void listCompHets() {
        if (outputList.size() > 0) {
            Collections.sort(outputList);
        } else {
            return;
        }

        initGeneVariantList();

        try {
            for (ArrayList<CompHetOutput> list : geneListVector) {
                String geneName = list.get(0).geneName;

                LogManager.writeAndPrint("Analyzing qualified variants in gene ("
                        + geneName + ")");

                CollapsingSummary summary = summaryMap.get(geneName);

                doOutput(list, summary);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initGeneVariantList() {
        ArrayList<CompHetOutput> geneOutputList = null;

        for (CompHetOutput output : outputList) {
            if (!currentGeneList.contains(output.geneName)) {
                currentGeneList.add(output.geneName);
                updateGeneSummaryMap(output.geneName);

                geneOutputList = new ArrayList<CompHetOutput>();
                geneOutputList.add(output);
                geneListVector.add(geneOutputList);
            } else {
                geneOutputList.add(output);
            }
        }
    }

    private void doOutput(ArrayList<CompHetOutput> geneOutputList, CollapsingSummary summary) {
        try {
            int outputSize = geneOutputList.size();

            CompHetOutput output1, output2;

            for (Sample sample : SampleManager.getList()) {

                for (int i = 0; i < outputSize; i++) {

                    output1 = geneOutputList.get(i);

                    int geno1 = output1.getCalledVariant().getGenotype(sample.getIndex());

                    if (output1.isQualifiedGeno(geno1)) {

                        output1.calculateLooFreq(sample);

                        if (output1.isLooFreqValid()) {

                            if (isOutputValid(output1, sample, summary)) {
                                continue;
                            }

                            for (int j = i + 1; j < outputSize; j++) {

                                output2 = geneOutputList.get(j);

                                checkOutputValid(output1, output2, sample, summary);
                            }
                        }
                    }
                }
            }

            variantIdSet.clear();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private boolean isOutputValid(CompHetOutput output1,
            Sample sample, CollapsingSummary summary) throws Exception {
        if (output1.isQualifiedGeno(sample)) {
            summary.updateSampleVariantCount4CompHet(sample.getIndex());

            updateSummaryVariantCount(output1, summary);

            StringBuilder sb = new StringBuilder();
            sb.append(sample.getFamilyId()).append(",");
            sb.append(sample.getName()).append(",");
            sb.append(sample.getPhenotype()).append(",");
            sb.append("'").append(output1.geneName).append("'").append(",");
            sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.geneName))).append(",");
            sb.append("NA,"); // Var Case Freq #1 & #2 (co-occurance)
            sb.append("NA,"); // Var Ctrl Freq #1 & #2 (co-occurance)
            sb.append(output1.getString(sample));

            bwCompHet.write(sb.toString());
            bwCompHet.newLine();

            SampleVariantCount.update(output1.getCalledVariant().isSnv(),
                    output1.getCalledVariant().getGenotype(sample.getIndex()),
                    sample.getIndex());

            return true;
        }

        return false;
    }

    private void checkOutputValid(CompHetOutput output1, CompHetOutput output2,
            Sample sample, CollapsingSummary summary) throws Exception {
        if (!output1.getCalledVariant().getVariantIdStr().equals(
                output2.getCalledVariant().getVariantIdStr())) {

            int geno2 = output2.getCalledVariant().getGenotype(sample.getIndex());

            if (output2.isQualifiedGeno(geno2)) {

                output2.calculateLooFreq(sample);

                if (output2.isLooFreqValid()) {

                    double[] coFreq = getCoOccurrenceFreq(output1, output2, sample);

                    if (CollapsingCommand.isMaxLooCombFreqValid(coFreq[Index.CTRL])) {

                        summary.updateSampleVariantCount4CompHet(sample.getIndex());

                        updateSummaryVariantCount(output1, summary);
                        updateSummaryVariantCount(output2, summary);

                        StringBuilder sb = new StringBuilder();
                        sb.append(sample.getFamilyId()).append(",");
                        sb.append(sample.getName()).append(",");
                        sb.append(sample.getPhenotype()).append(",");
                        sb.append("'").append(output1.geneName).append("'").append(",");
                        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.geneName))).append(",");
                        sb.append(FormatManager.getDouble(coFreq[Index.CASE])).append(",");
                        sb.append(FormatManager.getDouble(coFreq[Index.CTRL])).append(",");
                        sb.append(output1.getString(sample));
                        sb.append(output2.getString(sample));

                        SampleVariantCount.update(output1.getCalledVariant().isSnv(),
                                output1.getCalledVariant().getGenotype(sample.getIndex()),
                                sample.getIndex());

                        SampleVariantCount.update(output2.getCalledVariant().isSnv(),
                                output2.getCalledVariant().getGenotype(sample.getIndex()),
                                sample.getIndex());

                        bwCompHet.write(sb.toString());
                        bwCompHet.newLine();
                    }
                }
            }
        }
    }

    /*
     * The number of people who have BOTH of the variants divided by the total
     * number of covered people. freq[0] Frequency of Variant #1 & #2
     * (co-occurance) in cases. freq[1] Frequency of Variant #1 & #2
     * (co-occurance) in ctrls
     */
    private double[] getCoOccurrenceFreq(CompHetOutput output1,
            CompHetOutput output2, Sample observedSample) {
        double[] freq = new double[2];

        int quanlifiedCaseCount = 0, qualifiedCtrlCount = 0;
        int totalCaseCount = 0, totalCtrlCount = 0;

        for (Sample sample : SampleManager.getList()) {
            if (sample.equals(observedSample)) // ignore the sample where the variant was observed
            {
                continue;
            }

            output1.calculateLooFreq(sample); // is minor ref for sample level
            output2.calculateLooFreq(sample);

            boolean isCoQualifiedGeno = isCoQualifiedGeno(output1, output2, sample.getIndex());

            if (sample.isCase()) {
                totalCaseCount++;
                if (isCoQualifiedGeno) {
                    quanlifiedCaseCount++;
                }
            } else {
                totalCtrlCount++;
                if (isCoQualifiedGeno) {
                    qualifiedCtrlCount++;
                }
            }
        }

        freq[Index.CTRL] = MathManager.devide(qualifiedCtrlCount, totalCtrlCount);
        freq[Index.CASE] = MathManager.devide(quanlifiedCaseCount, totalCaseCount);

        output1.calculateLooFreq(observedSample); // update 'is maf ref' back for observed sample value
        output2.calculateLooFreq(observedSample);

        return freq;
    }

    private boolean isCoQualifiedGeno(CompHetOutput output1,
            CompHetOutput output2, int index) {
        int geno1 = output1.getCalledVariant().getGenotype(index);
        int geno2 = output2.getCalledVariant().getGenotype(index);

        if (output1.isQualifiedGeno(geno1)
                && output2.isQualifiedGeno(geno2)) {
            return true;
        } else {
            return false;
        }
    }

    private void updateSummaryVariantCount(CompHetOutput output, CollapsingSummary summary) {
        if (!variantIdSet.contains(output.getCalledVariant().getVariantId())) {
            summary.updateVariantCount(output);
            variantIdSet.add(output.getCalledVariant().getVariantId());
        }
    }

    private void clearList() {
        outputList.clear();
        geneListVector.clear();
        currentGeneList.clear();
    }

    @Override
    public String toString() {
        return "It is running a collapsing compound heterozygosity function...";
    }
}
