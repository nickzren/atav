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

    private static int maxATAVJobNum; // using this to control max connections to db server
    private static String priorityUsers;

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // init from config
    private static String dbPort;
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static String dbConfigProperties = "?serverTimezone=UTC&autoReconnect=true";
    private static HashMap<String, String> dbHostMap = new HashMap<String, String>();

    // init from user command
    private static String dbHostIp = "";
    private static String dbHostName = "";

    private static Connection conn;
    private static Statement stmt;
    // this is just for collecting annotation data
    private static Connection concurReadOnlyConn;
    private static Statement concurReadOnlyStmt;

    // count for all db servers that failed to connect
    private static int failDBConnectionCount = 0;

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

            concurReadOnlyConn = getConnection();
            concurReadOnlyStmt = concurReadOnlyConn.createStatement(
                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY); // The ResultSet object cannot be updated using the ResultSet interface.
            concurReadOnlyStmt.setFetchSize(Integer.MIN_VALUE);
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private static void initDataFromSystemConfig() {
        try {
            String configPath = Data.SYSTEM_CONFIG;

            if (CommonCommand.isDebug) {
                configPath = Data.SYSTEM_CONFIG_FOR_DEBUG;
            }

            InputStream input = new FileInputStream(configPath);
            Properties prop = new Properties();
            prop.load(input);

            initServers(prop.getProperty("servers"));

            dbPort = prop.getProperty("dbport", "3306");
            dbName = prop.getProperty("annodb");
            dbUser = prop.getProperty("dbuser");
            dbPassword = prop.getProperty("dbpassword");
            maxATAVJobNum = Integer.parseInt(prop.getProperty("max-atav-job"));
            priorityUsers = prop.getProperty("priority-user");
        } catch (IOException e) {
            ErrorManager.send(e);
        }
    }

    private static void initServers(String servers) {
        servers = servers.replaceAll("( )+", "");

        for (String server : servers.split(",")) {
            String[] tmp = server.split(";"); // format: server_name;server_ip
            dbHostMap.put(tmp[0], tmp[1]);
        }
    }

    private static Connection getConnection() {
        try {
            String url = "jdbc:mysql://" + dbHostIp + ":" + dbPort + "/" + dbName + dbConfigProperties;

            if (CommonCommand.isDebug) {
                System.out.println(url);
            }

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

    public static PreparedStatement initPreparedStatement(String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException ex) {
            ErrorManager.send(ex);
        }

        return null;
    }

    public static Statement createStatementByConcurReadOnlyConn() {
        try {
            return concurReadOnlyConn.createStatement();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        return null;
    }

    public static ResultSet executeConcurReadOnlyQuery(String sqlQuery) throws SQLException {
        return concurReadOnlyStmt.executeQuery(sqlQuery);
    }

    public static ResultSet executeQuery(String sqlQuery) throws SQLException {
        return stmt.executeQuery(sqlQuery);
    }

    //executeUpdate
    public static void executeUpdate(String sqlQuery) throws SQLException {
//        stmt.executeUpdate(StringEscapeUtils.escapeSql(sqlQuery));
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
                ErrorManager.print("Not exist server: " + dbHostName, ErrorManager.COMMAND_PARSING);
            }

            minNum = getNumOfATAV(dbHostIp);
        } else {
            minNum = getMinNumFromServers();

            if (minNum > maxATAVJobNum && !priorityUsers.contains(Data.userName)) {
                ErrorManager.print("All database servers "
                        + "reached to max concurrent jobs, please submit your ATAV job later.", ErrorManager.MAX_CONNECTION);
            }
        }

        LogManager.writeAndPrint("Database server: " + dbHostName + " " + "(" + minNum + " running jobs)");
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
            String url = "jdbc:mysql://" + hostIp + ":" + dbPort + dbConfigProperties;

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
            failDBConnectionCount++;
            
            // if all available database servers failed to connect then throw errors
            if (dbHostMap.size() == failDBConnectionCount) {
                ErrorManager.send(e);
            }

            return Integer.MAX_VALUE;
        }

        return 0;
    }

    public static String getHost() {
        return dbHostName;
    }
}
