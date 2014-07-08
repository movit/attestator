package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.AddEvent.AddHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class AddEvent<T> extends GwtEvent<AddHandler<T>> {

    /**
     * This event handler should implement this interface
     */
    public interface AddHandler<T> extends EventHandler {
        void onAdd(AddEvent<T> event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasAddEventHandlers<T> extends HasHandlers {
        HandlerRegistration addAddHandler(AddHandler<T> handler);
    }    

    private static Type<AddHandler<?>> TYPE;    
    
    public static Type<AddHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<AddHandler<?>>());
    }
    
    public AddEvent() {
        super();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<AddHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(AddHandler<T> handler) {
        handler.onAdd(this);
    }
}
