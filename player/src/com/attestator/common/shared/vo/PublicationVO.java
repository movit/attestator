package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.common.server.db.annotation.Reference;
import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;

@Entity("publication")
public class PublicationVO extends TenantableVO {
    private static final long serialVersionUID = -8206061763678113027L;    
    
    private String          metatestId;
    
    @Transient
    @Reference(fromField = "metatestId",  excludeFields = {"entries"})
    private MetaTestVO      metatest;

    private Date            start;
    private Date            end;
    
    private String          introduction;

    private Boolean         askFirstName;
    private Boolean         askFirstNameRequired;
    
    private Boolean         askLastName;
    private Boolean         askLastNameRequired;

    private Boolean         askMiddleName;
    private Boolean         askMiddleNameRequired;

    private Boolean         askEmail;
    private Boolean         askEmailRequired;
    
    private Integer         maxAttempts = 0;

    private Double			minScore = 0d;
    private Boolean 		interruptOnFalure;
    
    private Long            maxTakeTestTime;
    private Long            maxQuestionAnswerTime;
    private Boolean         allowSkipQuestions = false;  
    private Boolean         allowInterruptTest = false;  
    private Boolean         randomQuestionsOrder = false;
    
    private List<AdditionalQuestionVO> additionalQuestions = new ArrayList<AdditionalQuestionVO>();    
    private List<QuestionVO> questions = new ArrayList<QuestionVO>();
    
    public String getMetatestId() {
        return metatestId;
    }

    public void setMetatestId(String metatestId) {
        this.metatestId = metatestId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<AdditionalQuestionVO> getAdditionalQuestions() {
        return additionalQuestions;
    }

    public void setAdditionalQuestions(List<AdditionalQuestionVO> additionalQuestions) {
        this.additionalQuestions = additionalQuestions;
    }

    public MetaTestVO getMetatest() {
        return metatest;
    }

    public void setMetatest(MetaTestVO metatest) {
        this.metatest = metatest;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public boolean isAskFirstName() {
        return NullHelper.nullSafeTrue(askFirstName);
    }

    public Boolean getAskFirstName() {
        return askFirstName;
    }

    public void setAskFirstName(Boolean askFirstName) {
        this.askFirstName = askFirstName;
    }

    public boolean isAskFirstNameRequired() {
        return NullHelper.nullSafeTrue(askFirstNameRequired);
    }
    
    public Boolean getAskFirstNameRequired() {
        return askFirstNameRequired;
    }

    public void setAskFirstNameRequired(Boolean askFirstNameReqired) {
        this.askFirstNameRequired = askFirstNameReqired;
    }

    public boolean isAskLastName() {
        return NullHelper.nullSafeTrue(askLastName);
    }
    
    public Boolean getAskLastName() {
        return askLastName;
    }

    public void setAskLastName(Boolean askLastName) {
        this.askLastName = askLastName;
    }

    public boolean isAskLastNameRequired() {
        return NullHelper.nullSafeTrue(askLastNameRequired);
    }
    
    public Boolean getAskLastNameRequired() {
        return askLastNameRequired;
    }

    public void setAskLastNameRequired(Boolean askLastNameRequired) {
        this.askLastNameRequired = askLastNameRequired;
    }

    public boolean isAskMiddleName() {
        return NullHelper.nullSafeTrue(askMiddleName);
    }
    
    public Boolean getAskMiddleName() {
        return askMiddleName;
    }

    public void setAskMiddleName(Boolean askMiddleName) {
        this.askMiddleName = askMiddleName;
    }

    public boolean isAskMiddleNameRequired() {
        return NullHelper.nullSafeTrue(askMiddleNameRequired);
    }
    
    public Boolean getAskMiddleNameRequired() {
        return askMiddleNameRequired;
    }

    public void setAskMiddleNameRequired(Boolean askMiddleNameRequired) {
        this.askMiddleNameRequired = askMiddleNameRequired;
    }

    public boolean isAskEmail() {
        return NullHelper.nullSafeTrue(askEmail);
    }
    
    public Boolean getAskEmail() {
        return askEmail;
    }

    public void setAskEmail(Boolean askEmail) {
        this.askEmail = askEmail;
    }

    public boolean isAskEmailRequired() {
        return NullHelper.nullSafeTrue(askEmailRequired);
    }
    
    public Boolean getAskEmailRequired() {
        return askEmailRequired;
    }

    public void setAskEmailRequired(Boolean askEmailRequired) {
        this.askEmailRequired = askEmailRequired;
    }
    
    public boolean isUnlimitedAttempts() {
        return NullHelper.nullSafeIntegerOrZerro(maxAttempts) == 0;
    }
    
    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public AdditionalQuestionVO getAdditionalQuestionById(String id) {
        for (AdditionalQuestionVO aq: additionalQuestions) {
            if (NullHelper.nullSafeEquals(id, aq.getId())) {
                return aq;
            }
        }
        return null;
    }
    
    public     List<QuestionVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionVO> questions) {
        this.questions = questions;
    }
    
    public QuestionVO getQuestion(String questionId) {
        for (QuestionVO question: questions) {
            if (NullHelper.nullSafeEquals(question.getId(), questionId)) {
                return question;
            }
        }
        return null;
    }

    public Long getMaxTakeTestTime() {
        return maxTakeTestTime;
    }
    public void setMaxTakeTestTime(Long maxTakeTestTime) {
        this.maxTakeTestTime = maxTakeTestTime;
    }
    public Long getMaxQuestionAnswerTime() {
        return maxQuestionAnswerTime;
    }
    public void setMaxQuestionAnswerTime(Long maxQuestionAnswerTime) {
        this.maxQuestionAnswerTime = maxQuestionAnswerTime;
    }
    public Boolean getAllowSkipQuestions() {
        return allowSkipQuestions;
    }
    public boolean isAllowSkipQuestions() {
        return NullHelper.nullSafeTrue(allowSkipQuestions);
    }
    public void setAllowSkipQuestions(Boolean allowSkipQuestions) {
        this.allowSkipQuestions = allowSkipQuestions;
    }
    public boolean isRandomQuestionsOrder() {
        return NullHelper.nullSafeTrue(allowSkipQuestions);
    }
    public Boolean getRandomQuestionsOrder() {
        return randomQuestionsOrder;
    }
    public void setRandomQuestionsOrder(Boolean randomQuestionsOrder) {
        this.randomQuestionsOrder = randomQuestionsOrder;
    }
    public boolean isAllowInterruptTest() {
        return NullHelper.nullSafeTrue(allowInterruptTest);
    }
    public Boolean getAllowInterruptTest() {
        return allowInterruptTest;
    }
    public void setAllowInterruptTest(Boolean allowInterruptTest) {
        this.allowInterruptTest = allowInterruptTest;
    }    
    public double getThisMinScore() {
        return NullHelper.nullSafeDoubleOrZerro(minScore);
    }    
    public Double getMinScore() {
		return minScore;
	}
	public void setMinScore(Double minScore) {
		this.minScore = minScore;
	}
	public Boolean getInterruptOnFalure() {
		return interruptOnFalure;
	}
	public void setInterruptOnFalure(Boolean interruptOnFalure) {
		this.interruptOnFalure = interruptOnFalure;
	}
	public boolean isThisInterruptOnFalure(){
		return NullHelper.nullSafeTrue(interruptOnFalure);
	}

	@Override
    public String toString() {
        return "PublicationVO [metatestId=" + metatestId + ", metatest="
                + metatest + ", start=" + start + ", end=" + end
                + ", introduction=" + introduction + ", askFirstName="
                + askFirstName + ", askFirstNameRequired="
                + askFirstNameRequired + ", askLastName=" + askLastName
                + ", askLastNameRequired=" + askLastNameRequired
                + ", askMiddleName=" + askMiddleName
                + ", askMiddleNameRequired=" + askMiddleNameRequired
                + ", askEmail=" + askEmail + ", askEmailRequired="
                + askEmailRequired + ", maxAttempts=" + maxAttempts
                + ", maxTakeTestTime=" + maxTakeTestTime
                + ", maxQuestionAnswerTime=" + maxQuestionAnswerTime
                + ", allowSkipQuestions=" + allowSkipQuestions
                + ", allowInterruptTest=" + allowInterruptTest
                + ", randomQuestionsOrder=" + randomQuestionsOrder
                + ", additionalQuestions=" + additionalQuestions
                + ", questions=" + questions + ", getTenantId()="
                + getTenantId() + ", getId()=" + getId() + "]";
    }
}
