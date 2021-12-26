package ru.asb.program.operation.report;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.asb.program.bridge.database.DBWorker;
import ru.asb.program.bridge.database.MySQLJDBC;
import ru.asb.program.bridge.gui.GUI;
import ru.asb.program.bridge.sapbo.*;
import ru.asb.program.bridge.util.CSV;
import ru.asb.program.bridge.util.Log;
import ru.asb.program.operation.records.Parent;
import ru.asb.program.operation.records.Report;
import ru.asb.program.operation.records.Universe;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataForDB extends Thread {
    private String tableName;
    private String insertSQL;
    private String reportsQuery = "SELECT " +
//            "* " +
            "SI_ID, SI_NAME, SI_PARENTID, SI_KIND, SI_OWNER, SI_CREATION_TIME, SI_UPDATE_TS, SI_UNIVERSE, SI_DSL_UNIVERSE " +
                                  "FROM CI_INFOOBJECTS " +
                                  "WHERE SI_KIND in ('Webi', 'CrystalReport', 'FullClient', 'Publication') " +
                                  "AND SI_INSTANCE = 0";
    private String universesQuery = "SELECT " +
                                    "SI_ID, SI_NAME, SI_KIND, SI_WEBI, SI_SPECIFIC_KIND " +
//                                      "* " +
                                    "FROM CI_APPOBJECTS " +
                                    "WHERE SI_KIND in ('Universe', 'DSL.Universe') " +  //SI_SPECIFIC_KIND='DSL.Universe'
                                    "order by SI_NAME asc";
    private String pathsQuery;

    private List<Universe> universes = new ArrayList<>();
    private List<Report> reports = new ArrayList<>();
    private List<Parent> parents = new ArrayList<>();

    private List<String> empty = new ArrayList<>();

    public DataForDB(String tableName) {
        this.tableName = tableName;
        insertSQL = "INSERT INTO " + tableName +
                    "(FOLDER_NAME, " +
                    "REPORT_PATH, " +
                    "REPORT_NAME, " +
                    "REPORT_ID, " +
                    "REPORT_TYPE, " +
                    "UNIVERSE," +
                    "SI_OWNER, " +
                    "SI_CREATE," +
                    "UPDATE_TIME) " +
                    "VALUES(?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void run() {
        try {
            if (GUI.hasInit()) {
                GUI.get().getNewStopwatch().start();
                GUI.get().getRunPanel().setStatus("Выполняется...");
            }

            Log.info("Run report get Data for DB");
            BOConnection.logon();

            Log.out("Requesting universes...");
            String SQLrequest = BOConnection.runCmsQuery(universesQuery);
            Log.done("Universes received.");
            parseUniversesJson(SQLrequest);

            Log.out("Requesting reports...");
            SQLrequest = BOConnection.runCmsQuery(reportsQuery);
            Log.done("Reports received.");
            parseReportsJson(SQLrequest);

            Log.out("Requesting paths...");
            pathsQuery = "SELECT SI_ID, SI_NAME, SI_KIND, SI_PATH FROM CI_INFOOBJECTS WHERE SI_ID in (" + getParentsId() + ")";
            SQLrequest = BOConnection.runCmsQuery(pathsQuery);
            Log.done("Paths received.");
            parsePathsJson(SQLrequest);

            findPaths();
            writeCsv();
            insertData(tableName);

            BOConnection.logoff();
            Log.done("Report get Data for DB has formed.");

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
     * Распарсить JSON ответ от сервера по запросу Reports
     * */
    private void parseReportsJson(String jsonString) throws ParseException{
        Object obj = new JSONParser().parse(jsonString);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");

        for (Object object : entries) {
            Report report = new Report();
            JSONObject entry = (JSONObject) object;

            report.setId(getAttribute(entry, "SI_ID"));
            report.setParentId(getAttribute(entry, "SI_PARENTID"));
            report.setName(getAttribute(entry, "SI_NAME"));
            report.setKind(getAttribute(entry, "SI_KIND"));
            report.setOwner(getAttribute(entry, "SI_OWNER"));
            report.setCreationTime(getAttribute(entry, "SI_CREATION_TIME"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");
            report.setUpdateTs(getAttribute(entry, "SI_UPDATE_TS"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");
            JSONObject unv = (JSONObject)entry.get("SI_UNIVERSE");
            if (unv != null) {
                int count = Integer.parseInt(getAttribute(unv, "SI_TOTAL"));
                for (int i = 1; i <= count; i++) {
                    report.addUniverseId(getAttribute(unv, String.valueOf(i)));
                }
                empty.add(report.getId());
            }
            JSONObject unx = (JSONObject)entry.get("SI_DSL_UNIVERSE");
            if (unx != null) {
                int count = Integer.parseInt(getAttribute(unx, "SI_TOTAL"));
                for (int i = 1; i <= count; i++) {
                    report.addUniverseId(getAttribute(unx, String.valueOf(i)));
                }
                empty.add(report.getId());
            }
            report.setUniverses(findUniverses(report.getUniversesId()));
            reports.add(report);
        }
    }

    /**
     * Распарсить JSON ответ от сервера по запросу Universes
     * */
    private void parseUniversesJson(String jsonString) throws ParseException {
        Object obj = new JSONParser().parse(jsonString);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");

        for (Object object : entries) {
            Universe universe = new Universe();
            JSONObject entry = (JSONObject) object;

            universe.setId(getAttribute(entry, "SI_ID"));
            universe.setName(getAttribute(entry, "SI_NAME"));
            universe.setKind(getAttribute(entry, "SI_KIND"));
            JSONObject webies = (JSONObject)entry.get("SI_WEBI");
            if (webies != null) {
                int count = Integer.parseInt(getAttribute(webies, "SI_TOTAL"));
                for (int i = 1; i <= count; i++) {
                    universe.addWebi(getAttribute(webies, String.valueOf(i)));
                }
            }
            universes.add(universe);
        }
    }

    /**
     * Распарсить JSON ответ от сервера по запросу Paths
     * */
    private void parsePathsJson(String jsonString) throws ParseException {
        Object obj = new JSONParser().parse(jsonString);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");

        for (Object object : entries) {
            Parent parent = new Parent();
            JSONObject entry = (JSONObject) object;

            parent.setId(getAttribute(entry, "SI_ID"));
            parent.setName(getAttribute(entry, "SI_NAME"));
            parent.setKind(getAttribute(entry, "SI_KIND"));
            if (parent.getKind().equals("Inbox")) {
                parent.setPath("Папки \"Входящие\"" + "/" + parent.getName() + "/");
                parent.setRootFolder("Папки \"Входящие\"");
            }
            JSONObject paths = (JSONObject)entry.get("SI_PATH");
            if (paths != null) {
                int count = Integer.parseInt(getAttribute(paths, "SI_NUM_FOLDERS"));
                StringBuilder path = new StringBuilder();
                for (int i = count; i >= 1; i--) {
                    path.append(getAttribute(paths, ("SI_FOLDER_NAME" + i))).append("/");
                }
                parent.setPath(path.append(parent.getName()).toString());
                if (count == 0) {
                    parent.setRootFolder(parent.getName());
                } else {
                    parent.setRootFolder(getAttribute(paths, ("SI_FOLDER_NAME" + count)));
                }

            }
            parents.add(parent);
        }
    }

    /**
     * Найти юниверсы, на основе которых строится отчет
     * */
    private List<String> findUniverses(List<String> universesId) {
        List<String> result = new ArrayList<>();
        for (String universeId : universesId) {
            for (Universe universe : universes) {
                if (universe.getId().equals(universeId)) {
                    result.add(universe.getName());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Найти пути и корневую папку для всех отчетов
     * */
    private void findPaths() {
        for (Report report : reports) {
            report.findPaths(parents);
        }
    }

    /**
     * Вставка данных в базу КАРЕН
     * */
    private void insertData(String tableName){
        DBWorker worker = new DBWorker(MySQLJDBC.connect());
        try {
            worker.trancate(tableName);
            for (Report report : reports) {
                PreparedStatement statement = worker.getConnection().prepareStatement(insertSQL);
                statement.setString(1, report.getRootFolder());     //FOLDER_NAME
                statement.setString(2, report.getPath());           // REPORT_PATH
                statement.setString(3, report.getName());           // REPORT_NAME
                statement.setString(4, report.getId());             // REPORT_ID
                statement.setString(5, report.getKind());           // REPORT_TYPE
                statement.setString(6, report.getUniverses());      // UNIVERSES
                statement.setString(7, report.getOwner());          // SI_OWNER
                statement.setTimestamp(8, report.getCreationTime());// SI_CREATE
                statement.setTimestamp(9, report.getUpdateTs());    // UPDATE_TIME
                statement.executeUpdate();
                statement.close();
            }
            worker.closeConnection();
            Log.out("Data inserted correctly.");
        } catch (SQLException e) {
            Log.error("Error to generate or execute insert statement to KAREN DB.");
        }
    }

    /**
     * Запись отчета в CSV файл
     * */
    private void writeCsv() throws IOException{
        CSV csv = new CSV();
        String[] header = {
                "FOLDER_NAME",
                "REPORT_PATH",
                "REPORT_NAME",
                "REPORT_ID",
                "REPORT_TYPE",
                "UNIVERS",
                "SI_OWNER",
                "SI_CREATE",
                "UPDATE_TIME"
        };
        csv.setHeader(header);
        for (Report report : reports) {
            csv.addRow(report.getRow());
        }
        csv.withHeader(true).withSeparator(';').withQuoteChar('"').toFile("result/reports/reportGetDataForDB.csv").write();
    }

    /**
     * Получить все ID родителей объектов в reports
     * */
    private String getParentsId() {
        StringBuilder parentId = new StringBuilder();
        for (Report report : reports) {
            parentId.append(report.getParentId()).append(", ");
        }
        return parentId.substring(0, parentId.length()-2);
    }

    /**
     * Получить значение атрибута в JSON файле по названию атрибута
     * */
    private String getAttribute(JSONObject source, String attributeName) {
        Object obj = source.get(attributeName);
        if (obj == null) {
            return "";
        } else {
            return String.valueOf(obj);
        }
    }

}
