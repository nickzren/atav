package function.nondb.ppi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import utils.CommandValue;
import utils.ErrorManager;
import utils.LogManager;
import org.apache.commons.math3.random.RandomDataGenerator;
import utils.FormatManager;

/**
 *
 * @author quanli
 */
public class PPI {
 
    HashMap<String, ArrayList<String>> PPIMap = new HashMap<String, ArrayList<String>>();
    HashSet<String> GenesInPPI = new HashSet<String>();
    HashMap<Integer,HashSet<String>> ConnectionGeneMap = new HashMap<Integer,HashSet<String>>();
    HashMap<String,HashSet<String>> SampleGeneMap = new HashMap<String,HashSet<String>>();
    HashMap<String,Integer> GeneConnectionClassMap = new HashMap<String,Integer>();
    HashSet<String> GenesInGeno = new HashSet<String>();
    
    RandomDataGenerator random = new RandomDataGenerator();
    
    BufferedWriter bwPPI = null;
    final String ppiFilePath = CommandValue.outputPath + "ppi.csv";
    final String ppiSystemFile = "update_to_real_path";

    public void run() {
        try {
            initOutput();
            
            readPPI();
            readGeno();
            
            int sampleCount = 0;
            for (String sample: SampleGeneMap.keySet()) {
                sampleCount++;
                GeneSetStat Stat = new GeneSetStat(sample,SampleGeneMap.get(sample));
                Stat.init();
                int TotalExtemes = 0;
                for (int i = 0; i < CommandValue.ppiPermutaitons; i++) {
                    HashSet<String> PermutatedSet = Stat.nextPermutation();
                    int interactions = Stat.getTotalDirectConnections(PermutatedSet, false);
                    if (interactions > Stat.GeneSetInteractions) {
                        TotalExtemes++;
                    }
                }
                Stat.permutatedP = ((double) TotalExtemes) / CommandValue.ppiPermutaitons;
                StringBuilder str = new StringBuilder();
                str.append(Stat.SetName).append(",").append(Stat.NodesInSet).append(",");
                str.append(Stat.NodesInNetwork).append(",");
                str.append(Stat.TotalNetworkInteractions).append(",");
                str.append(Stat.GeneSetInteractions).append(",");
                str.append(FormatManager.getDouble(Stat.permutatedP)).append(",");
                
                //now for the edges
                if (Stat.EdgeList.isEmpty()) {
                    str.append("NA");
                } else {
                    for (String edge : Stat.EdgeList) {
                        str.append("|").append(edge);
                    }
                    str.append("|");
                }
                bwPPI.write(str.toString());
                bwPPI.newLine();
                LogManager.writeAndPrintNoNewLine(sample + ": " + sampleCount + " / " + SampleGeneMap.size());
            
            }
            
            closeOutput();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }
    
    
    private void readGeno() {
        File f = new File(CommandValue.ppiGenotypeFile);
        String lineStr = "";
        int lineNum = 0;
        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            int GeneColumn = -1;
            int SampleColumn = -1;
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.isEmpty()) {
                    continue;
                }
                lineNum++;
                String [] fields = lineStr.split(",");
                if (lineNum == 1) { //parse header
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].equalsIgnoreCase("Sample Name")) {
                            SampleColumn = i;
                        }
                        if (fields[i].equalsIgnoreCase("Gene Name")) {
                            GeneColumn = i;
                        }
                    }
                    if (SampleColumn < 0 || GeneColumn < 0) {
                        ErrorManager.print("Sample Name or Gene Name column not found");
                    }
                } else { //get one line of (sample,gene) pair
                    if (fields.length > SampleColumn && fields.length > GeneColumn) {
                        String gene = fields[GeneColumn];
                        String sample = fields[SampleColumn];
                        gene = gene.replaceAll("'", "");
                        sample = sample.replaceAll("'", "");
                        if (gene.length() > 0 && sample.length() > 0) {
                            if (!SampleGeneMap.containsKey(sample)) {
                                SampleGeneMap.put(sample,new HashSet<String>());
                            }
                            SampleGeneMap.get(sample).add(gene);
                            GenesInGeno.add(gene);
                        }
                    }
                }
            }
            
            int totalSampleGenePairs = 0;
            for (HashSet<String> geneset : SampleGeneMap.values()) {
                totalSampleGenePairs += geneset.size();
            }
            
            LogManager.writeAndPrint("Total number of samples = " + SampleGeneMap.size());
            LogManager.writeAndPrint("Total number of (sample,gene) pairs = " + totalSampleGenePairs);
            
            
        } catch (Exception e) {
            LogManager.writeAndPrint("\nError line ("
                    + lineNum + ") in genotype file: " + lineStr);
            ErrorManager.send(e);
        }
    }
    
    private void readPPI() {
        File f = new File(CommandValue.ppiFile);
        String lineStr = "";
        int lineNum = 0;

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.isEmpty()) {
                    continue;
                }
                lineNum++;
                if (lineNum>1) { //always skip the first line for column names
                    String [] fields = lineStr.split(",");
                    if (fields.length == 2) {
                        fields[0] = fields[0].replaceAll("'", "");
                            fields[1] = fields[1].replaceAll("'", "");
                        if (!fields[0].equalsIgnoreCase(CommandValue.ppiExclude) &&
                                !fields[1].equalsIgnoreCase(CommandValue.ppiExclude) ){
                            GenesInPPI.add(fields[0]);
                            GenesInPPI.add(fields[1]);
                            if (!fields[0].equalsIgnoreCase(fields[1])) { //remove self-interations
                                if (!PPIMap.containsKey(fields[0])) {
                                    PPIMap.put(fields[0],new ArrayList<String>());
                                }
                                if (!PPIMap.containsKey(fields[1])) {
                                    PPIMap.put(fields[1],new ArrayList<String>());
                                }
                                if (!PPIMap.get(fields[0]).contains(fields[1])) {
                                    PPIMap.get(fields[0]).add(fields[1]);
                                }
                                if (!PPIMap.get(fields[1]).contains(fields[0])) {
                                    PPIMap.get(fields[1]).add(fields[0]);
                                }
                            }
                        }
                    }
                }
            }
            int totalconnection = 0;
            for (ArrayList<String> genelist : PPIMap.values()) {
                totalconnection += genelist.size();
            }
            for (String gene: GenesInPPI) {
                if (PPIMap.containsKey(gene)) {
                    int NumberOfConnection = PPIMap.get(gene).size();
                    GeneConnectionClassMap.put(gene, NumberOfConnection);
                    if (!ConnectionGeneMap.containsKey(NumberOfConnection)) {
                        ConnectionGeneMap.put(NumberOfConnection, new HashSet<String>());
                    }
                    ConnectionGeneMap.get(NumberOfConnection).add(gene);
                }
            }
            
            LogManager.writeAndPrint("Total number of genes in PPI = " + GenesInPPI.size());
            LogManager.writeAndPrint("Total number of interactions = " + totalconnection);
            LogManager.writeAndPrint("Total number of interaction classes = " + ConnectionGeneMap.size());
            
        } catch (Exception e) {
            LogManager.writeAndPrint("\nError line ("
                    + lineNum + ") in protein-protein interaction file: " + lineStr);

            ErrorManager.send(e);
        }
    }
    public void initOutput() {
        try {
            bwPPI = new BufferedWriter(new FileWriter(ppiFilePath));
            bwPPI.write(Output.title);
            bwPPI.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void closeOutput() {
        try {
            bwPPI.flush();
            bwPPI.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public String toString() {
        return "It is running PPI function...";
    }
    
    class GeneSetStat {
        public String SetName;
        public int NodesInSet;
        public int NodesInNetwork;
        public int TotalNetworkInteractions;
        public int GeneSetInteractions;
        public double permutatedP;
        HashSet<String> NodeList = new HashSet<String>();
        HashSet<String> EdgeList = new HashSet<String>();
        HashMap<Integer,Integer> BinsAndCounts= new HashMap<Integer,Integer>();
        public GeneSetStat(String nodesetName,HashSet<String> nodeset) {
            SetName = nodesetName;
            NodesInSet = nodeset.size();
            NodeList = new HashSet<String>(nodeset);
            NodeList.retainAll(GenesInPPI);
            NodesInNetwork = NodeList.size();
            permutatedP = -1;
            //System.out.println(SetName + " " + NodeList);
        }
        public void init() {
            TotalNetworkInteractions = getTotalNetworkInteractions();
            GeneSetInteractions = getTotalDirectConnections(NodeList,true);
            GetPermutationParameters();
        }
        
        public HashSet<String> nextPermutation() {
            HashSet<String> PermutatedSet = new HashSet<String>();
            if (!BinsAndCounts.isEmpty()) {
                for (Integer bin : BinsAndCounts.keySet()) {
                    int count = BinsAndCounts.get(bin);
                    if (count > 0) { //should be always
                        if (count > ConnectionGeneMap.get(bin).size()) {
                            count = ConnectionGeneMap.get(bin).size();
                        }
                        Object[] temp = random.nextSample(ConnectionGeneMap.get(bin), count);
                        for (Object o : temp) {
                            PermutatedSet.add((String) o);
                        }
                    }
                }
            }
            return PermutatedSet;
        }
        public int getTotalNetworkInteractions() {
            int Count = 0;
            for (String gene : NodeList) {
                if (PPIMap.containsKey(gene)) {
                    Count += PPIMap.get(gene).size();
                }
            }
            return Count;
        }
        public void GetPermutationParameters() {
            for (String gene:NodeList) {
                if (GeneConnectionClassMap.containsKey(gene)) {
                    int nBin = GeneConnectionClassMap.get(gene);
                    if (nBin > 0) {
                        if (BinsAndCounts.containsKey(nBin)) {
                            BinsAndCounts.put(nBin, BinsAndCounts.get(nBin)+1);
                        } else {
                            BinsAndCounts.put(nBin, 1);
                        }
                    }
                }
            }
        }
        
        public int getTotalDirectConnections(HashSet<String> nodelist,boolean withEdges) {
            int Count = 0;
            for (String gene : nodelist) {
                HashSet<String> clonedNodeList = new HashSet<String>(nodelist);
                if (PPIMap.containsKey(gene)) {
                    clonedNodeList.retainAll(PPIMap.get(gene));
                } else {
                    clonedNodeList.clear();
                }
                if (withEdges) {
                    for (String gene2:clonedNodeList) {
                        String edge =gene.compareTo(gene2)>0 
                                ? gene + ".." + gene2:gene2 + ".."+ gene;
                        EdgeList.add(edge);
                    }
                }
                Count += clonedNodeList.size();
            }
            Count /= 2; //no double counting
            return Count;
        }
    }
}
