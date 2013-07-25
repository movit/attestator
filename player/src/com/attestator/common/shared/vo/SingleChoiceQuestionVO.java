package com.attestator.common.shared.vo;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;

@Entity("question")
public class SingleChoiceQuestionVO extends QuestionVO {
    private static final long serialVersionUID = -3128704433193435364L;
    
    private Boolean        randomChoiceOrder = false;
    @Size(min=1, message="Нужен хотя бы один вариант ответа")
    private List<ChoiceVO> choices  = new ArrayList<ChoiceVO>();
   
    public List<ChoiceVO> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceVO> choices) {
        this.choices = choices;
    }

    public boolean isThisRandomChoiceOrder() {
        return NullHelper.nullSafeTrue(randomChoiceOrder);
    }

    public Boolean getRandomChoiceOrder() {
        return randomChoiceOrder;
    }

    public void setRandomChoiceOrder(Boolean randomChoiceOrder) {
        this.randomChoiceOrder = randomChoiceOrder;
    }

    public ChoiceVO getChoice(String choiceId) {
        for (ChoiceVO choice: choices) {
            if (NullHelper.nullSafeEquals(choice.getId(), choiceId)) {
                return choice;
            }
        }
        return null;
    }
    
    @Override
    public boolean isRightAnswer(AnswerVO answer) {
        if (!(answer instanceof SCQAnswerVO)) {
            return false;
        }
        
        if (!NullHelper.nullSafeEquals(answer.getQuestionId(), getId())) {
            return false;
        }
        
        SCQAnswerVO scqAnswer = (SCQAnswerVO) answer;
        ChoiceVO choice = getChoice(scqAnswer.getChoiceId());
        if (choice == null) {
            return false;
        }
        
        return choice.isThisRight();
    }

    private ChoiceVO getRightChoice() {
        for (ChoiceVO choice: choices) {
            if (choice.isThisRight()) {
                return choice;
            }
        }
        return null;
    }
    
    @Override
    public AnswerVO getRightAnswer() {
        ChoiceVO rightChoice = getRightChoice();
        if (rightChoice != null) {
            SCQAnswerVO answer = new SCQAnswerVO();
            answer.setQuestionId(getId());
            answer.setChoiceId(rightChoice.getId());
            return answer;
        }
        return null;
    }

    @Override
    public String toString(AnswerVO answer) {
        if (answer instanceof SCQAnswerVO) {
            ChoiceVO choice = getChoice(((SCQAnswerVO) answer).getChoiceId());
            if (choice != null) {
                return choice.getText();
            }
        }
        return null;
    }

    @Override
    public String getTaskDescription() {
        return "Выберите один из вариантов";
    }

    @Override
    public String toString() {
        return "SingleChoiceQuestionVO [randomChoiceOrder=" + randomChoiceOrder
                + ", choices=" + choices + ", getRandomChoiceOrder()="
                + getRandomChoiceOrder() + ", getText()=" + getText()
                + ", getModified()=" + getModified() + ", getGroupId()="
                + getGroupId() + ", getMaxQuestionAnswerTime()="
                + getMaxQuestionAnswerTime() + ", getScore()=" + getScore()
                + ", getPenalty()=" + getPenalty() + ", getGroupName()="
                + getGroupName() + ", getTenantId()=" + getTenantId()
                + ", getId()=" + getId() + "]";
    }

}    
