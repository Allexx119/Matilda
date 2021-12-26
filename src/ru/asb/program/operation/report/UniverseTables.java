package ru.asb.program.operation.report;

import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.sapbo.BOConnectionException;
import ru.asb.program.bridge.util.Log;

import java.io.IOException;

public class UniverseTables implements Runnable {
    private String reportQuery = "SELECT SI_ID, SI_NAME, SI_KIND, SI_CREATION_TIME, SI_UPDATE_TS from CI_INFOOBJECTS where (SI_KIND = 'Webi')"; //"SELECT SI_ID, SI_NAME, SI_KIND, SI_CREATION_TIME, SI_UPDATE_TS from CI_INFOOBJECTS where (SI_KIND = 'Webi')";
    @Override
    public void run() {
        try {
            Log.info("Find tables in Universes");


            BOConnection.logon();
            String getRequest = BOConnection.runCmsQuery(reportQuery);
            System.out.println(getRequest);
            BOConnection.execute("");
            BOConnection.logoff();
        } catch (IOException | BOConnectionException ioe) {
            ioe.printStackTrace();
        }


    }
}
