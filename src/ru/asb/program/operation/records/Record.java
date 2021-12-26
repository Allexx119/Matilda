package ru.asb.program.operation.records;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public abstract class Record implements Serializable {
    protected String id;
    protected String name;
    protected String kind;

    protected Timestamp creationTime;
    protected Timestamp updateTs;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getUpdateTs() {
        return updateTs;
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

    public void setCreationTime(String creationTime, String format) {
        if (!creationTime.isEmpty()) {
            LocalDateTime dateTime = LocalDateTime.parse(creationTime, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
            this.creationTime = Timestamp.valueOf(dateTime);
        }
    }

    public void setCreationTime(String creationTime, String format, String altFormat) {
        if (!creationTime.isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(creationTime, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
                this.creationTime = Timestamp.valueOf(dateTime);
            } catch(DateTimeParseException e) {
                LocalDateTime dateTime = LocalDateTime.parse(creationTime, DateTimeFormatter.ofPattern(altFormat, Locale.ENGLISH));
                this.creationTime = Timestamp.valueOf(dateTime);
            }
        }
    }

    public void setUpdateTs(String updateTs, String format) {
        if (!updateTs.isEmpty()) {
            LocalDateTime dateTime = LocalDateTime.parse(updateTs, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
            this.updateTs = Timestamp.valueOf(dateTime);
        }
    }

    public void setUpdateTs(String updateTs, String format, String altFormat) {
        if (!updateTs.isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(updateTs, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
                this.updateTs = Timestamp.valueOf(dateTime);
            } catch(DateTimeParseException e) {
                LocalDateTime dateTime = LocalDateTime.parse(updateTs, DateTimeFormatter.ofPattern(altFormat, Locale.ENGLISH));
                this.updateTs = Timestamp.valueOf(dateTime);
            }
        }
    }
}
