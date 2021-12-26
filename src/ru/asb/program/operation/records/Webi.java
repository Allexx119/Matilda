package ru.asb.program.operation.records;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.sapbo.BOConnectionException;
import ru.asb.program.bridge.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Webi extends Record {
    private List<DataProvider> dataProviders = new ArrayList<>();

    public void requestWebiInfo() throws IOException, ParseException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            try {
                requestDataProviders();
                requestSqlQueries();
                Log.out("Получены SQL запросы документа WebI " + this.toString());
                break;
            } catch(BOConnectionException ex) {
                Log.out(ex.getMessage() + " Attempt " + (i+1) + " to reconnect.");
                switch (ex.getStatusCode()) {
                    case (503) : Thread.sleep(500); break;
                    case (401) : BOConnection.logoff(); BOConnection.logon(); break;
                }
            }
        }
    }

    /**
     * Заполнить DataProviders
     * */
    private void requestDataProviders() throws IOException, ParseException, BOConnectionException {
        try {
            //Generate request
            String requestURL = BOConnection.getBaseURL() + "/raylight/v1/documents/" + id + "/dataproviders";
            String jsonString = BOConnection.execute(requestURL);

            //Parse JSON and get required info
            Object obj = new JSONParser().parse(jsonString);
            JSONObject json = (JSONObject) obj;

            JSONArray dataProviderArray = (JSONArray) ((JSONObject) json.get("dataproviders")).get("dataprovider");

            for (Object object : dataProviderArray) {
                JSONObject dataProviderObject = (JSONObject) object;
                DataProvider dp = new DataProvider(
                        String.valueOf(dataProviderObject.get("id")),
                        String.valueOf(dataProviderObject.get("name")),
                        String.valueOf(dataProviderObject.get("dataSourceId")),
                        String.valueOf(dataProviderObject.get("dataSourceType")),
                        String.valueOf(dataProviderObject.get("updated"))
                );
                dataProviders.add(dp);
            }
        } catch(NullPointerException ex) {
//            System.out.println(json);
            ex.printStackTrace();
        }
    }

    /**
     * Запросить SQL запросы
     * */
    private void requestSqlQueries() throws IOException, ParseException, BOConnectionException {
        for (DataProvider dp : dataProviders) {
            //Generate request
            try {
                //Generate request
                String requestURL = BOConnection.getBaseURL() + "/raylight/v1/documents/" + id + "/dataproviders/" + dp.getId();
                String jsonString = BOConnection.execute(requestURL);

                //Parse JSON and get required info
                Object obj = new JSONParser().parse(jsonString);
                JSONObject json = (JSONObject) obj;

                String query = String.valueOf(((JSONObject) json.get("dataprovider")).get("query"));
                dp.setQuery(query);
            } catch(NullPointerException ex) {
                Log.error("\"query\" attribute not found.");
//                System.out.println(json);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Получить все SQL запросы WebI
     * */
    public List<String> getSqlQueries() {
        List<String> queries = new ArrayList<>();
        for (DataProvider dp : dataProviders) {
            queries.add(dp.getQuery());
        }
        return queries;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Queries count: " + getSqlQueries().size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Webi) {
            return this.id.equals(((Webi) obj).id)
                && this.name.equals(((Webi) obj).name)
                && this.updateTs.equals(((Webi) obj).updateTs)
                && this.creationTime.equals(((Webi) obj).creationTime);
        }
        return false;
    }
}
