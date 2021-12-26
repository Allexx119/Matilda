package ru.asb.program.bridge.sapbo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import ru.asb.program.bridge.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class BOConnection {
    private static HttpClient httpClient = HttpClientBuilder.create().build();
    private static String logonToken = "";
    private static String hostname = null;
    private static String baseURL = null;
    private static String username = null;
    private static String password = null;
    private static String clientType = null;
    private static String authenticationType = null;
    private static final int pagesize = 100000;

    /**
     * Установка соединения и вход в SAP BO. Получение Login Token
     * */
    public static String logon() throws IOException {
        Log.out("Connecting SAP BO...");
        //Generate auth request
        HttpPost authRequest = new HttpPost(baseURL + "/logon/long");
        authRequest.setHeader(HttpHeaders.ACCEPT, "application/json");
        authRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        String authJson = "{\"password\":\"" + password + "\"," +
                "\"clientType\":\"" + clientType + "\"," +
                "\"auth\":\"" + authenticationType + "\"," +
                "\"userName\":\"" + username + "\"}";
        HttpEntity authEntity = new ByteArrayEntity(authJson.getBytes("UTF-8"));
        authRequest.setEntity(authEntity);
        //Execute auth request
        HttpResponse response = httpClient.execute(authRequest);
        StatusLine status = response.getStatusLine();
        response.getEntity().getContent().close();
        if (status.getStatusCode() == 200) {
            logonToken = response.getFirstHeader("X-SAP-LogonToken").getValue();
            Log.out("Connection to SAP BO server " + hostname + " is established!");
        } else {
            Log.error("Connecting failed! Response status: " + status);
        }
        return status.toString();
    }

    /**
     * Выход из SAP BO
     * */
    public static void logoff() throws IOException{
        HttpPost logoff = new HttpPost(baseURL + "/logoff");
        logoff.setHeader(HttpHeaders.ACCEPT, "application/xml");
        logoff.addHeader("X-SAP-LogonToken", logonToken);
        HttpResponse response = httpClient.execute(logoff);
        StatusLine status = response.getStatusLine();
        response.getEntity().getContent().close();
        if (status.getStatusCode() == 200) {
            Log.out("Connection to SAP BO server " + hostname + " is closed!");
            logonToken = "";
        } else {
            Log.error("Logoff failed! Response status: " + status);
        }
    }

    public static boolean test() throws IOException{
        String logonStatus = logon();
        if (logonStatus.contains("200")) {
            logoff();
            return true;
        }
        return false;
    }


    /**
     * выполнить CMS запрос
     * */
    public static String runCmsQuery(String query) throws IOException, BOConnectionException {
        HttpPost cmsQuery = new HttpPost(baseURL + "/v1/cmsquery?page=1&pagesize=" + pagesize);
        cmsQuery.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        cmsQuery.addHeader(HttpHeaders.ACCEPT, "application/json");
        cmsQuery.addHeader("X-SAP-LogonToken", logonToken);
        String requestJson = "{\"query\":\"" + query + "\"}";

        HttpEntity cmsQueryEntity = new ByteArrayEntity(requestJson.getBytes(StandardCharsets.UTF_8));
        cmsQuery.setEntity(cmsQueryEntity);

        HttpResponse response = httpClient.execute(cmsQuery);
        HttpEntity entity = response.getEntity();
        String responseJson = EntityUtils.toString(entity);
        StatusLine status = response.getStatusLine();
        response.getEntity().getContent().close();
        if (status.getStatusCode() != 200) {
            throw new BOConnectionException(status);
        }
        return responseJson;
    }

    /**
     * Получить информацию о документе SAP BO
     * */
    public String getInfo(String docId) throws IOException, BOConnectionException {
        //Generate request
        HttpGet getDocInfo = new HttpGet(baseURL + "/raylight/v1/documents/" + docId);
        getDocInfo.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getDocInfo.setHeader(HttpHeaders.ACCEPT, "application/json");
        getDocInfo.addHeader("X-SAP-LogonToken", logonToken);

        //Execute request and receive  response entity
        HttpResponse response = httpClient.execute(getDocInfo);
        String jsonString = EntityUtils.toString(response.getEntity());
        StatusLine status = response.getStatusLine();
        response.getEntity().getContent().close();
        if (status.getStatusCode() != 200) {
            throw new BOConnectionException(status);
        }
        return jsonString;
    }



//    /**
//     * Шаблоны запросов
//     * Получить шаблон для выполнения CMS запроса к SAP BO через RESTful
//     * */
////    private void getCmsQueryTemplate() throws IOException {
////        HttpGet cmsQuery = new HttpGet(baseURL + "/v1/cmsquery");
////        cmsQuery.setHeader(HttpHeaders.ACCEPT, "application/json");
////        cmsQuery.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
////        cmsQuery.addHeader("X-SAP-LogonToken", logonToken);
////        execute(cmsQuery);
////    }
////
//    /**
//     * Получить шаблон для выполнения входа в SAP BO через RESTful
//     * */
////    private void getLogOnTemplate() throws IOException {
////        HttpGet logon = new HttpGet(baseURL + "/logon/long");
////        logon.setHeader(HttpHeaders.ACCEPT, "application/json");
////        logon.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
////        logon.addHeader("X-SAP-LogonToken", logonToken);
////        execute(logon);
////    }
//    /**
//     * Выполнить запрос и посмотерть результат выполнения
//     * */
//    private String execute(HttpUriRequest request) throws IOException {
//        HttpResponse response = httpClient.execute(request);
//        // Get HttpResponse Status
//        System.out.println("-------------------------");
//        System.out.println("Reponse status: " + response.getStatusLine().toString());
//        System.out.println("Headers:");
//        for (Header header : response.getAllHeaders()) {
//            System.out.println(header.toString());
//        }
//        HttpEntity entity = response.getEntity();
//        String responseString = EntityUtils.toString(entity);
//        System.out.println("\nResult:\n" + responseString);
//        FileUtil.writeFile(responseString, "output\\response.xml", false);
//        System.out.println("-------------------------");
//        return responseString;
//    }

    /**
     * Выполнить GET запрос и посмотерть результат выполнения
     * */
    public static String execute(String getRequest) throws IOException, BOConnectionException {
        HttpGet httpGet = new HttpGet(getRequest);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpGet.setHeader(HttpHeaders.ACCEPT, "application/json");
        httpGet.addHeader("X-SAP-LogonToken", BOConnection.getLogonToken());

        //Execute request and receive  response entity
        HttpResponse response = BOConnection.getHttpClient().execute(httpGet);
        String jsonString = EntityUtils.toString(response.getEntity());
        StatusLine status = response.getStatusLine();
        response.getEntity().getContent().close();
        if (status.getStatusCode() != 200) {
            throw new BOConnectionException(status);
        }
        return jsonString;
    }

    public static String getBaseURL() {
        return baseURL;
    }

    public static String getLogonToken() {
        return logonToken;
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }

    public static void setLogonInfo(String ip, String username, String password, String authType) throws UnknownHostException{
        InetAddress address = InetAddress.getByName(ip);
        String hostname = address.getHostName();
        BOConnection.hostname = hostname;
        BOConnection.baseURL = "http://" + hostname + ":6405/biprws";
        BOConnection.username = username;
        BOConnection.password = password;
        BOConnection.authenticationType = authType;
    }

    public static void printConnectionInfo() {
        System.out.println(
                "\nSAP BO Connection:\n" +
                "URL: " + BOConnection.baseURL +"\n" +
                "login: " + BOConnection.username + "\n" +
                "password: " + BOConnection.password + "\n" +
                "auth: " + BOConnection.authenticationType
        );
    }

    public static String getHostname() {
        return hostname;
    }
}

