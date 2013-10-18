package com.attestator.admin.client.props;

import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface MetatestEntryVOPropertyAccess extends PropertyAccess<MetaTestEntryVO> {
    ValueProvider<MetaTestEntryVO, Integer> numberOfQuestions();
    ModelKeyProvider<MetaTestEntryVO> id();
}
