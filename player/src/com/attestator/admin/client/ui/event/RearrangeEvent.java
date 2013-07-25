package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class RearrangeEvent extends GwtEvent<RearrangeHandler> {

    /**
     * This event handler should implement this interface
     */
    public interface RearrangeHandler extends EventHandler {
        void onRearrange(RearrangeEvent event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasRearrangeEventHandlers extends HasHandlers {
        HandlerRegistration addRearrangeHandler(RearrangeHandler handler);
    }    

    private static Type<RearrangeHandler> TYPE;    
    
    private final boolean up;
    
    public boolean isUp() {
        return up;
    }

    public static Type<RearrangeHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<RearrangeHandler>());
    }
    
    public RearrangeEvent(boolean up) {
        this.up = up;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<RearrangeHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(RearrangeHandler handler) {
        handler.onRearrange(this);
    }
}
