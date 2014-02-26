package com.attestator.admin.client.props;

import com.attestator.common.shared.vo.QuestionVO;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface QuestionVOPropertyAccess extends PropertyAccess<QuestionVO> {
    ValueProvider<QuestionVO, String> text();
    ValueProvider<QuestionVO, String> ownerUsername();
    ValueProvider<QuestionVO, String> groupName();

    ModelKeyProvider<QuestionVO> id();
}
