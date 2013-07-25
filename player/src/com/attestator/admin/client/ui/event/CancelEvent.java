package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.CancelEvent.CancelHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class CancelEvent extends GwtEvent<CancelHandler> {

    /**
     * This event handler should implement this interface
     */
    public interface CancelHandler extends EventHandler {
        void onCancel(CancelEvent event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasCancelEventHandlers extends HasHandlers {
        HandlerRegistration addCancelHandler(CancelHandler handler);
    }    

    private static Type<CancelHandler> TYPE;    
    
    public static Type<CancelHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<CancelHandler>());
    }
    
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<CancelHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(CancelHandler handler) {
        handler.onCancel(this);
    }
}
