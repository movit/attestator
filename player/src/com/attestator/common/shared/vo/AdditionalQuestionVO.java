package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Transient;

import com.attestator.common.shared.helper.NullHelper;

public class AdditionalQuestionVO extends BaseVO {    
    public static enum AnswerTypeEnum implements Displayable {
        text("Текстовое поле"),
        key("Секретный ключ");
        
        private String displayValue;
        
        private AnswerTypeEnum(String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }
    
    private static final long serialVersionUID = 6076155934213944850L;
    
    private String         text;
    private String         checkValue;
    private Boolean        required;
    @Transient
    private Integer order = 0;
    private AnswerTypeEnum answerType = AnswerTypeEnum.text;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public Boolean getRequired() {
        return required;
    }
    public boolean isThisRequired() {
        return NullHelper.nullSafeTrue(required);
    }
    public void setRequired(Boolean required) {
        this.required = required;
    }
    public String getCheckValue() {
        return checkValue;
    }
    public void setCheckValue(String regex) {
        this.checkValue = regex;
    }
    public AnswerTypeEnum getAnswerType() {
        return answerType;
    }
    public void setAnswerType(AnswerTypeEnum answerType) {
        this.answerType = answerType;
    }
    public Integer getOrder() {
        return order;
    }
    public int getOrderOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(order);
    }
    public void setOrder(Integer order) {
        this.order = order;
    }
    
    public AdditionalQuestionVO() {
    }
    
    public AdditionalQuestionVO(AdditionalQuestionVO src) {
        this.text = src.text;
        this.checkValue = src.checkValue;
        this.required = src.required;
        this.order = src.order;
        this.answerType = src.answerType;
    }    
}