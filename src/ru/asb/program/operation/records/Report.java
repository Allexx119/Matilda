package ru.asb.program.operation.records;

import java.util.ArrayList;
import java.util.List;

public class Report extends Record {
    private String path;
    private String owner;
    private String parentId;
    private String rootFolder;
    private List<String> universesId = new ArrayList<>();
    private List<String> universes = new ArrayList<>();


    public void findPaths(List<Parent> parents) {
        for (Parent parent : parents) {
            if (parent.getId().equals(parentId)) {
                rootFolder = parent.getRootFolder();
                path = parent.getPath();
                break;
            }
        }
    }

    public String[] getRow() {
        String[] row = {
              rootFolder,
              path,
              name,
              id,
              kind,
              getUniverses(),
              owner,
              String.valueOf(creationTime),
              String.valueOf(updateTs)
        };
        return row;
    }

    public void addUniverse(String universe) {
        this.universes.add(universe);
    }

    public void addUniverseId(String universeId) {
        this.universesId.add(universeId);
    }

    public String getParentId() {
        return parentId;
    }

    public String getPath() {
        return path;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public String getOwner() {
        return owner;
    }

    public String getUniverses() {
        StringBuilder result = new StringBuilder();
        for (String universe : this.universes) {
            result.append(universe).append(", ");
        }
        if (result.length() < 2) {
            return result.toString();
        } else {
            return result.substring(0, result.length()-2);
        }

    }

    public List<String> getUniversesId() {
        return universesId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setUniverses(List<String> universes) {
        this.universes = universes;
    }
}
