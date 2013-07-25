package com.attestator.admin.client.props;

import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface GroupVOProperties extends PropertyAccess<GroupVO> {
    ValueProvider<GroupVO, String> name();
    @Path("name")
    LabelProvider<GroupVO> nameLabel();
    ModelKeyProvider<GroupVO> id();
}
