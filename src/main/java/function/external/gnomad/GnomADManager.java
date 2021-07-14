package function.external.gnomad;

import function.external.base.DataManager;
import function.variant.base.RegionManager;
import global.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class GnomADManager {

    public static final String[] EXOME_POP = {
        "global", "controls", "non_neuro",
        "afr", "amr", "asj", "eas", "sas", "fin", "nfe", "nfemax", "easmax",
        "controls_afr", "controls_amr", "controls_asj", "controls_eas", "controls_sas", "controls_fin", "controls_nfe", "controls_nfemax", "controls_easmax",
        "non_neuro_afr", "non_neuro_amr", "non_neuro_asj", "non_neuro_eas", "non_neuro_sas", "non_neuro_fin", "non_neuro_nfe", "non_neuro_nfemax", "non_neuro_easmax"
    };

    public static final String[] GENOME_POP = {
        "global", "controls", "non_neuro",
        "afr", "amr", "asj", "eas", "fin", "nfe", "nfemax", "easmax",
        "controls_afr", "controls_amr", "controls_asj", "controls_eas", "controls_fin", "controls_nfe", "controls_nfemax", "controls_easmax",
        "non_neuro_afr", "non_neuro_amr", "non_neuro_asj", "non_neuro_eas", "non_neuro_fin", "non_neuro_nfe", "non_neuro_nfemax", "non_neuro_easmax"
    };

    private static final String exomeVariantTable = "gnomad_2_1.exome_variant";
    private static final String exomeMNVTable = "gnomad_2_1.exome_mnv";
    private static final String genomeVariantTable = "gnomad_2_1.genome_variant_chr";
    private static final String genomeMNVTable = "gnomad_2_1.genome_mnv";

    // gene metrics
    private static final String GENE_METRICS_PATH = "data/gnomad/gnomad.v2.1.1.lof_metrics.subset.by_gene.csv.gz";
    private static StringJoiner geneMetricsHeader = new StringJoiner(",");
    private static final HashMap<String, GnomADGene> geneMap = new HashMap<>();

    private static PreparedStatement preparedStatement4VariantExome;
    private static PreparedStatement preparedStatement4MNVExome;
    private static PreparedStatement preparedStatement4RegionExome;
    private static final HashMap<String, PreparedStatement> preparedStatement4VariantGenomeMap = new HashMap<>();
    private static PreparedStatement preparedStatement4MNVGenome;
    private static final HashMap<String, PreparedStatement> preparedStatement4RegionGenomeMap = new HashMap<>();

    public static void init() {
        if (GnomADCommand.isIncludeGeneMetrics) {
            initGeneMap();
        }

        initPreparedStatement();
    }

    public static void initPreparedStatement() {
        if (GnomADExomeCommand.getInstance().isInclude) {
            String sql = "SELECT * FROM " + exomeVariantTable + " WHERE chr =? AND pos =? AND ref =? AND alt =?";
            preparedStatement4VariantExome = DBManager.initPreparedStatement(sql);
            sql = "SELECT * FROM " + exomeMNVTable + " WHERE chr =? AND pos =? AND ref =? AND alt =?";
            preparedStatement4MNVExome = DBManager.initPreparedStatement(sql);
            sql = "SELECT * FROM " + exomeVariantTable + " WHERE chr=? AND pos BETWEEN ? AND ?";
            preparedStatement4RegionExome = DBManager.initPreparedStatement(sql);
        }

        if (GnomADGenomeCommand.getInstance().isInclude) {
            String sql = "SELECT * FROM " + genomeMNVTable + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
            preparedStatement4MNVGenome = DBManager.initPreparedStatement(sql);
            for (String chr : RegionManager.ALL_CHR) {
                sql = "SELECT * FROM " + genomeVariantTable + chr + " WHERE chr=? AND pos=? AND ref=? AND alt=?";
                preparedStatement4VariantGenomeMap.put(chr, DBManager.initPreparedStatement(sql));

                sql = "SELECT * FROM " + genomeVariantTable + chr + " WHERE chr=? AND pos BETWEEN ? AND ?";
                preparedStatement4RegionGenomeMap.put(chr, DBManager.initPreparedStatement(sql));
            }
        }
    }

    public static String getExomeHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("gnomAD Exome FILTER");
//        sj.add("gnomAD Exome segdup");
//        sj.add("gnomAD Exome lcr");
//        sj.add("gnomAD Exome decoy");
        sj.add("gnomAD Exome rf_tp_probability");
//        sj.add("gnomAD Exome qd");
//        sj.add("gnomAD Exome pab_max");

        for (int i = 0; i < GnomADManager.EXOME_POP.length; i++) {
            if (!GnomADExomeCommand.getInstance().popSet.contains(GnomADManager.EXOME_POP[i])) {
                continue;
            }

            String pop = GnomADManager.EXOME_POP[i];
            sj.add("gnomAD Exome " + pop + "_AF");

            switch (i) {
                case 0: // global
                case 1: // controls
                case 2: // non_neuro
                    sj.add("gnomAD Exome " + pop + "_AN");
                    sj.add("gnomAD Exome " + pop + "_nhet");
                    sj.add("gnomAD Exome " + pop + "_nhomalt");
                    sj.add("gnomAD Exome " + pop + "_nhemi");
                    break;
                default:
                    break;
            }
        }

        return sj.toString();
    }

    public static String getGenomeHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("gnomAD Genome FILTER");
//        sj.add("gnomAD Genome segdup");
//        sj.add("gnomAD Genome lcr");
//        sj.add("gnomAD Genome decoy");
        sj.add("gnomAD Genome rf_tp_probability");
//        sj.add("gnomAD Genome qd");
//        sj.add("gnomAD Genome pab_max");

        for (int i = 0; i < GnomADManager.GENOME_POP.length; i++) {
            if (!GnomADGenomeCommand.getInstance().popSet.contains(GnomADManager.GENOME_POP[i])) {
                continue;
            }

            String pop = GnomADManager.GENOME_POP[i];
            sj.add("gnomAD Genome " + pop + "_AF");

            switch (i) {
                case 0: // global
                case 1: // controls
                case 2: // non_neuro
                    sj.add("gnomAD Genome " + pop + "_AN");
                    sj.add("gnomAD Genome " + pop + "_nhet");
                    sj.add("gnomAD Genome " + pop + "_nhomalt");
                    sj.add("gnomAD Genome " + pop + "_nhemi");
                    break;
                default:
                    break;
            }
        }

        return sj.toString();
    }

    public static String getGeneMetricsHeader() {
        return geneMetricsHeader.toString();
    }

    public static String getExomeVersion() {
        return "gnomAD Exome: " + DataManager.getVersion(exomeVariantTable) + "\n";
    }

    public static String getGenomeVersion() {
        return "gnomAD Genome: " + DataManager.getVersion(genomeVariantTable) + "\n";
    }

    public static String getGeneMetricsVersion() {
        return "gnomAD Gene Metrics: " + DataManager.getVersion(GENE_METRICS_PATH) + "\n";
    }

    private static void initGeneMap() {
        try {
            File f = new File(Data.ATAV_HOME + GENE_METRICS_PATH);
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(f));
            Reader decoder = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(decoder);

            String lineStr = "";
            boolean isFirstLine = true;
            while ((lineStr = br.readLine()) != null) {
                String[] tmp = lineStr.split(",");
                String geneName = tmp[0];

                if (isFirstLine) {
                    geneMetricsHeader.add("gnomAD Gene pLI");
                    geneMetricsHeader.add("gnomAD Gene mis_z");

                    isFirstLine = false;
                } else {
                    GnomADGene gene = new GnomADGene();
                    gene.pli = FormatManager.getFloat(tmp[1]);
                    gene.misZ = FormatManager.getFloat(tmp[3]);
                    
                    geneMap.put(geneName, gene);
                }
            }
            
            br.close();
            decoder.close();
            in.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static StringJoiner getGeneMetrics(String geneName) {
        StringJoiner sj = new StringJoiner(",");
        
        GnomADGene gene = geneMap.get(geneName);
        
        if(gene == null) {
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
        } else {
            sj.add(FormatManager.getFloat(gene.pli));
            sj.add(FormatManager.getFloat(gene.misZ));
        }
        
        return sj;
    }

    public static boolean isGenePLIValid(String geneName) {
        GnomADGene gene = geneMap.get(geneName);

        if (gene == null) {
            return false;
        } else {
            return gene.pli >= 0.9;
        }
    }
    
    public static boolean isGeneMisZValid(String geneName) {
        GnomADGene gene = geneMap.get(geneName);

        if (gene == null) {
            return false;
        } else {
            return gene.misZ >= 2;
        }
    }

    public static PreparedStatement getPreparedStatement4VariantExome(boolean isMNV) {
        return isMNV ? preparedStatement4MNVExome : preparedStatement4VariantExome;
    }

    public static PreparedStatement getPreparedStatement4RegionExome() {
        return preparedStatement4RegionExome;
    }

    public static PreparedStatement getPreparedStatement4VariantGenome(String chr, boolean isMNV) {
        return isMNV ? preparedStatement4MNVGenome : preparedStatement4VariantGenomeMap.get(chr);
    }

    public static PreparedStatement getPreparedStatement4RegionGenome(String chr) {
        return preparedStatement4RegionGenomeMap.get(chr);
    }
}
