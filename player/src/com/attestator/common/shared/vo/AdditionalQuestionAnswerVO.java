package com.attestator.common.shared.vo;


public class AdditionalQuestionAnswerVO extends BaseVO {
    private static final long serialVersionUID = -570096777625498816L;
    private String questionId;
    private String question;
    private String answer;
    private Boolean valueChecked;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Boolean getValueChecked() {
        return valueChecked;
    }

    public void setValueChecked(Boolean valueChecked) {
        this.valueChecked = valueChecked;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
}