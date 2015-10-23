package utils;

import global.Data;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author nick
 */
public class DBManager {

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_PORT = "3306";

    // init from config
    private static String annodbName;
    public static String homoSapiensCoreName;
    private static String dbUser;
    private static String dbPassword;
    private static HashMap<String, String> dbHostMap = new HashMap<String, String>();

    // init from user command
    private static String dbHostIp = "";
    private static String dbHostName = "";

    private static Connection conn;
    private static Statement stmt;
    private static Connection readOnlyConn;
    private static Statement readOnlyStmt; // this is just for collecting annotation data

    public static void init() {
        try {
            if (CommonCommand.isNonDBAnalysis) {
                return;
            }

            initDataFromSystemConfig();

            Class.forName(DRIVER);

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

    private static void initDataFromSystemConfig() {
        try {
            InputStream input = new FileInputStream(Data.SYSTEM_CONFIG);
            Properties prop = new Properties();
            prop.load(input);

            initServers(prop.getProperty("servers"));

            annodbName = prop.getProperty("annodb");
            homoSapiensCoreName = prop.getProperty("homo_sapiens_core");
            dbUser = prop.getProperty("dbuser");
            dbPassword = prop.getProperty("dbpassword");
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    private static void initServers(String servers) {
        servers = servers.replaceAll("( )+", "");

        for (String server : servers.split(",")) {
            String[] tmp = server.split("-"); // format: server_name-server_ip
            dbHostMap.put(tmp[0], tmp[1]);
        }
    }

    private static Connection getConnection() {
        try {
            String url = "jdbc:mysql://" + dbHostIp + ":" + DB_PORT + "/" + annodbName;

            return DriverManager.getConnection(url, dbUser, dbPassword);
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
        dbHostName = hostName;
    }

    private static void chooseDBHost() throws Exception {
        int minNum = Integer.MAX_VALUE;

        if (!dbHostName.isEmpty()) { // --db-host
            dbHostIp = dbHostMap.get(dbHostName);

            if (dbHostIp == null) {
                ErrorManager.print("Non existing server: " + dbHostName);
            }

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

        for (String hostName : dbHostMap.keySet()) {
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
                    dbUser,
                    dbPassword);
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

    public static String getHost() {
        return dbHostName;
    }
}
