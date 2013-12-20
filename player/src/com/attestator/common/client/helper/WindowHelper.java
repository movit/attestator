package com.attestator.common.client.helper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.Mask;
import com.sencha.gxt.core.client.dom.XElement;

public class WindowHelper {    
    public static void setBrowserWindowTitle(String newTitle) {
        if (Document.get() != null) {
            Document.get().setTitle (newTitle);
        }
    }
    
    public static void mask(String text) {
        Mask.mask(XElement.as(RootPanel.get().getElement()), text); 
    }
    
    public static void unmask() {
        Mask.unmask(XElement.as(RootPanel.get().getElement()));
    }
}
