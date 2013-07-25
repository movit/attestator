package com.attestator.common.shared.vo;

public class SCQAnswerVO extends AnswerVO {
    private static final long serialVersionUID = -7536429484739482602L;
    private String choiceId;

    public String getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(String choiceId) {
        this.choiceId = choiceId;
    }

    @Override
    public String toString() {
        return "SCQAnswerVO [choiceId=" + choiceId + ", getQuestionId()="
                + getQuestionId() + ", getTime()=" + getTime() + "]";
    }    
    
    
}