package com.attestator.admin.client.ui.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HTMLPanel;

public class HTMLPanelExt extends HTMLPanel {

    public HTMLPanelExt() {
        super("<div id='initial'></div>");        
    }
    
//    public HTMLPanelExt(String html) {
//        super(html);        
//    }
    
    public void setHTML(String html) {
        setElement(Document.get().createDivElement());
        getElement().setInnerHTML(html);
    }
}
