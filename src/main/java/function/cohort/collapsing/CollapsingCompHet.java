package function.cohort.collapsing;

import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class CollapsingCompHet extends CollapsingBase {

    BufferedWriter bwCompHet = null;
    final String comphetFilePath = CommonCommand.outputPath + "comphet.csv";

    HashSet<Integer> variantIdSet = new HashSet<>();
    HashMap<String, List<CompHetOutput>> geneVariantListMap = new HashMap<>();

    @Override
    public void initOutput() {
        try {
            super.initOutput();

            bwCompHet = new BufferedWriter(new FileWriter(comphetFilePath));
            bwCompHet.write(CompHetOutput.getHeader());
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
    public void doAfterCloseOutput() {
        super.doAfterCloseOutput();

        if (CollapsingCommand.isMannWhitneyTest) {
            ThirdPartyToolManager.runMannWhitneyTest(comphetFilePath);
        }
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            CompHetOutput output = new CompHetOutput(calledVar);

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void addVariantToGeneList(CompHetOutput output) {
        List<CompHetOutput> geneOutputList
                = geneVariantListMap.get(output.getCalledVariant().getGeneName());

        if (geneOutputList == null) {
            geneOutputList = new ArrayList<>();
            geneOutputList.add(output);
            geneVariantListMap.put(output.getCalledVariant().getGeneName(), geneOutputList);
        } else {
            geneOutputList.add(output);
        }
    }

    private void listCompHets() {
        if (geneVariantListMap.isEmpty()) {
            return;
        }

        try {
            for (Entry<String, List<CompHetOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                CollapsingSummary summary = summaryMap.get(entry.getKey());

                if (summary == null) {
                    summary = new CollapsingGeneSummary(entry.getKey());
                    summaryMap.put(entry.getKey(), summary);
                }

                doOutput(entry.getValue(), summary);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<CompHetOutput> geneOutputList, CollapsingSummary summary) {
        try {
            int outputSize = geneOutputList.size();

            CompHetOutput output1, output2;

            for (Sample sample : SampleManager.getList()) {
                for (int i = 0; i < outputSize; i++) {
                    output1 = geneOutputList.get(i);

                    byte geno1 = output1.getCalledVariant().getGT(sample.getIndex());

                    if (output1.isQualifiedGeno(geno1)) {
                        output1.calculateLooAF(sample);

                        if (output1.isMaxLooAFValid()) {
                            if (isOutputValid(output1, geno1, sample, summary)) {
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

    /*
        output only if genotype is Hom Var, otherwise continue finding another variant with Het/Hom
     */
    private boolean isOutputValid(CompHetOutput output1, byte geno,
            Sample sample, CollapsingSummary summary) throws Exception {
        if (geno == Index.HOM) {
            summary.updateSampleVariantCount4CompHet(sample.getIndex());

            updateSummaryVariantCount(output1, summary);

            StringJoiner sj = new StringJoiner(",");
            sj.add(sample.getFamilyId());
            sj.merge(output1.getStringJoiner(sample));

            bwCompHet.write(sj.toString());
            bwCompHet.newLine();

            return true;
        }

        return false;
    }

    private void checkOutputValid(CompHetOutput output1, CompHetOutput output2,
            Sample sample, CollapsingSummary summary) throws Exception {
        CalledVariant var1 = output1.getCalledVariant();
        CalledVariant var2 = output2.getCalledVariant();

        if (var1.getVariantId() != var2.getVariantId()
                && isMinCompHetVarDistanceValid(var1, var2)
                && !isCompHetPIDVariantIdInvalid(var1, var2, sample)
                && !isCompHetHPVariantIdInvalid(var1, var2, sample)) {
            byte geno2 = output2.getCalledVariant().getGT(sample.getIndex());

            if (output2.isQualifiedGeno(geno2)) {
                output2.calculateLooAF(sample);

                if (output2.isMaxLooAFValid()) {
                    summary.updateSampleVariantCount4CompHet(sample.getIndex());

                    updateSummaryVariantCount(output1, summary);
                    updateSummaryVariantCount(output2, summary);

                    StringJoiner sj = new StringJoiner(",");
                    sj.add(sample.getFamilyId());
                    sj.merge(output1.getStringJoiner(sample));
                    sj.merge(output2.getStringJoiner(sample));

                    bwCompHet.write(sj.toString());
                    bwCompHet.newLine();
                }
            }
        }
    }

    private boolean isMinCompHetVarDistanceValid(CalledVariant var1, CalledVariant var2) {
        int varDistance = Math.abs(var1.getStartPosition() - var2.getStartPosition());

        return CollapsingCommand.isMinCompHetVarDistanceValid(varDistance);
    }

    private boolean isCompHetPIDVariantIdInvalid(CalledVariant var1, CalledVariant var2, Sample sample) {
        if (!CollapsingCommand.isExcludeCompHetPIDVariant) {
            return false;
        }

        Carrier carrier1 = var1.getCarrier(sample.getId());
        Carrier carrier2 = var2.getCarrier(sample.getId());

        int pidVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getPIDVariantId();
        int pidVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getPIDVariantId();

        return CollapsingCommand.isCompHetPIDVariantIdInvalid(
                var1.getVariantId(), var2.getVariantId(),
                pidVariantId1, pidVariantId2);
    }

    private boolean isCompHetHPVariantIdInvalid(CalledVariant var1, CalledVariant var2, Sample sample) {
        if (!CollapsingCommand.isExcludeCompHetHPVariant) {
            return false;
        }

        Carrier carrier1 = var1.getCarrier(sample.getId());
        Carrier carrier2 = var2.getCarrier(sample.getId());

        int hpVariantId1 = carrier1 == null ? Data.INTEGER_NA : carrier1.getHPVariantId();
        int hpVariantId2 = carrier2 == null ? Data.INTEGER_NA : carrier2.getHPVariantId();

        return CollapsingCommand.isCompHetHPVariantIdInvalid(
                var1.getVariantId(), var2.getVariantId(),
                hpVariantId1, hpVariantId2);
    }

    private void updateSummaryVariantCount(CompHetOutput output, CollapsingSummary summary) {
        if (!variantIdSet.contains(output.getCalledVariant().getVariantId())) {
            summary.updateVariantCount(output.getCalledVariant().isSnv());
            variantIdSet.add(output.getCalledVariant().getVariantId());
        }
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "Start running collapsing compound heterozygosity function";
    }
}
