package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import global.Index;
import function.annotation.base.GeneManager;
import function.annotation.base.IntolerantScoreManager;
import function.genotype.base.QualityManager;
import function.genotype.base.SampleManager;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 * @author nick
 */
public class ListTrioCompHet extends AnalysisBase4CalledVar {

    ArrayList<CompHetOutput> outputList = new ArrayList<CompHetOutput>();
    ArrayList<ArrayList<CompHetOutput>> geneListVector = new ArrayList<ArrayList<CompHetOutput>>();
    HashSet<String> currentGeneList = new HashSet<String>();
    HashSet<String> uniqueId = new HashSet<String>();
    BufferedWriter bwDetails = null;
    BufferedWriter bwDetails_noflag = null;
    final String flagFilePath = CommandValue.outputPath + "comphet.csv";
    final String noFlagFilePath = CommandValue.outputPath + "comphet_noflag.csv";
//    BufferedWriter bwFlagSummary = null;
    String[] FLAG = {
        "compound heterozygote", // 0
        "possibly compound heterozygote", // 1
        "no flag" //2
    };
    boolean hasMultiVariants;

    @Override
    public void initOutput() {
        try {
            bwDetails = new BufferedWriter(new FileWriter(flagFilePath));
            bwDetails.write(CompHetOutput.title);
            bwDetails.newLine();

            if (CommandValue.isIncludeNoflag) {
                bwDetails_noflag = new BufferedWriter(new FileWriter(noFlagFilePath));
                bwDetails_noflag.write(CompHetOutput.title);
                bwDetails_noflag.newLine();
            }

            //        String geneSummary = "Variant Type, Total number of trio/families analyzed,Gene,"
            //                + "Number of cases with a 'functional' variant in this gene,"
            //                + "Contributing variants,Family IDs\n";
            //        tmpFile = CommandValue.outputPath + "comphet_summary.csv";
            //        bwFlagSummary = new BufferedWriter(new FileWriter(tmpFile));
            //        bwFlagSummary.write(geneSummary);
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
            bwDetails.flush();
            bwDetails.close();

            if (CommandValue.isIncludeNoflag) {
                bwDetails_noflag.flush();
                bwDetails_noflag.close();
            }
            //        bwFlagSummary.flush();
            //        bwFlagSummary.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
//        generatePvaluesQQPlot();
    }

    @Override
    public void beforeProcessDatabaseData() {
        TrioManager.initList();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CompHetOutput output = new CompHetOutput(calledVar);

            output.countSampleGenoCov();

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                output.deleteParentGeno(trio);

                output.calculate();

                if (output.isValid()) {

                    int geno = output.getCalledVariant().getGenotype(trio.getChildIndex());

                    if (output.isQualifiedGeno(geno)) {
                        outputList.add((CompHetOutput) output.clone());
                    }
                }

                output.addParentGeno(trio);
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
                LogManager.writeAndPrint("Analyzing qualified variants in gene ("
                        + list.get(0).getCalledVariant().getGeneName() + ")");

                doOutput(list);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initGeneVariantList() {
        ArrayList<CompHetOutput> geneOutputList = null;

        for (CompHetOutput output : outputList) {
            if (!currentGeneList.contains(output.getCalledVariant().getGeneName())) {
                currentGeneList.add(output.getCalledVariant().getGeneName());

                geneOutputList = new ArrayList<CompHetOutput>();
                geneOutputList.add(output);
                geneListVector.add(geneOutputList);
            } else {
                geneOutputList.add(output);
            }
        }
    }

    private void checkHasMultiVariants(ArrayList<CompHetOutput> geneOutputList,
            CompHetOutput output1, CompHetOutput output2) {
        if (!hasMultiVariants) {
            // check how many qualified variants in the gene
            int analyzedRecords = 0;
            int outputSize = geneOutputList.size();

            CompHetOutput output11, output22;
            int geno11, geno22;

            for (int x = 0; x < outputSize - 1; x++) {
                output11 = geneOutputList.get(x);
                for (int y = x + 1; y < outputSize; y++) {
                    output22 = geneOutputList.get(y);

                    geno11 = output11.getCalledVariant().getGenotype(output1.childIndex);
                    geno22 = output22.getCalledVariant().getGenotype(output2.childIndex);
                    if (output11.isQualifiedGeno(geno11)
                            && output22.isQualifiedGeno(geno22)) {

                        analyzedRecords++;

                        if (analyzedRecords > 1) {
                            hasMultiVariants = true;
                            break;
                        }
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
    private double[] getCoOccurrenceFreq(CompHetOutput output1, CompHetOutput output2) {
        double[] freq = new double[2];

        int quanlifiedCaseCount = 0, qualifiedCtrlCount = 0;
        int totalCaseCount = 0, totalCtrlCount = 0;

        for (Sample sample : SampleManager.getList()) {
            if (sample.getName().equals(output1.fatherName)
                    || sample.getName().equals(output1.motherName)) // ignore parents trio
            {
                continue;
            }

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

        freq[Index.CTRL] = FormatManager.devide(qualifiedCtrlCount, totalCtrlCount);
        freq[Index.CASE] = FormatManager.devide(quanlifiedCaseCount, totalCaseCount);

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

    private String getCompHetStatus(
            int cGeno1, int cCov1, int mGeno1,
            int mCov1, int fGeno1, int fCov1,
            int cGeno2, int cCov2, int mGeno2,
            int mCov2, int fGeno2, int fCov2) {

        int minCov = CommandValue.minCoverage;

        if ((fGeno1 <= 0 && mGeno1 <= 0)
                || (fGeno2 <= 0 && mGeno2 <= 0)) {
            return FLAG[2];
        } else if ((cGeno1 == fGeno1 && cGeno2 == fGeno2)
                || (cGeno1 == mGeno1 && cGeno2 == mGeno2)) {
            return FLAG[2];
        } else if ((fGeno1 == 2 && fCov1 >= minCov)
                || (mGeno1 == 2 && mCov1 >= minCov)
                || (fGeno2 == 2 && fCov2 >= minCov)
                || ((mGeno2 == 2 && mCov2 >= minCov))) {
            return FLAG[2];
        } else if ((cGeno1 == 2 && cCov1 >= minCov)
                || (cGeno2 == 2 && cCov2 >= minCov)) {
            return FLAG[2];
        } else if ((cGeno1 == 2 && cCov1 < minCov)
                || (cGeno2 == 2 && cCov2 < minCov)) {
            return FLAG[1];
        } else if ((fGeno1 == 0 && fCov1 < minCov)
                || (mGeno1 == 0 && mCov1 < minCov)
                || (fGeno2 == 0 && fCov2 < minCov)
                || (mGeno2 == 0 && mCov2 < minCov)) {
            return FLAG[1];
        } else if ((fGeno1 == 2 && fCov1 < minCov)
                || (mGeno1 == 2 && mCov1 < minCov)
                || (fGeno2 == 2 && fCov2 < minCov)
                || (mGeno2 == 2 && mCov2 < minCov)) {
            return FLAG[1];
        } else if ((cGeno1 == fGeno1 && cGeno2 == mGeno2)
                || (cGeno1 == mGeno1 && cGeno2 == fGeno2)) {
            return FLAG[0];
        }

        return "";
    }

    private void doOutput(ArrayList<CompHetOutput> geneOutputList) {
        StringBuilder sb = new StringBuilder();

        try {
            hasMultiVariants = false;

            int outputSize = geneOutputList.size();

            CompHetOutput output1, output2;

            String flag, id;

            double[] coFreq;

            for (int i = 0; i < outputSize - 1; i++) {
                output1 = geneOutputList.get(i);
                for (int j = i + 1; j < outputSize; j++) {
                    output2 = geneOutputList.get(j);

                    if (!output1.getCalledVariant().getVariantIdStr().equals(
                            output2.getCalledVariant().getVariantIdStr())
                            && output1.childName.equals(output2.childName)) {
                        id = output1.familyId + output1.getCalledVariant().getVariantIdStr()
                                + output2.getCalledVariant().getVariantIdStr();

                        if (!uniqueId.contains(id)) {
                            uniqueId.add(id);

                            flag = getCompHetStatus(
                                    output1.cGeno, output1.cSamtoolsRawCoverage,
                                    output1.mGeno, output1.mSamtoolsRawCoverage,
                                    output1.fGeno, output1.fSamtoolsRawCoverage,
                                    output2.cGeno, output2.cSamtoolsRawCoverage,
                                    output2.mGeno, output2.mSamtoolsRawCoverage,
                                    output2.fGeno, output2.fSamtoolsRawCoverage);

                            if (!flag.isEmpty()) {
                                if (flag.equals(FLAG[0]) || flag.equals(FLAG[1])) {
                                    checkHasMultiVariants(geneOutputList, output1, output2);
                                }

                                coFreq = getCoOccurrenceFreq(output1, output2);

                                if (QualityManager.isCombFreqValid(coFreq[Index.CTRL])) {
                                    sb.append(output1.familyId).append(",");
                                    sb.append(output1.childName).append(",");
                                    sb.append(output1.childType).append(",");
                                    sb.append(output1.motherName).append(",");
                                    sb.append(output1.fatherName).append(",");
                                    sb.append("'").append(output1.getCalledVariant().getGeneName()).append("'").append(",");
                                    sb.append(IntolerantScoreManager.getValues(output1.getCalledVariant().getGeneName())).append(",");
                                    sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.getCalledVariant().getGeneName()))).append(",");
                                    sb.append(flag).append(",");
                                    sb.append(hasMultiVariants).append(",");
                                    sb.append(FormatManager.getDouble(coFreq[Index.CASE])).append(",");
                                    sb.append(FormatManager.getDouble(coFreq[Index.CTRL])).append(",");

                                    sb.append(output1.toString()).append(",");
                                    sb.append(output2.toString());

                                    if (flag.equals(FLAG[0]) || flag.equals(FLAG[1])) {
                                        bwDetails.write(sb.toString());
                                        bwDetails.newLine();
                                    } else if (CommandValue.isIncludeNoflag) {
                                        bwDetails_noflag.write(sb.toString());
                                        bwDetails_noflag.newLine();
                                    }

                                    sb.setLength(0);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        uniqueId.clear();
    }

    private void clearList() {
        outputList.clear();
        geneListVector.clear();
        currentGeneList.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio compound heterozygosity function...";
    }
}
