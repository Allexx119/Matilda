package ru.asb.program.bridge.database;

import oracle.jdbc.driver.OracleDriver;
import ru.asb.program.bridge.util.Log;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleJDBC {
    private static Connection connection;

    private static String URL = null;
    private static String USERNAME = null;
    private static String PASSWORD = null;

    public static Connection connect() {
        try {
            Driver driver = new OracleDriver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if (!connection.isClosed()) {
                Log.info("Connection to the Oracle database is established!");
            }

        } catch(SQLException e) {
            Log.error("Error connecting to the Oracle database!");
        }
        return connection;
    }

    public static boolean close() {
        try {
            connection.close();
            if (connection.isClosed()) {
                System.out.println("The connection to the Oracle database is closed!");
            }
            return true;
        } catch(SQLException e) {
            return false;
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void setLogonInfo(String ip, String port, String dbName, String login, String password) {
        URL = "jdbc:oracle://" + ip + ":" + port + "/" + dbName + "?useLegacyDatetimeCode=false&serverTimezone=America/New_York&relaxAutoCommit=true";
        USERNAME = login;
        PASSWORD = password;
    }
}
