package com.attestator.admin.client.ui.widgets;

import com.attestator.common.shared.helper.NullHelper;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class BooleanCell extends AbstractCell<Boolean> {

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
            Boolean value, SafeHtmlBuilder sb) {
        if (NullHelper.nullSafeTrue(value)) {
            sb.appendEscaped("да");
        }
        else {
            sb.appendEscaped("нет");
        }
    }

}
