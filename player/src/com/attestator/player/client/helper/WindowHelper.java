package com.attestator.player.client.helper;

import com.google.gwt.dom.client.Document;

public class WindowHelper {    
    public static void setBrowserWindowTitle(String newTitle) {
        if (Document.get() != null) {
            Document.get().setTitle (newTitle);
        }
    }
}
