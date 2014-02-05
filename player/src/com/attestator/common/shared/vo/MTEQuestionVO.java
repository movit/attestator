package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Transient;

import com.attestator.common.server.db.annotation.Reference;

public class MTEQuestionVO extends MetaTestEntryVO {
    private static final long serialVersionUID = -2665975538192261095L;
    private String questionId;
    
    @Transient
    @Reference(fromField = "questionId")
    private QuestionVO  question;
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public QuestionVO getQuestion() {
        return question;
    }

    public void setQuestion(QuestionVO question) {
        this.question = question;
    }

    @Override
    public Integer getNumberOfQuestions() {
        return 1;
    }
}