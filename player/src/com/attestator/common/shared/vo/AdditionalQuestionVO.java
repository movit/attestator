package com.attestator.common.shared.vo;

import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Embedded;

@Embedded
public class AdditionalQuestionVO extends BaseVO {
    private static final long serialVersionUID = 6076155934213944850L;
    
    private String  text;
    private String  checkValue;
    private Boolean required;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public boolean isRequired() {
        return NullHelper.nullSafeTrue(required);
    }
    public Boolean getRequired() {
        return required;
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
}