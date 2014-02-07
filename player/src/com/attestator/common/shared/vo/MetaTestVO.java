package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;

import com.attestator.common.server.db.annotation.PostUpdate;
import com.attestator.common.shared.helper.NullHelper;

@Entity("metatest")
public class MetaTestVO extends ShareableVO implements PublicationsTreeItem {
	private static final long serialVersionUID = 4340732034670583318L;
	
    private String                 name;    
	private List<MetaTestEntryVO>  entries = new ArrayList<MetaTestEntryVO>();
	private Integer                numberOfQuestions;
	private List<SharingEntryVO>   sharingEntries = new ArrayList<SharingEntryVO>();
	
    public List<MetaTestEntryVO> getEntries() {
        return entries;
    }
    public void setEntries(List<MetaTestEntryVO> entries) {
        this.entries = entries;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }
    public int getNumberOfQuestionsOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(numberOfQuestions);
    }
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
    public List<SharingEntryVO> getSharingEntries() {
        return sharingEntries;
    }
    public void setSharingEntries(List<SharingEntryVO> sharingEntries) {
        this.sharingEntries = sharingEntries;
    }
    
    @SuppressWarnings("unused")
    @PrePersist
    @PostUpdate
    private void prePersist() {
        int count = 0;
        for (MetaTestEntryVO entry : entries) {
            count += entry.getNumberOfQuestionsOrZero();
        }
        numberOfQuestions = count;
    }
    
    @Override
    public void resetIdentity() {        
        super.resetIdentity();
        if (entries != null) {
            for (MetaTestEntryVO entry: entries) {
                entry.resetIdentity();
            }
        }
    }
    @Override
    public String toString() {
        return "MetaTestVO [name=" + name + ", numberOfQuestions="
                + numberOfQuestions + ", getTenantId()=" + getTenantId()
                + ", getCreated()=" + getCreated() + ", getModified()="
                + getModified() + ", getId()=" + getId() + "]";
    }
}