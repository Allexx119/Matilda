package ru.asb.program.operation.records;

public class Parent extends Record{
    private String path;
    private String rootFolder;

    public String getPath() {
        return path;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }
}
