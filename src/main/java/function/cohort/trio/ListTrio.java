package function.cohort.trio;

import function.annotation.base.EffectManager;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
import function.cohort.base.CalledVariant;
import function.cohort.base.AnalysisBase4CalledVar;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import static function.cohort.trio.TrioManager.COMP_HET_FLAG;
import function.variant.base.Output;
import function.variant.base.Region;
import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import global.Data;
import global.Index;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.LogManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import utils.DBManager;
import utils.FormatManager;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListTrio extends AnalysisBase4CalledVar {

    BufferedWriter bwTrioGenotype = null;
    final String trioGenotypeFilePath = CommonCommand.outputPath + "trio_genotypes.csv";

    BufferedWriter bwTrioGenotypeNoFlag = null;
    final String trioGenotypeFilePathNoFlag = CommonCommand.outputPath + "trio_genotypes_noflag.csv";
    
    BufferedWriter bwTrioGeneName = null;
    final String trioGeneNamesFilePath = CommonCommand.outputPath + "trio_gene_names.csv"; 
    
    HashMap<String, List<TrioOutput>> geneVariantListMap = new HashMap<>();
    // avoid output duplicate carrier (comp var & single var)
    HashSet<String> outputCarrierSet = new HashSet<>();

    
    HashSet<String> geneSet = new HashSet<>();
    private static final String phenolyzerOutputPath = CommonCommand.realOutputPath + "/phenolyzer";
            
    private static final String phenolyzerRankFilePath = phenolyzerOutputPath + ".annotated_gene_list";
   
    public HashMap<String, Integer> phenolyzerRankMap = new HashMap<>();
       
    @Override
    public void initOutput() {
        try {
            bwTrioGenotype = new BufferedWriter(new FileWriter(trioGenotypeFilePath));
            bwTrioGenotype.write(TrioManager.getHeader());
            bwTrioGenotype.newLine();

            bwTrioGenotypeNoFlag = new BufferedWriter(new FileWriter(trioGenotypeFilePathNoFlag));
            bwTrioGenotypeNoFlag.write(TrioManager.getHeader());
            bwTrioGenotypeNoFlag.newLine();
            
            if(TrioCommand.isPhenolyzer){
                bwTrioGeneName = new BufferedWriter(new FileWriter(trioGeneNamesFilePath));
                bwTrioGeneName.newLine();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doOutput() {
        outputDenovoAndCompHets();

        clearList();
    }

    @Override
    public void closeOutput() {
        try {
            bwTrioGenotype.flush();
            bwTrioGenotype.close();

            bwTrioGenotypeNoFlag.flush();
            bwTrioGenotypeNoFlag.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
        Output.logTierVariantCount();
    }

    @Override
    public void beforeProcessDatabaseData() {
        // init unique gene list file 
            // Copy get annotation from analyssiBase4Variant
        // select distinct gene from variant_chr1 ... (for loop)
        // run Phenolyzer
        // parse Phenolyzer output and keep gene score in HashMap<String,Integer>
        // add new field into output "Phenolyzer Rank"
    
        if (TrioCommand.isPhenolyzer) {
            try {
                processGeneList();
                for(String geneName : geneSet){
                    if (geneName != null){
                        outputGeneList(geneName);
                    }
                } 
                bwTrioGeneName.flush();
                bwTrioGeneName.close();
            } catch (Exception ex) {
                ErrorManager.send(ex);
            }
            
            doPhenolyzer();
            parsePhenolyzerOutput();
        }
        TrioManager.init();
       
        
    }
    
    private void outputGeneList(String geneName){   
        try {
            bwTrioGeneName.write(geneName);
            bwTrioGeneName.newLine();
        } catch(Exception e){
            ErrorManager.send(e);
        }
    }
    
    private void parsePhenolyzerOutput() {
        try{
            phenolyzerRankMap = (HashMap<String, Integer>) Files.readAllLines(Paths.get(phenolyzerRankFilePath))
                    .stream()
                    .skip(1)
                    .collect(Collectors.toMap(
                            data -> String.valueOf(data.split("\t")[1]), 
                            data -> Integer.parseInt(data.split("\t")[0])));
            
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    
    public void processGeneList() throws Exception {
        for (String chrStr: RegionManager.getChrList()) {

            // when --case-only used and case num > 0
            VariantManager.initCaseVariantTable(chrStr);

            rset = getAnnotationList(chrStr);
           
            while (rset.next()) {
                String nextGene = rset.getString("gene"); 
                geneSet.add(nextGene);
            }
            
            rset.close();
            
            // clear temp case variant able
            VariantManager.dropCaseVariantTable(chrStr); 
        }

    }
    
    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            TrioOutput output = new TrioOutput(calledVar);

            addVariantToGeneList(output);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    protected static ResultSet getAnnotationList(String chrStr) throws SQLException {
        String sql = "SELECT DISTINCT gene "
                + "FROM variant_chr" + chrStr + " ";

        // case only filter - add tmp table
        if (CohortLevelFilterCommand.isCaseOnlyValid2CreateTempTable()) {
            sql += ", tmp_case_variant_id_chr" + chrStr + " ";
            sql += "WHERE variant_id = case_variant_id ";
        }

        return DBManager.executeConcurReadOnlyQuery(sql);
    }

    
    private void addVariantToGeneList(TrioOutput output) {
        List<TrioOutput> geneOutputList
                = geneVariantListMap.get(output.getCalledVariant().getGeneName());

        if (geneOutputList == null) {
            geneOutputList = new ArrayList<>();
            
            geneOutputList.add(output);
            geneVariantListMap.put(output.getCalledVariant().getGeneName(), geneOutputList);
        } else {
            geneOutputList.add(output);
        }
    }

    private void outputDenovoAndCompHets() {
        if (geneVariantListMap.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, List<TrioOutput>> entry : geneVariantListMap.entrySet()) {
                LogManager.writeAndPrint("Processing variants in gene:" + entry.getKey());

                doOutput(entry.getValue());
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void doOutput(List<TrioOutput> geneOutputList) {
        try {
            outputCarrierSet.clear();

            for (int i = 0; i < geneOutputList.size(); i++) {
                TrioOutput output1 = geneOutputList.get(i);

                for (Trio trio : TrioManager.getList()) {
                    output1.initTrioData(trio);

                    if (output1.isQualifiedGeno(output1.cGeno)) {
                        output1.initDenovoFlag(trio.getChild());
                        output1.initTierFlag4SingleVar();
                        output1.initACMG();
                        output1.initPhenolyzerRank(phenolyzerRankMap);
                        for (int j = i + 1; j < geneOutputList.size(); j++) {
                            TrioOutput output2 = geneOutputList.get(j);
                            output2.initTrioData(trio);

                            if (output2.isQualifiedGeno(output2.cGeno)) {
                                // init variant denovo flag for finding potential comp het
                                output2.initDenovoFlag(trio.getChild());
                                output2.initTierFlag4SingleVar();
                                output2.initACMG();
                                output2.initPhenolyzerRank(phenolyzerRankMap);
                                outputCompHet(output1, output2);
                            }
                        }

                        outputDenovoOrHomOrInheritedVar(output1);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void outputDenovoOrHomOrInheritedVar(TrioOutput output) throws Exception {
        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());

        if (outputCarrierSet.contains(carrierIDSB.toString())) {
            return;
        }

        output.countSingleVar();

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getName());
        sj.add(output.child.getFamilyId());
        sj.add(output.motherName);
        sj.add(output.fatherName);
        sj.add(output.getSingleVariantPrioritization());
        sj.add(Data.STRING_NA);
        sj.add(output.getCalledVariant().getATAVLINK());
        sj.add(output.getCalledVariant().getGeneLink());
        sj.add("'" + output.getCalledVariant().getGeneName() + "'");
        sj.add(output.getCalledVariant().getVariantIdStr());
        sj.add(output.getCalledVariant().getImpact());
        sj.add(output.getCalledVariant().getEffect());
        sj.add(output.getCalledVariant().getCanonicalEffect());
        sj.add(output.denovoFlag);
        sj.add(output.getInheritedFrom().name());
        sj.add(FormatManager.getInteger(
                output.denovoFlag.contains("DE NOVO")
                && output.isHotZone == 1
                && output.getCalledVariant().getMgi().split(",")[1].equals("1") ? 1 : 0));
        sj.add(FormatManager.getInteger(output.isClinGenVarLoF));
        sj.add(FormatManager.getInteger(output.isLoFdepletedpLI));
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(Data.STRING_NA);
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());
        sj.add(FormatManager.appendDoubleQuote(output.getSummary()));

        if (output.isFlag() && output.getCalledVariant().isNotSynonymousAndNotSliceOrHighTraP()) {
            bwTrioGenotype.write(sj.toString());
            bwTrioGenotype.newLine();
        } else {
            bwTrioGenotypeNoFlag.write(sj.toString());
            bwTrioGenotypeNoFlag.newLine();
        }
        
        output.clearSingleVariantPrioritization();
    }

    private void outputCompHet(TrioOutput output1, TrioOutput output2) throws Exception {
        String compHetFlag = getTrioCompHetFlag(output1, output2);

        if (!compHetFlag.equals(COMP_HET_FLAG[2])) { // no flag
            doCompHetOutput(output1, output2);
        }
    }

    private String getTrioCompHetFlag(TrioOutput output1, TrioOutput output2) {
        String flag = TrioManager.getCompHetFlag(
                output1.cGeno, output1.mGeno, output1.fGeno,
                output2.cGeno, output2.mGeno, output2.fGeno);

        flag = TrioManager.getCompHetFlagByDenovo(flag,
                output1.cGeno, output1.mGeno, output1.fGeno, output1.denovoFlag,
                output2.cGeno, output2.mGeno, output2.fGeno, output2.denovoFlag);

        return flag;
    }

    private void doCompHetOutput(TrioOutput output1, TrioOutput output2) throws Exception {
        float[] coFreq = TrioManager.getCoOccurrenceFreq(output1, output2);

        StringBuilder compHetVarSB = new StringBuilder();
        compHetVarSB.append(output1.getCalledVariant().getVariantIdStr());
        compHetVarSB.append("&");
        compHetVarSB.append(output2.getCalledVariant().getVariantIdStr());

        String compHetVar1 = compHetVarSB.toString() + "#1";
        String compHetVar2 = compHetVarSB.toString() + "#2";

        // apply tier rules
        byte tierFlag4CompVar = Data.BYTE_NA;

        // init CH variant prioritization
        String chVariantPrioritization = Data.STRING_NA;

        if (output1.getCalledVariant().isNotSynonymousAndNotSliceOrHighTraP()
                && output2.getCalledVariant().isNotSynonymousAndNotSliceOrHighTraP()) {
            // tier 1
            if (output1.isParentsNotHom() && output2.isParentsNotHom()
                    // co-occurance freq in controls is 0
                    && coFreq[Index.CTRL] == 0
                    // for both variants, genotype is not observed in Hemizygous or Homozygous from IGM default controls and gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isNotObservedInHomAmongControl() && output2.getCalledVariant().isNotObservedInHomAmongControl()
                    // for both variants, max 0.5% AF to IGM default controls and gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isControlAFValid() && output2.getCalledVariant().isControlAFValid()) {
                tierFlag4CompVar = 1;
                chVariantPrioritization = "01_TIER1";
                Output.tier1CompoundVarCount++;
            } else if ( // tier 2
                    // if one of the variant meets tier 2 inclusion criteria
                    (output1.getCalledVariant().isMetTier2InclusionCriteria(output1.cCarrier)
                    || output2.getCalledVariant().isMetTier2InclusionCriteria(output2.cCarrier))
                    // for both variants, less than 10 homozygous observed from IGM default controls + gnomAD (WES & WGS) controls
                    && output1.getCalledVariant().isNHomFromControlsValid(10) && output2.getCalledVariant().isNHomFromControlsValid(10)) {
                tierFlag4CompVar = 2;
                chVariantPrioritization = "02_TIER2";
                Output.tier2CompoundVarCount++;
            }
        }

        initACMGPM3orBP2(output1, output2);

        // single var (tier 1 or 2 or LoF or KV) and isNotSynonymousAndNotSliceOrHighTraP
        boolean hasSingleVarFlagged
                = (output1.isFlag() && output1.getCalledVariant().isNotSynonymousAndNotSliceOrHighTraP())
                || (output2.isFlag() && output2.getCalledVariant().isNotSynonymousAndNotSliceOrHighTraP());

        doCompHetOutput(tierFlag4CompVar, chVariantPrioritization, output1, coFreq, compHetVar1, hasSingleVarFlagged);
        doCompHetOutput(tierFlag4CompVar, chVariantPrioritization, output2, coFreq, compHetVar2, hasSingleVarFlagged);
    }

    private void initACMGPM3orBP2(TrioOutput output1, TrioOutput output2) {
        boolean isV1KVandV2NonKV = output1.getCalledVariant().getKnownVar().isKnownVariant()
                && !output2.getCalledVariant().getKnownVar().isKnownVariant();

        boolean isV2KVandV1NonKV = output2.getCalledVariant().getKnownVar().isKnownVariant()
                && !output1.getCalledVariant().getKnownVar().isKnownVariant();

        // OMIM Recessive and comphet and one of the var is KV
        // Only non-KV variant will be labeled PM3
        if (output1.getCalledVariant().getKnownVar().isOMIMRecessive()) {
            output2.isPM3 = isV1KVandV2NonKV;
            output1.isPM3 = isV2KVandV1NonKV;
        }

        // OMIM Dominant and comphet and one of the var is KV
        // Only non-KV variant will be labeled BP2 
        if (output1.getCalledVariant().getKnownVar().isOMIMDominant()) {
            output2.isBP2 = isV1KVandV2NonKV;
            output1.isBP2 = isV2KVandV1NonKV;
        }

        if (output1.isPM3 || output1.isBP2) {
            output1.initACMG();
        }

        if (output2.isPM3 || output2.isBP2) {
            output2.initACMG();
        }
    }

    private void doCompHetOutput(byte tierFlag4CompVar, String chVariantPrioritization, TrioOutput output, float[] coFreq,
            String compHetVar, boolean hasSingleVarFlagged) throws Exception {
        output.initClinGenVarLoF4CHET();
        output.initLoFdepletedpLI4CHET();

        StringBuilder carrierIDSB = new StringBuilder();
        carrierIDSB.append(output.getCalledVariant().variantId);
        carrierIDSB.append("-");
        carrierIDSB.append(output.cCarrier.getSampleId());

        output.countSingleVar();
        outputCarrierSet.add(carrierIDSB.toString());

        StringJoiner sj = new StringJoiner(",");
        sj.add(output.child.getName());
        sj.add(output.child.getFamilyId());
        sj.add(output.motherName);
        sj.add(output.fatherName);
        sj.add(output.getSingleVariantPrioritization());
        sj.add(chVariantPrioritization);
        sj.add(output.getCalledVariant().getATAVLINK());
        sj.add(output.getCalledVariant().getGeneLink());
        sj.add("'" + output.getCalledVariant().getGeneName() + "'");
        sj.add(output.getCalledVariant().getVariantIdStr());
        sj.add(output.getCalledVariant().getImpact());
        sj.add(output.getCalledVariant().getEffect());
        sj.add(output.getCalledVariant().getCanonicalEffect());
        sj.add(output.denovoFlag);
        sj.add(output.getInheritedFrom().name());
        sj.add(FormatManager.getInteger(
                output.denovoFlag.contains("DE NOVO")
                && output.isHotZone == 1
                && output.getCalledVariant().getMgi().split(",")[1].equals("1") ? 1 : 0));
        sj.add(FormatManager.getInteger(output.isClinGenVarLoF));
        sj.add(FormatManager.getInteger(output.isLoFdepletedpLI));
        sj.add(compHetVar);
        sj.add(FormatManager.getFloat(coFreq[Index.CTRL]));
        sj.add(FormatManager.getByte(tierFlag4CompVar));
        sj.add(FormatManager.getByte(output.getTierFlag4SingleVar()));
        sj.add(output.toString());
        sj.add(FormatManager.appendDoubleQuote(output.getSummary()));

        if (tierFlag4CompVar != Data.BYTE_NA || hasSingleVarFlagged) {
            bwTrioGenotype.write(sj.toString());
            bwTrioGenotype.newLine();
        } else {
            bwTrioGenotypeNoFlag.write(sj.toString());
            bwTrioGenotypeNoFlag.newLine();
        }
        
        output.clearSingleVariantPrioritization();
    }

    private void doPhenolyzer() {
        String cmd = ThirdPartyToolManager.PERL
                + " " + ThirdPartyToolManager.PHENOLYZER
                + " -ph -f " + TrioCommand.phenolyzerPhenotypePath
                + " --gene " + trioGeneNamesFilePath
                + " --out " + phenolyzerOutputPath;
        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    private void clearList() {
        geneVariantListMap.clear();
    }

    @Override
    public String toString() {
        return "It is running a list trio function...";
    }
}
