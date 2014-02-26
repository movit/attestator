package com.attestator.admin.client.ui.widgets;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
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
    
    private void setToolbarChildsEnabled(boolean enabled) {
        for (int i = 0; i < toolBar.getWidgetCount(); i++) {
            Widget child = toolBar.getWidget(i);
            if (child instanceof Component) {
                ((Component) child).setEnabled(enabled);
            }
        }
    }
    
    @Override
    protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();
        setToolbarChildsEnabled(toolBar.isEnabled());
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
        if (textArea != null) {
            textArea.setEnabled(false);
        }
        if (sourceTextArea != null) {
            sourceTextArea.setEnabled(false);
        }
        toolBar.setEnabled(false);
        setToolbarChildsEnabled(toolBar.isEnabled());
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
        if (textArea != null) {
            textArea.setEnabled(true);
        }
        if (sourceTextArea != null) {
            sourceTextArea.setEnabled(true);
        }
        toolBar.setEnabled(true);
        setToolbarChildsEnabled(toolBar.isEnabled());
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
