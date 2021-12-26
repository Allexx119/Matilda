package ru.asb.program.operation.records;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class User extends Record {
    private String userFullName;
    private String emailAddress;
    private String appointment;
    private String administration;
    private String division;
    private String direction;
    private String department;
    private String office;
    private String group;
    private Timestamp lastLogonTime;
    //CUSTOM_MAPPED_ATTRIBUTES
    private String tn;
    private String domain;
    private String dept;
    private String disabledId;
    private Timestamp disabledTime;

    public String[] getRow() {
        String[] row = {
                id,
                name,
                userFullName,
                emailAddress,
                disabledId,
                String.valueOf(creationTime),
                String.valueOf(lastLogonTime),
                String.valueOf(updateTs),
                String.valueOf(disabledTime),
                tn,
                appointment,
                domain,
                dept
        };
        return row;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAppointment() {
        return appointment;
    }

    public String getAdministration() {
        return administration;
    }

    public String getDivision() {
        return division;
    }

    public String getDirection() {
        return direction;
    }

    public String getDepartment() {
        return department;
    }

    public String getOffice() {
        return office;
    }

    public String getGroup() {
        return group;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastlogontime() {
        return lastLogonTime;
    }

    public Timestamp getUpdateTs() {
        return updateTs;
    }

    public String getTn() {
        return tn;
    }

    public String getDomain() {
        return domain;
    }

    public String getDept() {
        return dept;
    }

    public String getDisabledId() {
        return disabledId;
    }

    public Timestamp getDisabledTime() {
        return disabledTime;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setAppointment(String appointment) {
        this.appointment = appointment;
    }

    public void setAdministration(String administration) {
        this.administration = administration;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setLastLogonTime(String lastlogontime, String format, String altFormat) {
        if (!lastlogontime.isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(lastlogontime, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
                this.lastLogonTime = Timestamp.valueOf(dateTime);
            } catch(DateTimeParseException e) {
                LocalDateTime dateTime = LocalDateTime.parse(lastlogontime, DateTimeFormatter.ofPattern(altFormat, Locale.ENGLISH));
                this.lastLogonTime = Timestamp.valueOf(dateTime);
            }
        }
    }

    public void setDisabledTime(String disabledTime, String format, String altFormat) {
        if (!disabledTime.isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDate.parse(disabledTime, DateTimeFormatter.ofPattern(format, Locale.ENGLISH)).atStartOfDay().plusHours(7);
                this.disabledTime = Timestamp.valueOf(dateTime);
            } catch(DateTimeParseException e) {
                LocalDateTime dateTime = LocalDate.parse(disabledTime, DateTimeFormatter.ofPattern(altFormat, Locale.ENGLISH)).atStartOfDay().plusHours(7);
                this.disabledTime = Timestamp.valueOf(dateTime);
            }
        }
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setDisabledId(String disabledId) {
        this.disabledId = disabledId;
    }
}
