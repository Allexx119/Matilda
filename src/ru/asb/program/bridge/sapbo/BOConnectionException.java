package ru.asb.program.bridge.sapbo;

import org.apache.http.StatusLine;

public class BOConnectionException extends Exception {
    private int statusCode;
    private String reasonPhrase;

    public BOConnectionException(StatusLine statusLine) {
        statusCode = statusLine.getStatusCode();
        reasonPhrase = statusLine.getReasonPhrase();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String getMessage() {
        return "Request error: " + statusCode + " " + reasonPhrase;
    }
}
