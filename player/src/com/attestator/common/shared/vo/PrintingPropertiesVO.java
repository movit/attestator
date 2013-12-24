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

    private String          titlePage = "<div style=\"text-align: center;\">\r\n" + 
    		"    <span style=\"font-family: Georgia, 'Times New Roman', Times, serif;\"><font size=\"5\"><br></font></span>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: center;\">\r\n" + 
    		"    <span style=\"font-family: Georgia, 'Times New Roman', Times, serif;\"><font size=\"5\"><br></font></span>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: center;\">\r\n" + 
    		"    <span style=\"font-family: Georgia, 'Times New Roman', Times, serif;\"><font size=\"5\">{test}</font></span>\r\n" + 
    		"</div>\r\n" +
    		"<div style=\"text-align: center;\">\r\n" + 
            "    <span style=\"font-family: Georgia, 'Times New Roman', Times, serif;\"><font size=\"5\"><br></font></span>\r\n" + 
            "</div>\r\n" +
    		"<div style=\"text-align: center;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\">Вариант {variant}</font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\">&nbsp;&nbsp;&nbsp;&nbsp;Фамилия<span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t<u><span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t\t\t\t\t\t\t\t\t\t\t</span></u></span></font></div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\">&nbsp;&nbsp;&nbsp;&nbsp;Имя<span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t\t<u><span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t\t\t\t\t\t\t\t\t\t\t</span></u></span></font></div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\"><br></font>\r\n" + 
    		"</div>\r\n" + 
    		"<div style=\"text-align: left;\">\r\n" + 
    		"    <font face=\"Georgia, 'Times New Roman', Times, serif\" size=\"4\">&nbsp;&nbsp;&nbsp;&nbsp;Отчество<span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t<u><span class=\"Apple-tab-span\" style=\"white-space:pre\">\t\t\t\t\t\t\t\t\t\t\t\t</span></u></span></font></div>";
    
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
    public void setVariantsCount(Integer variantsCount) {
        this.variantsCount = variantsCount;
    }    
}
