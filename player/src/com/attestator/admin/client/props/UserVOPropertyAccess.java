package com.attestator.admin.client.props;

import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface UserVOPropertyAccess extends PropertyAccess<UserVO>{
    ModelKeyProvider<UserVO> id();
    ValueProvider<UserVO, String> username();
    @Path("username")
    LabelProvider<UserVO> usernameLabel();
}
