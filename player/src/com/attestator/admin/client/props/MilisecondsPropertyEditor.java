package com.attestator.admin.client.props;

import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.LongPropertyEditor;

public class MilisecondsPropertyEditor extends LongPropertyEditor {
    private long multiplier = 1000 * 60;

    @Override
    protected Long parseString(String string) {
        Long result = super.parseString(string);
        result = result * multiplier;
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    protected Long returnTypedValue(Number number) {
        Long result = number.longValue() * multiplier;
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    public String render(Number value) {
        value = value.longValue() / multiplier;
        return super.render(value);
    }
}
