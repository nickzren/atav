package function.external.clingen;

import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class ClinGenManager {

    private static final String clingenFile = Data.ATAV_HOME + "data/clingen/ClinGen_gene_curation_list_GRCh37.tsv";
    private static final HashMap<String, ClinGen> clinGenMap = new HashMap<>();

    public static String getHeader() {
        return "ClinGen"; // HaploinsufficiencyDesc
    }

    public static void init() {
        if (ClinGenCommand.isInclude) {
            initMap();
        }
    }

    private static void initMap() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(clingenFile)));
            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("#")) {
                    continue;
                }

                String[] tmp = lineStr.split("\t");

                String gene = tmp[0];
                String haploinsufficiencyDescription = tmp[5].replace("Dosage sensitivity unlikely", "Unlikely");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Gene associated with autosomal recessive phenotype", "Recessive evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("No evidence available", "No evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Little evidence for dosage pathogenicity", "Little evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Some evidence for dosage pathogenicity", "Some evidence");
                haploinsufficiencyDescription = haploinsufficiencyDescription.replace("Sufficient evidence for dosage pathogenicity", "Sufficient evidence");

                ClinGen clinGen = new ClinGen(haploinsufficiencyDescription);
                clinGenMap.put(gene, clinGen);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static ClinGen getClinGen(String geneName) {
        ClinGen clinGen = clinGenMap.get(geneName);

        if (clinGen == null) {
            return new ClinGen(Data.STRING_NA);
        }

        return clinGen;
    }

    public static HashMap<String, ClinGen> getMap() {
        return clinGenMap;
    }
}
