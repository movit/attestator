package com.attestator.admin.client.ui.widgets;

import com.sencha.gxt.widget.core.client.box.MessageBox;

public class InfoMessageBox extends MessageBox {    
    public InfoMessageBox(String headingHtml, String messageHtml) {
        super(headingHtml, messageHtml);
        setIcon(ICONS.info());
    }
}
