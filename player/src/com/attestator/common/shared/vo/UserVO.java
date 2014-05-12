package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Entity;


@Entity("user")
public class UserVO extends TenantableVO {
    private static final long serialVersionUID = -2277743477030122995L;

    private String defaultGroupId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String organization;
    private String email;
    private String username;
    private String password;

    public UserVO() {
        setTenantId(getId());
        defaultGroupId = getId();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public void setDefaultGroupId(String defaultGroupId) {
        this.defaultGroupId = defaultGroupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}