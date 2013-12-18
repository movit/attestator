package com.attestator.admin.client.ui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;

public class HtmlEditorExt extends HtmlEditor {
    private HandlerRegistration setFontNameOnAttachRegistration;
    public HtmlEditorExt() {
        super();
    }

    public HtmlEditorExt(HtmlEditorAppearance appearance) {
        super(appearance);
    }
     
    public void setFontName(final String fontName) {
        if (isAttached()) {
            textArea.getFormatter().setFontName(fontName);
        }
        else {
            if (setFontNameOnAttachRegistration != null) {
                setFontNameOnAttachRegistration.removeHandler();
            }
            setFontNameOnAttachRegistration = addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (event.isAttached()) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            @Override
                            public void execute() {
                                textArea.getFormatter().setFontName(fontName);                                
                            }
                        });
                    }
                }
            });
        }
    }
}
