package ru.asb.program.operation.report;

import net.sf.jsqlparser.JSQLParserException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.asb.program.bridge.gui.GUI;
import ru.asb.program.bridge.parser.Parser;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.sapbo.BOConnectionException;
import ru.asb.program.bridge.util.Comparator;
import ru.asb.program.bridge.util.Helper;
import ru.asb.program.operation.records.Webi;
import ru.asb.program.bridge.util.Log;

import java.io.*;
import java.util.*;

public class ReportsTables extends Thread {
    private String reportQuery = "SELECT SI_ID, SI_NAME, SI_KIND, SI_CREATION_TIME, SI_UPDATE_TS from CI_INFOOBJECTS where (SI_KIND = 'Webi')";
    private List<Webi> fullWebiList = new ArrayList<>();
    private List<String> tablesFilterList = new ArrayList<>();
    private boolean withSQL;

    private ObjectOutputStream tempObjectOutputStream;

    public ReportsTables(List<String> tablesFilterList, boolean withSQL) {
        this.tablesFilterList.addAll(tablesFilterList);
        this.withSQL = withSQL;
    }

    @Override
    public void run() {
        try {
            if (GUI.hasInit()) {
                GUI.get().getNewStopwatch().start();
                GUI.get().getRunPanel().setStatus("Выполняется...");
            }

            Log.info("Report Find SQL Tables");
            BOConnection.logon();
            int webiCount = getReportCount();
            Log.out("Web Intelligence documents count: " + webiCount);
            String SQLrequest = BOConnection.runCmsQuery(reportQuery);

            fullWebiList = getWebies(SQLrequest);
            fullWebiList = updateWebies();
            BOConnection.logoff();

            writeReport(BOConnection.getHostname(), fullWebiList, tablesFilterList, withSQL);

            if (GUI.hasInit()) {
                GUI.get().getRunPanel().getRunButton().setEnabled(true);
                GUI.get().getRunPanel().getSqlCheckBox().setEnabled(true);
                GUI.get().getRunPanel().setStatus("Готово");
                GUI.get().getStopwatch().interrupt();
            }
        } catch (IOException | ParseException | BOConnectionException ex) {
            Log.error(ex.getMessage());
            ex.printStackTrace();
        } catch(InterruptedException inex) {
            Log.out("Формирование отчета остановлено пользователем.");
        } catch (NumberFormatException nfe) {
            try {
                nfe.printStackTrace();
                BOConnection.logoff();
            }catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    /**
     * Получение ID объектов WebI из JSON
     * */
    private List<Webi> getWebies(String jsonString) throws ParseException {
        List<Webi> webiList = new ArrayList<>();
        Object obj = new JSONParser().parse(jsonString);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");

        for (Object object : entries) {
            Webi webi = new Webi();
            JSONObject entry = (JSONObject) object;
            webi.setId(getAttribute(entry, "SI_ID"));
            webi.setName(getAttribute(entry, "SI_NAME"));
            webi.setKind(getAttribute(entry, "SI_KIND"));
            webi.setCreationTime(getAttribute(entry, "SI_CREATION_TIME"), "dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");
            webi.setUpdateTs(getAttribute(entry, "SI_UPDATE_TS"),"dd.MM.yyyy HH:mm", "dd.MM.yyyy H:mm");

            webiList.add(webi);
        }
        return webiList;
    }

    /**
     * Получить количество документов типа WebI.
     * */
    private Integer getReportCount() throws IOException, ParseException, BOConnectionException {
        String response = BOConnection.runCmsQuery("SELECT COUNT(SI_ID) from CI_INFOOBJECTS where (SI_KIND = 'Webi')");
        Object obj = new JSONParser().parse(response);
        JSONObject json = (JSONObject) obj;
        JSONArray entries = (JSONArray) json.get("entries");
        JSONObject entry = (JSONObject) entries.get(0);
        return Integer.parseInt(String.valueOf(((JSONObject)entry.get("SI_AGGREGATE_COUNT")).get("SI_ID")));
    }

    /**
     * Обновить документы Webi
     * */
    private List<Webi> updateWebies() throws IOException, ParseException, InterruptedException {
        List<Webi> updatedList = new ArrayList<>();
        try {
            //отслеживаем изменения по сравнению с прошлым успешным запуском
            Comparator comparator = new Comparator(readWebies(), fullWebiList);
            List<Webi> actualWebies = comparator.getActual();
            List<Webi> changedWebies = comparator.getChanged();

            //восстанавливаем уже загруженные webi, в случае если программа упала
            Comparator recovery = new Comparator(recoverWebies(), changedWebies);
            actualWebies.addAll(recovery.getActual());
            changedWebies = recovery.getChanged();

            Log.out("Количество актуальных Webi: " + actualWebies.size());
            Log.out("Количество измененных Webi: " + changedWebies.size());

            if (changedWebies.size() > 0) {
                actualWebies.addAll(requestWebiesInfo(changedWebies));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("result\\data\\Webi_" + BOConnection.getHostname() + ".data"));
                objectOutputStream.writeObject(actualWebies);
                objectOutputStream.close();
            }

            updatedList = actualWebies;

            tempObjectOutputStream.close();
            File tempFile = new File("result\\data\\Webi_" + BOConnection.getHostname() + ".temp");
            if (tempFile.exists()) {
                if (tempFile.delete()) {
                    Log.out("Временный файл удален.");
                } else {
                    Log.out("Не могу удалить временный файл.");
                }
            }
        } catch(IOException ioe) {
            if (tempObjectOutputStream != null) tempObjectOutputStream.close();
        }
        return updatedList;
    }

    /**
     * Прочитать сохраненные документы Webi. Чтение из сериализованного файла.
     * */
    private List<Webi> readWebies() throws IOException {
        List<Webi> webies = new ArrayList<>();
        File dataFile = new File("result\\data\\Webi_" + BOConnection.getHostname() + ".data");
        if (dataFile.exists()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFile))){
                Object object = objectInputStream.readObject();
                if (object instanceof List) {
                    webies.addAll((List<Webi>)object);
                }
            } catch(ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return webies;
    }

    /**
     * Восстановить загруженные документы Webi после неудачного завершения программы
     * */
    private List<Webi> recoverWebies() throws IOException {
        List<Webi> recoveryList = new ArrayList<>();
        try {
            File tempFile = new File("result\\data\\Webi_" + BOConnection.getHostname() + ".temp");
            if (tempFile.exists()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(tempFile));
                while (true) {
                    try {
                        Webi webi = (Webi) objectInputStream.readObject();
                        recoveryList.add(webi);
                    } catch (EOFException e) {
                        break;
                    }
                }
                objectInputStream.close();
            }
            tempObjectOutputStream = new ObjectOutputStream(new FileOutputStream("result\\data\\Webi_" + BOConnection.getHostname() + ".temp"));
            for (Webi webi : recoveryList) {
                tempObjectOutputStream.writeObject(webi);
                tempObjectOutputStream.flush();
            }
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return recoveryList;
    }


    /**
     * Запросить информацию по документам Webi
     * */
    private List<Webi> requestWebiesInfo(List<Webi> webiList) throws IOException, ParseException, InterruptedException {
        Log.out("Обновляю информацию по документам Webi.");
        int count = 1;
        for (Webi webi : webiList) {
            Thread.sleep(0);
            webi.requestWebiInfo();
            tempObjectOutputStream.writeObject(webi);
            tempObjectOutputStream.flush();

            if (GUI.hasInit()) {
                GUI.get().getRunPanel().setStatus("Обновлено " + count++ + " отчетов из " + webiList.size());
            }
        }
        Log.out("Обновление документов Webi завершено.");
        return webiList;
    }

    /**
     * Записать отчет на диск
     * */
    private void writeReport(String name, List<Webi> webiList, List<String> tablesFilter, boolean withSQL) throws IOException {
        Log.out("Записываю отчет на диск.");
        int count = 1;
        boolean useFilter = tablesFilter.size() > 0;
        String separator = ";";
        String fileName = "ReportFindSqlTable_" + (useFilter?"filtered_":"") + (withSQL?"withSQL_":"") + name + ".csv";
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter("result\\reports\\" + fileName));
        fileWriter.write("\"ID\"" + separator + "\"TABNAME\"" + separator + "\"REPID\"" + separator + "\"REPNAME\"" + separator + "\"CREATE_TS\"" + separator + "\"LAST_UPDATE_TS\"" + (withSQL?(separator + "\"SQL\""):"") + System.lineSeparator());

        for (Webi webi : webiList) {
            Log.out("Записываю отчет Webi " + webi.toString());
            for (String sql : webi.getSqlQueries()) {
                List<String> tables = new ArrayList<>();
                try {
                    Parser.parse(sql);
                    tables.addAll(Parser.getTables());
                } catch(JSQLParserException ex) {
                    Log.error("Ошибка парсинга");
                    tables.add("Ошибка парсинга");
                }

                for (String tableName : tables) {
                    String line = "\"" + count + "\"" + separator +
                            "\"" + tableName + "\"" + separator +
                            "\"" + webi.getId() + "\"" + separator +
                            "\"" + webi.getName() + "\"" + separator +
                            "\"" + webi.getCreationTime() + "\"" + separator +
                            "\"" + webi.getUpdateTs() + "\"" +
                            (withSQL?(separator + "\"" + Helper.prepareReportSQL(sql) + "\""):"") +
                            System.lineSeparator();
                    if (useFilter) {
                        if (tablesFilter.contains(tableName.trim().toUpperCase())) {
                            fileWriter.write(line);
                        }
                    } else {
                        fileWriter.write(line);
                    }
                }
            }
            if (GUI.hasInit()) {
                GUI.get().getRunPanel().setStatus("Обработано " + count++ + " отчетов из " + webiList.size());
            }
        }
        fileWriter.close();
        Log.out("Отчет записан на диск.");
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
