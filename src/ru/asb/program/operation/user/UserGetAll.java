package ru.asb.program.operation.user;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.asb.program.bridge.database.DBWorker;
import ru.asb.program.bridge.database.MySQLJDBC;
import ru.asb.program.bridge.gui.GUI;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.sapbo.BOConnectionException;
import ru.asb.program.bridge.util.CSV;
import ru.asb.program.operation.records.User;
import ru.asb.program.bridge.util.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserGetAll extends Thread {
    private final String reportQuery = "SELECT " +
            "SI_ID, " +
            "SI_NAME, " +
            "SI_USERFULLNAME, " +
            "SI_EMAIL_ADDRESS, " +
            "SI_DISABLED_ID, " +    //SI_CUSTOM_MAPPED_ATTRIBUTES   //SI_ENT_CUSTOM_MAPPED_ATTRIBUTES
            "SI_APPOINTMENT, " +    //нет
            "SI_ADMINISTRATION, " + //нет
            "SI_DIVISION, " +
            "SI_DIRECTION, " +
            "SI_DEPARTMENT, " +
            "SI_OFFICE, " +
            "SI_GROUP, " +
            "SI_CREATION_TIME, " +
            "SI_LASTLOGONTIME, " +
            "SI_UPDATE_TS, " +
            "SI_DISABLED_TIME, " +  //SI_CUSTOM_MAPPED_ATTRIBUTES   //SI_ENT_CUSTOM_MAPPED_ATTRIBUTES
            "SI_CUSTOM_MAPPED_ATTRIBUTES, " +
            "SI_TN, " + //SI_CUSTOM_MAPPED_ATTRIBUTES   //SI_ENT_CUSTOM_MAPPED_ATTRIBUTES
            "SI_DESCRIPTION, " +
            "SI_1_DOMAIN, " + //SI_CUSTOM_MAPPED_ATTRIBUTES //SI_ENT_CUSTOM_MAPPED_ATTRIBUTES
            "SI_2_DEPT" +   //SI_CUSTOM_MAPPED_ATTRIBUTES   //SI_ENT_CUSTOM_MAPPED_ATTRIBUTES
            " FROM CI_SYSTEMOBJECTS WHERE SI_PROGID= 'CrystalEnterprise.USER'";

    private final String insertSQL;
    private final List<User> users = new ArrayList<>();

    public UserGetAll(String tableName) {

        insertSQL = "INSERT INTO " + tableName +
                "(SI_ID, " +
                "SI_NAME, " +
                "SI_USERFULLNAME, " +
                "SI_EMAIL_ADDRESS, " +
                "SI_DISABLED_ID, " +
                "SI_CREATE, " +
                "SI_LASTLOGON, " +
                "SI_UPDATE, " +
                "SI_DISABLED_TIME, " +
                "SI_TN, " +
                "SI_1_DOMAIN, " +
                "SI_2_DEPT ) VALUES"
                + "(?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void run() {
        try {
            if (GUI.hasInit()) {
                GUI.get().getNewStopwatch().start();
                GUI.get().getRunPanel().setStatus("Выполняется...");
            }

            Log.info("Start forming report User Get All.");
            BOConnection.logon();

            String SQLrequest = BOConnection.runCmsQuery(reportQuery);
            Log.out("Request complete.");

            Files.write(Paths.get("result/request.txt"), SQLrequest.getBytes(), StandardOpenOption.CREATE);

            parseJson(SQLrequest);
            writeCsv();
          //  insertData(tableName);

            BOConnection.logoff();
            Log.done("Report User Get All has formed.");

            if (GUI.hasInit()) {
                GUI.get().getRunPanel().getRunButton().setEnabled(true);
                GUI.get().getRunPanel().setStatus("Готово");
                GUI.get().getStopwatch().interrupt();
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch(ParseException pe) {
            Log.error("Unable to parse JSON.");
        } catch (BOConnectionException boe) {
            Log.error(boe.getMessage());
        }
    }

    /**
     * Парсинг запроса, полученного из SAP BO
     * */
    private void parseJson(String jsonString) throws ParseException {
        Object obj = new JSONParser().parse(jsonString);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");

        for (Object object : entries) {
            User user = new User();
            JSONObject entry = (JSONObject) object;
            user.setId(getAttribute(entry, "SI_ID"));
            user.setName(getAttribute(entry, "SI_NAME"));
            user.setUserFullName(getAttribute(entry, "SI_USERFULLNAME"));
            user.setEmailAddress(getAttribute(entry, "SI_EMAIL_ADDRESS"));
            user.setAppointment(getAttribute(entry, "SI_APPOINTMENT"));
            user.setAdministration(getAttribute(entry, "SI_ADMINISTRATION"));
            user.setDivision(getAttribute(entry, "SI_DIVISION"));
            user.setDirection(getAttribute(entry, "SI_DIRECTION"));
            user.setDepartment(getAttribute(entry, "SI_DEPARTMENT"));
            user.setOffice(getAttribute(entry, "SI_OFFICE"));
            user.setGroup(getAttribute(entry, "SI_GROUP"));
            user.setCreationTime(getAttribute(entry, "SI_CREATION_TIME"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");
            user.setLastLogonTime(getAttribute(entry, "SI_LASTLOGONTIME"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");
            user.setUpdateTs(getAttribute(entry, "SI_UPDATE_TS"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");

            JSONObject customMappedAttributes = (JSONObject)entry.get("SI_CUSTOM_MAPPED_ATTRIBUTES");
            if (customMappedAttributes != null) {
                user.setTn(getAttribute(customMappedAttributes, "SI_TN"));
                user.setDomain(getAttribute(customMappedAttributes, "SI_1_DOMAIN"));
                user.setDept(getAttribute(customMappedAttributes, "SI_2_DEPT"));
                user.setDisabledId(getAttribute(customMappedAttributes, "SI_DISABLED_ID"));
                user.setDisabledTime(getAttribute(customMappedAttributes, "SI_DISABLED_TIME"), "dd.MM.yyyy", "dd.MM.yyyy");
            }
            users.add(user);
        }
        Log.out("JSON parsing finished correctly.");
    }

    /**
     * Вставка данных в базу КАРЕН
     * */
    private void insertData(String tableName){
        DBWorker worker = new DBWorker(MySQLJDBC.connect());
        try {
            worker.trancate(tableName);
            for (User user : users) {
                PreparedStatement statement = worker.getConnection().prepareStatement(insertSQL);
                statement.setInt(1, Integer.parseInt(user.getId())); //SI_ID,
                statement.setString(2, user.getName()); // SI_NAME,
                statement.setString(3, user.getUserFullName()); // SI_USERFULLNAME,
                statement.setString(4, user.getEmailAddress()); // SI_EMAIL_ADDRESS,
                statement.setString(5, user.getDisabledId()); // SI_DISABLED_ID, --not filling
                statement.setTimestamp(6, user.getCreationTime()); // SI_CREATE,
                statement.setTimestamp(7, user.getLastlogontime()); // SI_LASTLOGON,
                statement.setTimestamp(8, user.getUpdateTs()); // SI_UPDATE,
                statement.setTimestamp(9, user.getDisabledTime()); // SI_DISABLED_TIME, --not filling
                statement.setString(10, user.getTn()); // SI_TN,
                statement.setString(11, user.getDomain()); // SI_1_DOMAIN,
                statement.setString(12, user.getDept()); // SI_2_DEPT
                statement.executeUpdate();
                statement.close();
            }
            worker.closeConnection();
            Log.out("Data inserted correctly.");
        } catch (SQLException e) {
            Log.error("Error to generate or execute statement.");
        }
    }

    /**
     * Запись CSV
     * */
    private void writeCsv() throws IOException{
        CSV csv = new CSV();
        String[] header = {
                "SI_ID",
                "SI_NAME",
                "SI_USERFULLNAME",
                "SI_EMAIL_ADDRESS",
                "SI_DISABLED_ID",
                "SI_CREATE",
                "SI_LASTLOGON" ,
                "SI_UPDATE",
                "SI_DISABLED_TIME",
                "SI_TN",
                "IS_APPOINTMENT",
                "SI_1_DOMAIN",
                "SI_2_DEPT"
        };
        csv.setHeader(header);
        for (User user : users) {
            csv.addRow(user.getRow());
        }
        csv.withHeader(true).withSeparator(';').withQuoteChar('"').toFile("result/reports/userGetAll.csv").write();
    }

    private String getAttribute(JSONObject source, String attributeName) {
        Object obj = source.get(attributeName);
        if (obj == null) {
            return "";
        } else {
            return String.valueOf(obj);
        }
    }
}
