package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;

@Entity("metatest")
public class MetaTestVO extends TenantableVO implements Comparable<MetaTestVO>{
	private static final long serialVersionUID = 4340732034670583318L;
	
    private String                 name;    
	private List<MetaTestEntryVO>  entries = new ArrayList<MetaTestEntryVO>();
	
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
    
    @Override
    public int compareTo(MetaTestVO other) {
        
        if (getId() != null && other.getId() != null ) {
            return getId().compareTo(other.getId()); 
        }
        
        return hashCode() - other.hashCode(); 
    }
    
}