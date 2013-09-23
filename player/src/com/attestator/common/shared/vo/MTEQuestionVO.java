package com.attestator.common.shared.vo;

public class MTEQuestionVO extends MetaTestEntryVO {
    private static final long serialVersionUID = -2665975538192261095L;
    private String questionId;
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    @Override
    public Integer getNumberOfQuestions() {
        return 1;
    }
}