package com.attestator.admin.client.props;

import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;

public class IntegerGreaterZerroPropertyEditor extends IntegerPropertyEditor {

    @Override
    protected Integer parseString(String string) {
        Integer result = super.parseString(string);
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    protected Integer returnTypedValue(Number number) {
        Integer result = number.intValue();
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }
}
