package com.attestator.common.shared.vo;

import com.attestator.common.shared.helper.NullHelper;

public abstract class MetaTestEntryVO extends BaseVO {
    private static final long serialVersionUID = 8686712792141280960L;
    public abstract Integer getNumberOfQuestions();
    public int getNumberOfQuestionsOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(getNumberOfQuestions());
    }
    public void setNumberOfQuestions(Integer numberOfQuestions) {}; //Need this method for JSON Serializer
}