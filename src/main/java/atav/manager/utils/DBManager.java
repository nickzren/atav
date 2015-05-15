package atav.manager.utils;

import atav.global.Data;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;

/**
 *
 * @author nick
 */
public class DBManager {

    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String DB_USER_NAME = "atav";
    public static final String DB_PASSWORD = "13qeadzc";
    public static final String DB_PORT = "3306";
    public static final String DB_NAME = "CHGV_Annotation_DB_slave";
    public static final String DB_HOMO_SAPIENS_CORE_NAME = "homo_sapiens_core_73_37";
    public static String[] dbHostNameList;
    private static HashMap<String, String> dbHostMap = new HashMap<String, String>();

    static {
        dbHostMap.put("sva0", "192.168.1.50");
        dbHostMap.put("annodb01", "10.73.50.31");
        dbHostMap.put("annodb02", "10.73.50.32");
        dbHostMap.put("annodb03", "10.73.50.33");
        dbHostMap.put("annodb04", "10.73.50.34");
        dbHostMap.put("annodb05", "10.73.50.35");
    }

    private static Connection conn;
    private static Statement stmt;
    private static Connection readOnlyConn;
    private static Statement readOnlyStmt; // this is just for collecting annotation data
    public static String dbHostIp = "";
    public static String dbHostName = "";

    public static void init() {
        try {
            Class.forName(DRIVER);

            initHostList();

            chooseDBHost();

            conn = getConnection();
            stmt = conn.createStatement();

            readOnlyConn = getConnection();
            readOnlyStmt = readOnlyConn.createStatement(
                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            readOnlyStmt.setFetchSize(Integer.MIN_VALUE);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initHostList() {        
        String configPath = Data.DB_HOST_CONFIG_PATH;

        if (CommandValue.isDebug) {
            configPath = Data.RECOURCE_PATH + configPath;
        }
        
        File f = new File(configPath);
        
        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String str;
            if ((str = br.readLine()) != null) {
                str = str.toLowerCase();
                dbHostNameList = str.split(",");
            }

            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static Connection getConnection() {
        try {
            String url = "jdbc:mysql://" + dbHostIp + ":" + DB_PORT + "/" + DB_NAME;

            return DriverManager.getConnection(url, DB_USER_NAME, DB_PASSWORD);
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return null;
    }

    public static Statement createStatement() {
        try {
            return conn.createStatement();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return null;
    }

    public static ResultSet executeReadOnlyQuery(String sqlQuery) throws SQLException {
        return readOnlyStmt.executeQuery(sqlQuery);
    }

    public static ResultSet executeQuery(String sqlQuery) throws SQLException {
        return stmt.executeQuery(sqlQuery);
    }

    //executeUpdate
    public static void executeUpdate(String sqlQuery) throws SQLException {
        stmt.executeUpdate(sqlQuery);
    }

    public static void setDBHost(String hostName) {
        dbHostIp = dbHostMap.get(hostName);
        dbHostName = hostName;
    }

    private static void chooseDBHost() throws Exception {
        int minNum = Integer.MAX_VALUE;

        if (!dbHostIp.isEmpty()) { // --db-host
            minNum = getNumOfATAV(dbHostIp);
        } else {
            while (true) {
                minNum = getMinNumFromServers();

                if (minNum <= 10) {
                    break;
                } else {
                    LogManager.writeAndPrint("All available AnnoDB servers are "
                            + "reached to max concurrent jobs(10), your job "
                            + "will wait for 30 minutes then auto restart.");

                    Thread.sleep(1800000);
                }
            }
        }

        LogManager.writeAndPrint("Your ATAV Job is quering data from server " + dbHostName + ". "
                + "(" + minNum + " concurrent ATAV Jobs)");
    }

    private static int getMinNumFromServers() {
        int minNum = Integer.MAX_VALUE;
        int currentNum;

        for (String hostName : dbHostNameList) {
            String hostIp = dbHostMap.get(hostName);

            currentNum = getNumOfATAV(hostIp);

            if (currentNum < minNum) {
                minNum = currentNum;
                dbHostIp = hostIp;
                dbHostName = hostName;
            }
        }

        return minNum;
    }

    private static int getNumOfATAV(String hostIp) {
        String sqlCarrier = "select count(USER) from "
                + "information_schema.processlist where USER='atav'";

        try {
            String url = "jdbc:mysql://" + hostIp + ":" + DB_PORT;

            Connection conn = DriverManager.getConnection(url,
                    DB_USER_NAME,
                    DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlCarrier);

            if (rs.next()) {
                return rs.getInt(1) / 2;
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }

        return 0;
    }
}
