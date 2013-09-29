package com.attestator.common.client.props;

import java.util.Arrays;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

public class EnumStore<E extends Enum<E>> extends ListStore<E> {    
    public EnumStore(Class<E> clazz) {
        super(new ModelKeyProvider<E>() {
            @Override
            public String getKey(E item) {
                return item.name();
            }
        });
        addAll(Arrays.asList(clazz.getEnumConstants()));
    }
}
