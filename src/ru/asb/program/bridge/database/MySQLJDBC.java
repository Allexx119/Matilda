package ru.asb.program.bridge.database;


import ru.asb.program.bridge.util.Log;

import java.sql.Connection;
import com.mysql.cj.jdbc.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLJDBC {
    private static Connection connection;
    private static String URL = null;
    private static String USERNAME = null;
    private static String PASSWORD = null;

    public static Connection connect() {
        try {
            Log.out("Connecting MySQL...");
//            Driver driver = new com.mysql.cj.jdbc.Driver();
//            DriverManager.registerDriver(driver);
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if (!connection.isClosed()) {
                Log.out("Connection to MySQL database is established!");
            }

        } catch(SQLException e) {
            Log.error("Connecting failed! " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.error("Cannot find MySQL driver!");
        }
        return connection;
    }

    public static void close() {
        try {
            connection.close();
            if (connection.isClosed()) {
                Log.out("Connection to MySQL database is closed!");
            }
        } catch(SQLException e) {
            Log.error(e.getMessage());
        }
    }

    public static boolean test() {
        Connection connection = connect();
        if (connection != null) {
            close();
            return true;
        }
        return false;
    }

    public static void setLogonInfo(String ip, String port, String dbName, String login, String password) {
        URL = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useLegacyDatetimeCode=false&serverTimezone=America/New_York&relaxAutoCommit=true";
        USERNAME = login;
        PASSWORD = password;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void printConnectionInfo() {
        System.out.println(
                "\nMySQL JDBC Connection:\n" +
                "URL: " + MySQLJDBC.URL +"\n" +
                "login: " + MySQLJDBC.USERNAME + "\n" +
                "password: " + MySQLJDBC.PASSWORD
        );
    }
}
