package com.attestator.common.client.props;

import com.attestator.common.shared.vo.Displayable;
import com.sencha.gxt.data.shared.LabelProvider;

public class EnumLaybelProvider<E extends Enum<E>> implements LabelProvider<E> {
    @Override
    public String getLabel(E item) {
        if (item instanceof Displayable) {
            return ((Displayable) item).getDisplayValue();
        }
        else {
            return ((Enum<E>)item).name();
        }
    }
}
