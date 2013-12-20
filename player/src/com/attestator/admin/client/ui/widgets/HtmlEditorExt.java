package com.attestator.admin.client.ui.widgets;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;

public class HtmlEditorExt extends HtmlEditor {
    private HandlerRegistration setFontNameHandlerRegistration;
    private boolean textAreaFocused = false;
    
    public HtmlEditorExt() {
        super();
        registerFocusHandlers();
    }

    public HtmlEditorExt(HtmlEditorAppearance appearance) {
        super(appearance);
        registerFocusHandlers();       
    }
     
    public void setFontName(final String fontName) {
        if (textAreaFocused) {
            textArea.getFormatter().setFontName(fontName);
        }
        else {
            if (setFontNameHandlerRegistration != null) {
                setFontNameHandlerRegistration.removeHandler();
            }
            setFontNameHandlerRegistration = textArea.addFocusHandler(new FocusHandler() {                
                @Override
                public void onFocus(FocusEvent event) {
                    textArea.getFormatter().setFontName(fontName);
                    setFontNameHandlerRegistration.removeHandler();
                }
            });                    
        }
    }
    
    private void registerFocusHandlers() {
        textArea.addFocusHandler(new FocusHandler() {            
            @Override
            public void onFocus(FocusEvent event) {
                textAreaFocused = true;                
            }
        });
        textArea.addBlurHandler(new BlurHandler() {            
            @Override
            public void onBlur(BlurEvent event) {
                textAreaFocused = false;                
            }
        });
    }
}
