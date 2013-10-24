package com.attestator.admin.client.props;

import com.sencha.gxt.data.shared.Converter;

public class BoundedConverter<N extends Number> implements Converter<N, N> {
    private N prevValue;
    private N minValue;
    private N maxValue;
    
    public void setPrevValue(N prevValue) {
        this.prevValue = prevValue;
    }

    public void setMinValue(N minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(N maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public N convertFieldValue(N object) {
        if (object == null) {
            return prevValue;
        }
        if (minValue != null) {
            if (object.doubleValue() < minValue.doubleValue()) {
                return minValue;
            }
        }
        if (maxValue != null) {
            if (object.doubleValue() > maxValue.doubleValue()) {
                return maxValue;
            }
        }
        return object;
    }

    @Override
    public N convertModelValue(N object) {
        return object;
    }
}
