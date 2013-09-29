package com.attestator.admin.client.props;

import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;

public class DoubleGreaterZerroPropertyEditor extends DoublePropertyEditor {

    @Override
    protected Double parseString(String string) {
        Double result = super.parseString(string);
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    protected Double returnTypedValue(Number number) {
        Double result = number.doubleValue();
        if (result <= 0) {
            return null;
        } else {
            return result;
        }
    }
}
