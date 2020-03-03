package function.cohort.vargeno;

import function.annotation.base.Annotation;
import function.annotation.base.PolyphenManager;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.external.ccr.CCRCommand;
import function.external.ccr.CCRManager;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.DiscovEHRManager;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.gnomad.GnomADCommand;
import function.external.gnomad.GnomADManager;
import function.external.limbr.LIMBRCommand;
import function.external.limbr.LIMBRManager;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCManager;
import function.external.mtr.MTRCommand;
import function.external.mtr.MTRManager;
import function.external.pext.PextCommand;
import function.external.pext.PextManager;
import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.StringJoiner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import utils.CommonCommand;
import utils.ErrorManager;
import utils.FormatManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class ListVarGenoLite {

    public static BufferedWriter bwGenotypes = null;

    public static final String genotypeLiteFilePath = CommonCommand.outputPath + "genotypes.csv";

    public static final String VARIANT_ID_HEADER = "Variant ID";
    public static final String STABLE_ID_HEADER = "Transcript Stable Id";
    public static int STABLE_ID_HEADER_INDEX;
    public static final String EFFECT_HEADER = "Effect";
    public static int EFFECT_HEADER_INDEX;
    public static final String HAS_CCDS_HEADER = "Has CCDS Transcript";
    public static int HAS_CCDS_HEADER_INDEX;
    public static final String HGVS_c_HEADER = "HGVS_c";
    public static int HGVS_c_HEADER_INDEX;
    public static final String HGVS_p_HEADER = "HGVS_p";
    public static int HGVS_p_HEADER_INDEX;
    public static final String POLYPHEN_HUMDIV_SCORE_HEADER = "Polyphen Humdiv Score";
    public static int POLYPHEN_HUMDIV_SCORE_HEADER_INDEX;
    public static final String POLYPHEN_HUMDIV_PREDICTION_HEADER = "Polyphen Humdiv Prediction";
    public static int POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX;
    public static final String POLYPHEN_HUMDIV_SCORE_CCDS_HEADER = "Polyphen Humdiv Score (CCDS)";
    public static int POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX;
    public static final String POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER = "Polyphen Humdiv Prediction (CCDS)";
    public static int POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX;
    public static final String POLYPHEN_HUMVAR_SCORE_HEADER = "Polyphen Humvar Score";
    public static int POLYPHEN_HUMVAR_SCORE_HEADER_INDEX;
    public static final String POLYPHEN_HUMVAR_PREDICTION_HEADER = "Polyphen Humvar Prediction";
    public static int POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX;
    public static final String POLYPHEN_HUMVAR_SCORE_CCDS_HEADER = "Polyphen Humvar Score (CCDS)";
    public static int POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX;
    public static final String POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER = "Polyphen Humvar Prediction (CCDS)";
    public static int POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX;
    public static final String GENE_NAME_HEADER = "Gene Name";
    public static int GENE_NAME_HEADER_INDEX;
    public static final String ALL_ANNOTATION_HEADER = "Consequence annotations: Effect|Gene|Transcript|HGVS_c|HGVS_p|Polyphen_Humdiv|Polyphen_Humvar";
    public static int ALL_ANNOTATION_HEADER_INDEX;
    public static final String SAMPLE_NAME_HEADER = "Sample Name";
    public static final String QC_FAIL_CASE_HEADER = "QC Fail Case";
    public static final String QC_FAIL_CTRL_HEADER = "QC Fail Ctrl";
    public static final String LOO_AF_HEADER = "LOO AF";
    public static final String TRAP_HEADER = "TraP Score";
    public static int TRAP_HEADER_INDEX;
    
    public void initOutput() {
        try {
            bwGenotypes = new BufferedWriter(new FileWriter(genotypeLiteFilePath));
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    public void closeOutput() {
        try {
            bwGenotypes.flush();
            bwGenotypes.close();
        } catch (IOException ex) {
            ErrorManager.send(ex);
        }
    }

    public void run() {
        try {
            LogManager.writeAndPrint("Start running list var geno lite function");

            initOutput();

            boolean isHeaderOutput = false;
            Iterable<CSVRecord> records = getRecords();
            for (CSVRecord record : records) {
                if (!isHeaderOutput) {
                    outputHeader(record);
                    isHeaderOutput = true;
                }

                VariantLite variantLite = new VariantLite(record);

                // output qualifed record to genotypes file
                if (variantLite.isValid()) {
                    outputGenotype(variantLite);
                }
            }

            closeOutput();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static String[] getHeaders() {
        String[] headers = {
            VARIANT_ID_HEADER,
            STABLE_ID_HEADER,
            EFFECT_HEADER,
            HAS_CCDS_HEADER,
            HGVS_c_HEADER,
            HGVS_p_HEADER,
            POLYPHEN_HUMDIV_SCORE_HEADER,
            POLYPHEN_HUMDIV_PREDICTION_HEADER,
            POLYPHEN_HUMDIV_SCORE_CCDS_HEADER,
            POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER,
            POLYPHEN_HUMVAR_SCORE_HEADER,
            POLYPHEN_HUMVAR_PREDICTION_HEADER,
            POLYPHEN_HUMVAR_SCORE_CCDS_HEADER,
            POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER,
            GENE_NAME_HEADER,
            ALL_ANNOTATION_HEADER,
            SAMPLE_NAME_HEADER,
            QC_FAIL_CASE_HEADER,
            QC_FAIL_CTRL_HEADER,
            LOO_AF_HEADER
        };

        if (ExACCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, ExACManager.getHeader().split(","));
        }

        if (GnomADCommand.isIncludeExome) {
            headers = (String[]) ArrayUtils.addAll(headers, GnomADManager.getExomeHeader().split(","));
        }

        if (GnomADCommand.isIncludeGenome) {
            headers = (String[]) ArrayUtils.addAll(headers, GnomADManager.getGenomeHeader().split(","));
        }

        if (SubRvisCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, SubRvisManager.getHeader().split(","));
        }

        if (LIMBRCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, LIMBRManager.getHeader().split(","));
        }

        if (CCRCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, CCRManager.getHeader().split(","));
        }
        
        if (TrapCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, TRAP_HEADER.split(","));
        }
        
        if (DiscovEHRCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, DiscovEHRManager.getHeader().split(","));
        }

        if (MTRCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, MTRManager.getHeader().split(","));
        }

        if (RevelCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, RevelManager.getHeader().split(","));
        }

        if (PrimateAICommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, PrimateAIManager.getHeader().split(","));
        }

        if (MPCCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, MPCManager.getHeader().split(","));
        }

        if (PextCommand.isInclude) {
            headers = (String[]) ArrayUtils.addAll(headers, PextManager.getHeader().split(","));
        }

        return headers;
    }

    public Iterable<CSVRecord> getRecords() throws FileNotFoundException, IOException {
        Reader in = new FileReader(GenotypeLevelFilterCommand.genotypeFile);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(getHeaders())
                .withFirstRecordAsHeader()
                .parse(in);

        return records;
    }

    public void outputHeader(CSVRecord record) throws IOException {
        StringJoiner sj = new StringJoiner(",");

        for (int headerIndex = 0; headerIndex < record.getParser().getHeaderNames().size(); headerIndex++) {
            String value = record.getParser().getHeaderNames().get(headerIndex);
            switch (value) {
                case STABLE_ID_HEADER:
                    STABLE_ID_HEADER_INDEX = headerIndex;
                    break;
                case EFFECT_HEADER:
                    EFFECT_HEADER_INDEX = headerIndex;
                    break;
                case HAS_CCDS_HEADER:
                    HAS_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case HGVS_c_HEADER:
                    HGVS_c_HEADER_INDEX = headerIndex;
                    break;
                case HGVS_p_HEADER:
                    HGVS_p_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_SCORE_HEADER:
                    POLYPHEN_HUMDIV_SCORE_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_PREDICTION_HEADER:
                    POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_SCORE_CCDS_HEADER:
                    POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER:
                    POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_SCORE_HEADER:
                    POLYPHEN_HUMVAR_SCORE_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_PREDICTION_HEADER:
                    POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_SCORE_CCDS_HEADER:
                    POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER:
                    POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX = headerIndex;
                    break;
                case GENE_NAME_HEADER:
                    GENE_NAME_HEADER_INDEX = headerIndex;
                    break;
                case ALL_ANNOTATION_HEADER:
                    ALL_ANNOTATION_HEADER_INDEX = headerIndex;
                    break;
                case TRAP_HEADER:
                    TRAP_HEADER_INDEX = headerIndex;
                    break;
                default:
                    break;
            }

            sj.add(value);
        }

        bwGenotypes.write(sj.toString());
        bwGenotypes.newLine();
    }

    public void outputGenotype(VariantLite variantLite) throws IOException {
        CSVRecord record = variantLite.getRecord();
        Annotation mostDamagingAnnotation = variantLite.getMostDamagingAnnotation();
        String allAnnotation = variantLite.getAllAnnotation();

        StringJoiner sj = new StringJoiner(",");

        for (int headerIndex = 0; headerIndex < record.size(); headerIndex++) {
            String value = "";

            if (headerIndex == STABLE_ID_HEADER_INDEX) {
                value = mostDamagingAnnotation.getStableId();
            } else if (headerIndex == EFFECT_HEADER_INDEX) {
                value = mostDamagingAnnotation.effect;
            } else if (headerIndex == HAS_CCDS_HEADER_INDEX) {
                value = Boolean.toString(mostDamagingAnnotation.hasCCDS);
            } else if (headerIndex == HGVS_c_HEADER_INDEX) {
                value = mostDamagingAnnotation.HGVS_c;
            } else if (headerIndex == HGVS_p_HEADER_INDEX) {
                value = mostDamagingAnnotation.HGVS_p;
            } else if (headerIndex == POLYPHEN_HUMDIV_SCORE_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumdiv);
            } else if (headerIndex == POLYPHEN_HUMDIV_PREDICTION_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumdiv, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMDIV_SCORE_CCDS_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumdivCCDS);
            } else if (headerIndex == POLYPHEN_HUMDIV_PREDICTION_CCDS_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumdivCCDS, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMVAR_SCORE_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumvar);
            } else if (headerIndex == POLYPHEN_HUMVAR_PREDICTION_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumvar, mostDamagingAnnotation.effect);
            } else if (headerIndex == POLYPHEN_HUMVAR_SCORE_CCDS_HEADER_INDEX) {
                value = FormatManager.getFloat(mostDamagingAnnotation.polyphenHumvarCCDS);
            } else if (headerIndex == POLYPHEN_HUMVAR_PREDICTION_CCDS_HEADER_INDEX) {
                value = PolyphenManager.getPrediction(mostDamagingAnnotation.polyphenHumvarCCDS, mostDamagingAnnotation.effect);
            } else if (headerIndex == GENE_NAME_HEADER_INDEX) {
                value = "'" + mostDamagingAnnotation.geneName + "'";
            } else if (headerIndex == ALL_ANNOTATION_HEADER_INDEX) {
                value = allAnnotation;
            } else if (headerIndex == TRAP_HEADER_INDEX) {
                value = FormatManager.getFloat(variantLite.getTrapScore());
            } else {
                value = record.get(headerIndex);
            }

            if (value.contains(",")) {
                value = FormatManager.appendDoubleQuote(value);
            }

            sj.add(value);
        }

        bwGenotypes.write(sj.toString());
        bwGenotypes.newLine();
    }
}
