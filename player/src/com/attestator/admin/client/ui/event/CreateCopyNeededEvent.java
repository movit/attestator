package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.CreateCopyNeededEvent.CreateCopyNeededHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class CreateCopyNeededEvent<T> extends GwtEvent<CreateCopyNeededHandler<T>> {

    /**
     * This event handler should implement this interface
     */
    public interface CreateCopyNeededHandler<T> extends EventHandler {
        void onCreateCopyNeeded(CreateCopyNeededEvent<T> event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasCreateCopyNeededEventHandlers<T> extends HasHandlers {
        HandlerRegistration addCreateCopyNeededHandler(CreateCopyNeededHandler<T> handler);
    }    

    private static Type<CreateCopyNeededHandler<?>> TYPE;    
    
    final private T value;
    
    public static Type<CreateCopyNeededHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<CreateCopyNeededHandler<?>>());
    }
    
    
    public CreateCopyNeededEvent(T value) {
        super();
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<CreateCopyNeededHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(CreateCopyNeededHandler<T> handler) {
        handler.onCreateCopyNeeded(this);
    }
}
