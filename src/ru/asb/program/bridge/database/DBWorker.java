package ru.asb.program.bridge.database;

import ru.asb.program.bridge.util.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWorker {
    private Connection connection;
    private ResultSet set;

    public DBWorker(Connection connection) {
        this.connection = connection;
    }

    public ResultSet executeQuery(String query) {
        try {
            if(!connection.isClosed()){
                Statement statement = connection.createStatement();
                set = statement.executeQuery(query);
            }
        } catch (SQLException e) {
            Log.error("Error executing query: " + e.getMessage());
        }
        return set;
    }

    public void trancate(String table){
        try {
            if(!connection.isClosed()){
                Statement statement = connection.createStatement();
                statement.executeUpdate("TRUNCATE TABLE " + table);
                Log.out(table + " has been truncated.");
    //              connection.commit();
                statement.close();
            }
        } catch (SQLException e) {
            Log.error("Error truncating table: " + e.getMessage());
        }
    }

    public void closeConnection(){
        try {
            connection.close();
            Log.out("Connection to DB is closed!");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
