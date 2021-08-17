package function.external.omim;

import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class OMIMManager {
    public static final String genemap2File = Data.ATAV_HOME + "data/omim/genemap2.txt";
    private static final HashMap<String, String> omimMap = new HashMap<>();

    public static String getHeader() {
        return "OMIM Disease,OMIM Inheritance";
    }

    public static void init() {
        if (OMIMCommand.isInclude) {
            initMap();
        }
    }

    public static void main(String[] args) {
        initMap();
    }
    
    private static void initMap() {
        if (!omimMap.isEmpty()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(genemap2File)));
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");
                
                if(tmp.length < 13) {
                    continue;
                }

                String[] geneSymbols = tmp[6].replaceAll("( )+", "").toUpperCase().split(",");

                String phenotype = tmp[12];

                for (String gene : geneSymbols) {
                    if (!gene.isEmpty() && !phenotype.isEmpty()) {
                        omimMap.put(gene, phenotype);
                    }
                }

                String gene = tmp[8].toUpperCase();
                if (!gene.isEmpty() && !phenotype.isEmpty() && !omimMap.containsKey(gene)) {
                    omimMap.put(gene, phenotype);
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static String getOMIM(String geneName) {
        return FormatManager.getString(omimMap.get(geneName.toUpperCase()));
    }

    public static Set<String> getAllGeneSet() {
        return omimMap.keySet();
    }
}
