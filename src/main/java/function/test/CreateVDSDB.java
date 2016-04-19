package function.test;

import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import utils.DBManager;
import utils.LogManager;

/**
 *
 * @author nick
 */
public class CreateVDSDB {

    public static void run() throws Exception {
//        createTable();

        loadData();
    }

    private static void createTable() throws SQLException {
        for (String chr : RegionManager.ALL_CHR) {
            String sql = "CREATE TABLE snv_score_chr" + chr + " ("
                    + "  pos int(11) NOT NULL,"
                    + "  ref varchar(1) NOT NULL,"
                    + "  alt varchar(1) NOT NULL,"
                    + "  ensg_gene varchar(64) NOT NULL,"
                    + "  score float NOT NULL,"
                    + "  PRIMARY KEY (pos,alt,ensg_gene)"
                    + ") ENGINE=TokuDB;";

            LogManager.writeAndPrint(sql);
            DBManager.executeUpdate(sql);
        }
    }

    private static void loadData() throws SQLException, Exception {
        File dir = new File("/nfs/goldstein/goldsteinlab/sahar/workspace/SpliceScore/runs/allGenome/varScore/VDSdb");

        int count = 0;

        for (File file : dir.listFiles()) {
            count++;
            System.out.println(count);

            String fileName = file.getName();

            if (fileName.startsWith("chr")) {
                String chr = fileName.substring(fileName.indexOf("chr"), fileName.indexOf("_"));

                if (isGeneLoaded("snv_score_" + chr, file.getAbsolutePath())) {
                    continue;
                } else {
                    String sql = "LOAD DATA LOCAL INFILE "
                            + "'" + file.getAbsolutePath() + "' "
                            + "IGNORE INTO TABLE snv_score_" + chr;

//                    LogManager.writeAndPrint(sql);
                    DBManager.executeQuery(sql);
                }
            }
        }
    }

    private static boolean isGeneLoaded(String table, String filePath) throws Exception {
        int geneFileCount = countLines(filePath);

        String line = getFirstLine(filePath);
        String gene = line.split("\t")[3];

        String query = "SELECT count(*) AS count "
                + "FROM " + table + " v, ensg_gene_region g "
                + "WHERE g.ensg_gene = '" + gene + "' "
                + "AND v.pos BETWEEN g.start AND g.end "
                + "AND v.ensg_gene = '" + gene + "'";

        ResultSet rset = DBManager.executeQuery(query);
        int carrierTableCount = 0;
        if (rset.next()) {
            carrierTableCount = rset.getInt("count");
        }
        rset.close();

        if (geneFileCount == carrierTableCount) {
            return true;
        }

        return false;
    }

    private static String getFirstLine(String filePath) throws IOException {
        Vector<String> vector = systemCall("head -n1 " + filePath);

        return vector.get(0);
    }

    private static int countLines(String filePath) throws IOException {
        int lineCount = 0;

        Vector<String> vector = systemCall("wc -l " + filePath);

        String[] tmp = vector.get(0).split("( )+");

        lineCount = Integer.valueOf(tmp[0]);

        return lineCount;
    }

    private static Vector<String> systemCall(String cmd) {
        Vector<String> result = new Vector<String>();

        try {
            Process myProc = Runtime.getRuntime().exec(cmd);

            InputStream is = myProc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line;

            while ((line = br.readLine()) != null) {
                result.add(line);
            }

            myProc.waitFor();
        } catch (Exception ex) {
        }

        return result;
    }
}
