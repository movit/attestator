package com.attestator.common.shared.vo;

import com.attestator.common.server.db.annotation.SetOnSave;
import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;

@Entity("question")
public abstract class QuestionVO extends TenantableVO {
    private static final long serialVersionUID = -2683964459602620222L;
    private String            text;
    private String            groupId;
    @SetOnSave(refField = "groupId", targetClass = GroupVO.class, targetValueField = "name")
    private String            groupName;
    private Long              maxQuestionAnswerTime;
    private Double            score            = (double) 1;
    private Double            penalty          = (double) 0;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroup(GroupVO group) {
        if (group != null) {
            groupId = group.getId();
            groupName = group.getName();
        } else {
            groupId = null;
            groupName = null;
        }
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getMaxQuestionAnswerTime() {
        return maxQuestionAnswerTime;
    }

    public void setMaxQuestionAnswerTime(Long maxQuestionAnswerTime) {
        this.maxQuestionAnswerTime = maxQuestionAnswerTime;
    }

    public double getScoreOrZero() {
        return NullHelper.nullSafeDoubleOrZerro(score);
    }
    
    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getPenalty() {
        return penalty;
    }
    
    public double getPenaltyOrZero() {
        return NullHelper.nullSafeDoubleOrZerro(penalty);
    }

    public void setPenalty(Double penalty) {
        this.penalty = penalty;
    }

    public double getAnswerScore(AnswerVO answer) {
        Double questionScore = isRightAnswer(answer) ? getScoreOrZero()
                : -getPenaltyOrZero();
        return NullHelper.nullSafeDoubleOrZerro(questionScore);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
 
    public abstract boolean isRightAnswer(AnswerVO answer);

    public abstract AnswerVO getRightAnswer();

    public abstract String toString(AnswerVO answer);

    public abstract String getTaskDescription();
}
