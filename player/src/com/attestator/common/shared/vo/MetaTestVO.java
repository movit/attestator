package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;

@Entity("metatest")
public class MetaTestVO extends ShareableVO implements PublicationsTreeItem {
	private static final long serialVersionUID = 4340732034670583318L;
	
    private String                 name;    
	private List<MetaTestEntryVO>  entries = new ArrayList<MetaTestEntryVO>();
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
    public int getNumberOfQuestionsOrZero() {
        int result = 0;
        if (entries != null) {
            for (MetaTestEntryVO entry : entries) {
                result += entry.getNumberOfQuestionsOrZero();
            }
        }
        return result;
    }
    public List<SharingEntryVO> getSharingEntries() {
        return sharingEntries;
    }
    public void setSharingEntries(List<SharingEntryVO> sharingEntries) {
        this.sharingEntries = sharingEntries;
    }
    
    @Override
    public void resetIdentity() {        
        super.resetIdentity();
        if (entries != null) {
            for (MetaTestEntryVO entry: entries) {
                entry.resetIdentity();
            }
        }
        if (sharingEntries != null) {
            sharingEntries = new ArrayList<SharingEntryVO>();
        }
    }
    @Override
    public String toString() {
        return "MetaTestVO [name=" + name + ", entries=" + entries
                + ", sharingEntries=" + sharingEntries
                + ", getNumberOfQuestionsOrZero()="
                + getNumberOfQuestionsOrZero() + ", getSharedForTenantIds()="
                + getSharedForTenantIds() + ", getOwnerUsername()="
                + getOwnerUsername() + ", getTenantId()=" + getTenantId()
                + ", getCreated()=" + getCreated() + ", getModified()="
                + getModified() + ", getId()=" + getId() + "]";
    }
    
    
}