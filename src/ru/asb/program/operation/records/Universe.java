package ru.asb.program.operation.records;

import java.util.ArrayList;
import java.util.List;

public class Universe {
    private String id;
    private String name;
    private String kind;
    private List<String> webiesId = new ArrayList<>();


    public boolean addWebi(String webiId) {
        return webiesId.add(webiId);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public List<String> getWebiesId() {
        return webiesId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

}
