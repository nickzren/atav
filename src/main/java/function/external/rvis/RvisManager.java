package function.external.rvis;

import function.external.base.DataManager;
import global.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RvisManager {

    private static final String RVIS_PATH = "data/rvis/gene_score_140318.csv.gz";

    private static final HashMap<String, RVIS> rvisMap = new HashMap<>();
    private static StringJoiner NA = new StringJoiner(",");

    public static final String GENE_HEADER = "Gene";
    public static final String RVIS_EVS_HEADER = "0.1%RVIS%[EVS]";
    public static final String EDGE_CASE_EVS_HEADER = "EdgeCase[EVS]";
    public static final String OERATIO_PERCENTILE_EVS_HEADER = "OEratio%tile[EVS]";
    public static final String GENIC_CONSTRAINT_EVS_HEADER = "GenicConstraint[EVS]";
    public static final String ANYPOPN_RVIS_PERCENTILE_EXAC_HEADER = "0.05%_anypopn_RVIS%tile[ExAC]";
    public static final String OERATIO_PERCENTILE_EXAC_HEADER = "OEratio%tile[ExAC]";
    public static final String GENIC_CONSTRAINT_MIS_Z_PERCENTILE_EXAC_HEADER = "GenicConstraint_mis-z%tile[ExAC]";

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");
        sj.add(RVIS_EVS_HEADER);
        sj.add(EDGE_CASE_EVS_HEADER);
        sj.add(OERATIO_PERCENTILE_EVS_HEADER);
        sj.add(GENIC_CONSTRAINT_EVS_HEADER);
        sj.add(ANYPOPN_RVIS_PERCENTILE_EXAC_HEADER);
        sj.add(OERATIO_PERCENTILE_EXAC_HEADER);
        sj.add(GENIC_CONSTRAINT_MIS_Z_PERCENTILE_EXAC_HEADER);

        return sj.toString();
    }

    public static String getVersion() {
        return "RVIS: " + DataManager.getVersion(RVIS_PATH) + "\n";
    }

    public static void init() {
        if (RvisCommand.isInclude) {
            initRvisMap();
        }
    }

    private static void initRvisMap() {
        try {
            Iterable<CSVRecord> records = getRecords();
            for (CSVRecord record : records) {
                String gene = record.get(GENE_HEADER);

                RVIS rvis = new RVIS(record);

                rvisMap.put(gene, rvis);
            }

            for (int i = 0; i < getHeader().split(",").length; i++) {
                NA.add(Data.STRING_NA);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static Iterable<CSVRecord> getRecords() throws FileNotFoundException, IOException {
        InputStream fileStream = new FileInputStream(Data.ATAV_HOME + RVIS_PATH);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream);

        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(getHeaders())
                .withFirstRecordAsHeader()
                .parse(decoder);

        return records;
    }

    private static String[] getHeaders() {
        String[] headers = {
            GENE_HEADER,
            RVIS_EVS_HEADER,
            EDGE_CASE_EVS_HEADER,
            OERATIO_PERCENTILE_EVS_HEADER,
            GENIC_CONSTRAINT_EVS_HEADER,
            ANYPOPN_RVIS_PERCENTILE_EXAC_HEADER,
            OERATIO_PERCENTILE_EXAC_HEADER,
            GENIC_CONSTRAINT_MIS_Z_PERCENTILE_EXAC_HEADER};

        return headers;
    }

    public static String getLine(String geneName) {
        RVIS rvis = rvisMap.get(geneName);

        return rvis == null ? NA.toString() : rvis.toString();
    }
    
    public static boolean isHotZone(String geneName) {
        RVIS rvis = rvisMap.get(geneName);
        
        return rvis == null ? false : rvis.isHotZone();
    }
}
