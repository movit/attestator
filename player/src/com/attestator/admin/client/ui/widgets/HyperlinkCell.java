package com.attestator.admin.client.ui.widgets;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Hyperlink;

public class HyperlinkCell extends AbstractCell<Hyperlink>{
    @Override
    public void render(Context context,
                    Hyperlink h, SafeHtmlBuilder sb){
        sb.append(SafeHtmlUtils.fromTrustedString(h.toString()));
    }
}