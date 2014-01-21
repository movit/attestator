package com.attestator.common.shared.vo;

import com.attestator.common.server.db.annotation.ReferenceCount;
import com.attestator.common.shared.helper.NullHelper;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;

@Entity("group")
public class GroupVO extends ShareableVO {
    private static final long serialVersionUID = 8072477436639798845L;
    
    public static String DEFAULT_GROUP_INITIAL_NAME = "Общие вопросы";
    
    private String  name;
    
    @Transient
    @ReferenceCount(toClass=QuestionVO.class, toField="groupId")
    private Long questionsCount;

    public GroupVO() {
        super();
    }

    public GroupVO(String name) {
        super();
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getQuestionsCount() {
        return questionsCount;
    }
    
    public long getQuestionsCountOrZero() {
        return NullHelper.nullSafeLongOrZerro(questionsCount);
    }
    
    public void setQuestionsCount(Long questionsCount) {
        this.questionsCount = questionsCount;
    }

    @Override
    public String toString() {
        return "GroupVO [name=" + name + ", questionsCount=" + questionsCount
                + ", getTenantId()=" + getTenantId() + ", getId()=" + getId()
                + "]";
    }
}
