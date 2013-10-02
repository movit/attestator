package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.List;

import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.PrePersist;

@Entity("metatest")
public class MetaTestVO extends ModificationDateAwareVO implements PublicationsTreeItem {
	private static final long serialVersionUID = 4340732034670583318L;
	
    private String                 name;    
	private List<MetaTestEntryVO>  entries = new ArrayList<MetaTestEntryVO>();
	private Integer                numberOfQuestions;
  
//	@Transient
//    @ReferenceCount(toClass=PublicationVO.class, toField="metatestId")
//    private Integer                publicationsCount;
	
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
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
//    public Integer getPublicationsCount() {
//        return publicationsCount;
//    }
//    public void setPublicationsCount(Integer publicationsCount) {
//        this.publicationsCount = publicationsCount;
//    }
    @SuppressWarnings("unused")
    @PrePersist
    private void prePersist() {
        int count = 0;
        for (MetaTestEntryVO entry : entries) {
            count += NullHelper.nullSafeIntegerOrZerro(entry.getNumberOfQuestions());
        }
        numberOfQuestions = count;
    }    
}