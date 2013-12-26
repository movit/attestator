package com.attestator.common.client.helper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.Mask;
import com.sencha.gxt.core.client.dom.XElement;

public class WindowHelper {

    private static class HiddenIFrame extends Frame {
        public HiddenIFrame(String url) {
            super();
            setSize("0px", "0px");
            setVisible(false);
            sinkEvents(Event.ONLOAD);
            RootPanel.get().add(this);
            setUrl(url);
        }

        public void onBrowserEvent(Event event) {
            if (DOM.eventGetType(event) == Event.ONLOAD) {
                unsinkEvents(Event.ONLOAD);
                DOM.eventCancelBubble(event, true);
                RootPanel.get().remove(this);
            } else {
                super.onBrowserEvent(event);
            }
        }
    }
    
    public static void downloadFile(String url) {
        new HiddenIFrame(url);
    }

    public static void setBrowserWindowTitle(String newTitle) {
        if (Document.get() != null) {
            Document.get().setTitle(newTitle);
        }
    }

    public static void mask(String text) {
        Mask.mask(XElement.as(RootPanel.get().getElement()), text);
    }

    public static void unmask() {
        Mask.unmask(XElement.as(RootPanel.get().getElement()));
    }
}
