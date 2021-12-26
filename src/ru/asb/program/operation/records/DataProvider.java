package ru.asb.program.operation.records;

import java.io.Serializable;

public class DataProvider implements Serializable {
    private String id;
    private String name;
    private String dataSourceId;
    private String dataSourceType;
    private String updateTs;
    private String query;

    DataProvider(String id, String name, String dataSourceId, String dataSourceType, String updateTs) {
        this.id = id;
        this.name = name;
        this.dataSourceId = dataSourceId;
        this.dataSourceType = dataSourceType;
        this.updateTs = updateTs;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public String getUpdateTs() {
        return updateTs;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + dataSourceType;
    }
}
