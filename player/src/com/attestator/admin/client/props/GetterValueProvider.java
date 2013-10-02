package com.attestator.admin.client.props;

import com.sencha.gxt.core.client.ValueProvider;

public abstract class GetterValueProvider<T, V> implements ValueProvider<T, V> {
    private String path;
    
    public GetterValueProvider(String path) {
        super();
        this.path = path;
    }

    @Override
    public void setValue(T object, V value) {
    }

    @Override
    public String getPath() {
        return path;
    }
}
