package com.attestator.common.shared.vo;

import com.attestator.common.server.db.annotation.Reference;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;

@Entity("printingProperties")
public class PrintingPropertiesVO extends TenantableVO implements PublicationsTreeItem {
    private static final long serialVersionUID = -8206061763678113027L;    
    
    private String          metatestId;
    
    @Transient
    @Reference(fromField = "metatestId",  excludeFields = {"entries"})
    private MetaTestVO      metatest;

    private String          titlePage;
    private Boolean         randomQuestionsOrder;
    
    private Integer         printAttempt = 1;
    
    public String getMetatestId() {
        return metatestId;
    }
    public void setMetatestId(String metatestId) {
        this.metatestId = metatestId;
    }
    public MetaTestVO getMetatest() {
        return metatest;
    }
    public void setMetatest(MetaTestVO metatest) {
        this.metatest = metatest;
    }
    public String getTitlePage() {
        return titlePage;
    }
    public void setTitlePage(String titlePage) {
        this.titlePage = titlePage;
    }
    public Boolean getRandomQuestionsOrder() {
        return randomQuestionsOrder;
    }
    public void setRandomQuestionsOrder(Boolean randomQuestionsOrder) {
        this.randomQuestionsOrder = randomQuestionsOrder;
    }
    public Integer getPrintAttempt() {
        return printAttempt;
    }
    public void setPrintAttempt(Integer printAttempt) {
        this.printAttempt = printAttempt;
    }
}
