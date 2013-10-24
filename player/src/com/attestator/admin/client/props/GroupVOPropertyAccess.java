package com.attestator.admin.client.props;

import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface GroupVOPropertyAccess extends PropertyAccess<GroupVO> {
    ValueProvider<GroupVO, Long> questionsCount();
    ValueProvider<GroupVO, String> name();
    @Path("name")
    LabelProvider<GroupVO> nameLabel();
    @Path("id")
    ValueProvider<GroupVO, String> goupId();
    ModelKeyProvider<GroupVO> id();
}
