package com.attestator.common.shared.vo;

import java.util.Date;

public abstract class AnswerVO extends BaseVO {
    private static final long serialVersionUID = -4738160660130886949L;
    private String     questionId;
    private Date       time;
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}