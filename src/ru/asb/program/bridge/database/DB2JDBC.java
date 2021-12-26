package ru.asb.program.bridge.database;

import com.ibm.db2.jcc.DB2Driver;
import ru.asb.program.bridge.util.Log;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB2JDBC {
    private static Connection connection;

    private static String URL = null;
    private static String USERNAME = null;
    private static String PASSWORD = null;

    public static Connection connect() {
        try {
            Driver driver = new DB2Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if (!connection.isClosed()) {
                Log.info("Соединение с БД DB2 установлено!");
            }

        } catch(SQLException e) {
            Log.error("Ошибка при установке соединения с БД DB2!");
        }
        return connection;
    }

    public static boolean close() {
        try {
            connection.close();
            if (connection.isClosed()) {
                System.out.println("Соединение с БД закрыто!");
            }
            return true;
        } catch(SQLException e) {
            return false;
        }
    }

    public static void setLogonInfo(String ip, String port, String dbName, String login, String password) {
        URL = "jdbc:db2://" + ip + ":" + port + "/" + dbName;
        USERNAME = login;
        PASSWORD = password;
    }

    public static Connection getConnection() {
        return connection;
    }
}
