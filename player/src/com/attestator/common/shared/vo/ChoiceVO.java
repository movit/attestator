package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Transient;

import com.attestator.common.shared.helper.NullHelper;

public class ChoiceVO extends BaseVO {
    private static final long serialVersionUID = 8324774439485268133L;
    private String  text;
    private Boolean right;
    @Transient
    private Integer order = 0;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public boolean isThisRight() {
        return NullHelper.nullSafeTrue(right);
    }
    public Boolean getRight() {
        return right;
    }
    public void setRight(Boolean right) {
        this.right = right;
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
    @Override
    public String toString() {
        return "ChoiceVO [text=" + text + ", right=" + right + ", getId()="
                + getId() + "]";
    }
}