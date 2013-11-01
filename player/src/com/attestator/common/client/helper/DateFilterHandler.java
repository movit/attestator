package com.attestator.common.client.helper;

import java.util.Date;

import com.attestator.common.shared.SharedConstants;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.data.shared.loader.FilterHandler;

public class DateFilterHandler extends FilterHandler<Date>{
    @Override
    public Date convertToObject(String str) {
        Date result = DateTimeFormat.getFormat(SharedConstants.DATE_TRANSFER_FORMAT).parse(str);
        result = (new DateWrapper(result)).clearTime().asDate();
        return result;
    }

    @Override
    public String convertToString(Date date) {
        date = (new DateWrapper(date)).clearTime().asDate();
        return DateTimeFormat.getFormat(SharedConstants.DATE_TRANSFER_FORMAT).format(date);
    }
}
