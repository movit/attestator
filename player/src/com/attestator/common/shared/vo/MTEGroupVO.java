package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Transient;

import com.attestator.common.server.db.annotation.Reference;

public class MTEGroupVO extends MetaTestEntryVO {
    private static final long serialVersionUID = -2665975538192261095L;

    private String            groupId;
    @Transient
    @Reference(fromField = "groupId")
    private GroupVO           group;
    private Integer           numberOfQuestions;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public GroupVO getGroup() {
        return group;
    }

    public void setGroup(GroupVO group) {
        this.group = group;
    }
}