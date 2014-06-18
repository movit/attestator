package com.attestator.admin.client.helper;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;

public class Validator {
    private boolean valid = true;
    private StringBuilder sb = new StringBuilder();
    private Component focusWidget;
    
    public void addError(String message, Component widget) {
        valid = false;
        sb.append(message + "<br/>");
        if (focusWidget == null) {
            focusWidget = widget;
        }
    }
    
    public String getMessage() {
        return sb.toString();
    }
    
    public Component getFocusWidget() {
        return focusWidget;
    }

    public boolean isValid() {
        return valid;
    }
    
    public void showAlert() {
        if (valid) {
            return;
        }
        
        AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
        
        if (focusWidget != null) {
            alert.addHideHandler(new HideHandler() {
                @Override
                public void onHide(HideEvent event) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                        @Override
                        public void execute() {
                            focusWidget.focus();
                        }
                    });
                }
            });
        }
        
        alert.show();
    }
}
