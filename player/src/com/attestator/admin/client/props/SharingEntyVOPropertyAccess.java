package com.attestator.admin.client.props;

import java.util.Date;

import com.attestator.common.shared.vo.SharingEntryVO;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface SharingEntyVOPropertyAccess extends PropertyAccess<SharingEntryVO> {    
    ValueProvider<SharingEntryVO, String> username();
    ValueProvider<SharingEntryVO, Date> start();
    ValueProvider<SharingEntryVO, Date> end();
    ModelKeyProvider<SharingEntryVO> id();
}
