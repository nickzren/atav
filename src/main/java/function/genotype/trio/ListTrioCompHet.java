package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import function.genotype.base.AnalysisBase4CalledVar;
import global.Index;
import function.annotation.base.GeneManager;
import function.genotype.base.GenotypeLevelFilterCommand;
import function.genotype.base.SampleManager;
import global.Data;
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
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListTrioCompHet extends AnalysisBase4CalledVar {

    ArrayList<CompHetOutput> outputList = new ArrayList<>();
    ArrayList<ArrayList<CompHetOutput>> geneListVector = new ArrayList<>();
    HashSet<String> currentGeneList = new HashSet<>();
    BufferedWriter bwCompHet = null;
    BufferedWriter bwCompHetNoFlag = null;
    final String compHetFilePath = CommonCommand.outputPath + "comphet.csv";
    final String compHetNoFlagFilePath = CommonCommand.outputPath + "comphet_noflag.csv";
    public static final String[] FLAG = {
        "compound heterozygote", // 0
        "possibly compound heterozygote", // 1
        "no flag" //2
    };

    @Override
    public void initOutput() {
        try {
            bwCompHet = new BufferedWriter(new FileWriter(compHetFilePath));
            bwCompHet.write(CompHetOutput.getTitle());
            bwCompHet.newLine();

            if (TrioCommand.isIncludeNoflag) {
                bwCompHetNoFlag = new BufferedWriter(new FileWriter(compHetNoFlagFilePath));
                bwCompHetNoFlag.write(CompHetOutput.getTitle());
                bwCompHetNoFlag.newLine();
            }
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
            bwCompHet.flush();
            bwCompHet.close();

            if (TrioCommand.isIncludeNoflag) {
                bwCompHetNoFlag.flush();
                bwCompHetNoFlag.close();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        if (TrioCommand.isRunTier) {
            ThirdPartyToolManager.runTrioCompHetTier(compHetFilePath);
        }
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

            output.countSampleGeno();

            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);

                output.deleteParentGeno(trio);

                output.calculate();

                if (output.isValid()) {

                    int geno = output.getCalledVariant().getGenotype(trio.getChild().getIndex());

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

                geneOutputList = new ArrayList<>();
                geneOutputList.add(output);
                geneListVector.add(geneOutputList);
            } else {
                geneOutputList.add(output);
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

        freq[Index.CTRL] = MathManager.devide(qualifiedCtrlCount, totalCtrlCount);
        freq[Index.CASE] = MathManager.devide(quanlifiedCaseCount, totalCaseCount);

        return freq;
    }

    private boolean isCoQualifiedGeno(CompHetOutput output1,
            CompHetOutput output2, int index) {
        int geno1 = output1.getCalledVariant().getGenotype(index);
        int geno2 = output2.getCalledVariant().getGenotype(index);

        return output1.isQualifiedGeno(geno1)
                && output2.isQualifiedGeno(geno2);
    }

    public static String getCompHetStatus(
            int cGeno1, int cCov1,
            int mGeno1, int mCov1,
            int fGeno1, int fCov1,
            boolean isMinorRef1,
            int cGeno2, int cCov2,
            int mGeno2, int mCov2,
            int fGeno2, int fCov2,
            boolean isMinorRef2) {
        int minCov = GenotypeLevelFilterCommand.minCoverage;

        // to limit confusion, we swap genotypes 0<->2 if isMinorRef
        // i.e. hom ref<->hom variant
        // that enables us to ignore the isMinorRef aspect thereafter
        if (isMinorRef1) {
            cGeno1 = swapGenotypes(cGeno1);
            fGeno1 = swapGenotypes(fGeno1);
            mGeno1 = swapGenotypes(mGeno1);
        }
        if (isMinorRef2) {
            cGeno2 = swapGenotypes(cGeno2);
            fGeno2 = swapGenotypes(fGeno2);
            mGeno2 = swapGenotypes(mGeno2);
        }
        // exclude if the child is missing any call
        if (cGeno1 == Data.NA || cGeno2 == Data.NA) {
            return FLAG[2];
        }
        // exclude if the child is homozygous, wild type or variant, for either variant
        if (((cGeno1 == Index.REF || cGeno1 == Index.HOM) && cCov1 >= minCov)
                || ((cGeno2 == Index.REF || cGeno2 == Index.HOM) && cCov2 >= minCov)) {
            return FLAG[2];
        }
        if ((fGeno1 == Data.NA && mGeno1 == Data.NA) || (fGeno2 == Data.NA && mGeno2 == Data.NA)) {
            // if both parents are missing the same call, exclude this candidate
            return FLAG[2];
        }
        if ((fGeno1 == Data.NA && fGeno2 == Data.NA) || (mGeno1 == Data.NA && mGeno2 == Data.NA)) {
            // if one parent is missing both calls, exclude this candidate
            return FLAG[2];
        }
        // if any parental call is hom at at least minCov depth, exclude
        if ((fGeno1 == Index.HOM && fCov1 >= minCov) || (fGeno2 == Index.HOM && fCov2 >= minCov)
                || (mGeno1 == Index.HOM && mCov1 >= minCov) || (mGeno2 == Index.HOM && mCov2 >= minCov)) {
            return FLAG[2];
        }
        // if either parent has both variants, exclude
        if (((fGeno1 == Index.HET || fGeno1 == Index.HOM) && (fGeno2 == Index.HET || fGeno2 == Index.HOM))
                || ((mGeno1 == Index.HET || mGeno1 == Index.HOM) && (mGeno2 == Index.HET || mGeno2 == Index.HOM))) {
            return FLAG[2];
        }
        // if either parent has neither variant, exclude
        if ((fGeno1 == Index.REF && fCov1 >= minCov && fGeno2 == Index.REF && fCov2 >= minCov)
                || (mGeno1 == Index.REF && mCov1 >= minCov && mGeno2 == Index.REF && mCov2 >= minCov)) {
            return FLAG[2];
        }
        // if both parents are wild type for the same variant, exclude
        if ((fGeno1 == Index.REF && fCov1 >= minCov && mGeno1 == Index.REF && mCov1 >= minCov)
                || (fGeno2 == Index.REF && fCov2 >= minCov && mGeno2 == Index.REF && mCov2 >= minCov)) {
            return FLAG[2];
        }
        // if both parents have the same variant, exclude
        if (((fGeno1 == Index.HET || fGeno1 == Index.HOM) && (mGeno1 == Index.HET || mGeno1 == Index.HOM))
                || ((fGeno2 == Index.HET || fGeno2 == Index.HOM) && (mGeno2 == Index.HET || mGeno2 == Index.HOM))) {
            return FLAG[2];
        }
        // we've excluded all that should be excluded - the possibilities are now that
        // the compound het is "Shared" or that it's "Possibly Shared", i.e. there's a
        // possibility the variants don't segregate properly but there wasn't sufficient
        // cause to exclude entirely
        if ((fGeno1 == Index.HET && fGeno2 == Index.REF && mGeno1 == Index.REF && mGeno2 == Index.HET)
                || (fGeno1 == Index.REF && fGeno2 == Index.HET && mGeno1 == Index.HET && mGeno2 == Index.REF)) {
            if (cGeno1 == Index.HET && cGeno2 == Index.HET) {
                return FLAG[0];
            } else {
                return FLAG[1];
            }
        } else {
            return FLAG[1];
        }
    }

    private static int swapGenotypes(
            int genotype) {
        switch (genotype) {
            case Index.REF:
                return Index.HOM;
            case Index.HOM:
                return Index.REF;
            default:
                return genotype;
        }
    }

    private void doOutput(ArrayList<CompHetOutput> geneOutputList) {
        StringBuilder sb = new StringBuilder();

        try {
            int outputSize = geneOutputList.size();

            CompHetOutput output1, output2;

            String flag;

            double[] coFreq;

            for (int i = 0; i < outputSize - 1; i++) {
                output1 = geneOutputList.get(i);
                for (int j = i + 1; j < outputSize; j++) {
                    output2 = geneOutputList.get(j);

                    if (output1.getCalledVariant().getVariantIdNegative4Indel()
                            != output2.getCalledVariant().getVariantIdNegative4Indel()
                            && output1.child.getId() == output2.child.getId()) {
                        flag = getCompHetStatus(
                                output1.cGeno, output1.cSamtoolsRawCoverage,
                                output1.mGeno, output1.mSamtoolsRawCoverage,
                                output1.fGeno, output1.fSamtoolsRawCoverage,
                                output1.isMinorRef(),
                                output2.cGeno, output2.cSamtoolsRawCoverage,
                                output2.mGeno, output2.mSamtoolsRawCoverage,
                                output2.fGeno, output2.fSamtoolsRawCoverage,
                                output2.isMinorRef());

                        if (!flag.isEmpty()) {
                            coFreq = getCoOccurrenceFreq(output1, output2);

                            if (TrioCommand.isCombFreqValid(coFreq[Index.CTRL])) {
                                sb.append(output1.child.getFamilyId()).append(",");
                                sb.append(output1.child.getName()).append(",");
                                sb.append(output1.motherName).append(",");
                                sb.append(output1.fatherName).append(",");
                                sb.append("'").append(output1.getCalledVariant().getGeneName()).append("'").append(",");
                                sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(output1.getCalledVariant().getGeneName()))).append(",");
                                sb.append(flag).append(",");
                                sb.append(FormatManager.getDouble(coFreq[Index.CASE])).append(",");
                                sb.append(FormatManager.getDouble(coFreq[Index.CTRL])).append(",");

                                sb.append(output1.toString());
                                sb.append(output2.toString());

                                if (flag.equals(FLAG[0]) || flag.equals(FLAG[1])) {
                                    bwCompHet.write(sb.toString());
                                    bwCompHet.newLine();
                                } else if (TrioCommand.isIncludeNoflag) {
                                    bwCompHetNoFlag.write(sb.toString());
                                    bwCompHetNoFlag.newLine();
                                }

                                sb.setLength(0);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void clearList() {
        outputList.clear();
        geneListVector.clear();
        currentGeneList.clear();
    }

    @Override
    public String toString() {
        return "Start running list trio compound heterozygosity function";
    }
}
