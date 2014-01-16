package com.attestator.common.shared.vo;

import com.attestator.common.server.db.annotation.Reference;
import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;

@Entity("printingProperties")
public class PrintingPropertiesVO extends TenantableVO implements PublicationsTreeItem {
    private static final long serialVersionUID = -8206061763678113027L;    
    
    private String          metatestId;
    
    @Transient
    @Reference(fromField = "metatestId",  excludeFields = {"entries"})
    private MetaTestVO      metatest;

    private String          titlePage = "<br>\r\n" + 
    		"<br>\r\n" + 
    		"<br>\r\n" + 
    		"<br>\r\n" + 
    		"<h2 style=\"text-align: center;\">{test}</h2>\r\n" + 
    		"<h3 style=\"text-align: center;\">Вариант {variant}</h3>\r\n" + 
    		"<br>\r\n" + 
    		"<br>\r\n" + 
    		"<table><tbody>\r\n" + 
    		"<tr><td><h3>Фамилия</h3></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;________________________________________________________</td></tr>\r\n" + 
    		"<tr><td><h3>Имя</h3></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;________________________________________________________</td></tr>\r\n" + 
    		"<tr><td><h3>Отчество</h3></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;________________________________________________________</td></tr>\r\n" + 
    		"</tbody></table>\r\n";
    
    private Boolean         randomQuestionsOrder;
    
    private Integer         printAttempt = 0;
    
    private Integer         variantsCount = 1;
    
    private Boolean         doublePage;    
    
    private Boolean         onePdfPerVariant;
    
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
    public boolean isThisRandomQuestionsOrder() {
        return NullHelper.nullSafeTrue(randomQuestionsOrder);
    }
    public void setRandomQuestionsOrder(Boolean randomQuestionsOrder) {
        this.randomQuestionsOrder = randomQuestionsOrder;
    }
    public Integer getPrintAttempt() {
        return printAttempt;
    }
    public Integer getPrintAttemptOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(printAttempt);
    }
    public void setPrintAttempt(Integer printAttempt) {
        this.printAttempt = printAttempt;
    }
    public Boolean getDoublePage() {
        return doublePage;
    }
    public boolean isThisDoublePage() {
        return NullHelper.nullSafeTrue(doublePage);
    }
    public void setDoublePage(Boolean doublePage) {
        this.doublePage = doublePage;
    }
    public Boolean getOnePdfPerVariant() {
        return onePdfPerVariant;
    }
    public boolean isThisOnePdfPerVariant() {
        return NullHelper.nullSafeTrue(onePdfPerVariant);
    }
    public void setOnePdfPerVariant(Boolean onePdfPerVariant) {
        this.onePdfPerVariant = onePdfPerVariant;
    }
    public Integer getVariantsCount() {
        return variantsCount;
    }
    public int getVariantsCountOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(variantsCount);
    }    
    public void setVariantsCount(Integer variantsCount) {
        this.variantsCount = variantsCount;
    }    
}
