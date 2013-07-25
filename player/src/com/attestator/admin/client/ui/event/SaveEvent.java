package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class SaveEvent<T> extends GwtEvent<SaveHandler<T>> {

    /**
     * This event handler should implement this interface
     */
    public interface SaveHandler<T> extends EventHandler {
        void onSave(SaveEvent<T> event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasSaveEventHandlers<T> extends HasHandlers {
        HandlerRegistration addSaveHandler(SaveHandler<T> handler);
    }    

    private static Type<SaveHandler<?>> TYPE;    
    
    final private T value;
    
    public static Type<SaveHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<SaveHandler<?>>());
    }
    
    
    public SaveEvent(T value) {
        super();
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<SaveHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(SaveHandler<T> handler) {
        handler.onSave(this);
    }
}
