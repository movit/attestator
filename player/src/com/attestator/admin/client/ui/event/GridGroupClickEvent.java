package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.GridGroupClickEvent.GridGroupClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class GridGroupClickEvent<T> extends GwtEvent<GridGroupClickHandler<T>> {

    /**
     * This event handler should implement this interface
     */
    public interface GridGroupClickHandler<T> extends EventHandler {
        void onClick(GridGroupClickEvent<T> event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasGridGroupClickHandlers<T> {
        HandlerRegistration addGridGroupClickHandler(GridGroupClickHandler<T> handler);
    }    

    private static Type<GridGroupClickHandler<?>> TYPE;    
    
    final private T value;
    
    public static Type<GridGroupClickHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<GridGroupClickHandler<?>>());
    }
    
    
    public GridGroupClickEvent(T value) {
        super();
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<GridGroupClickHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(GridGroupClickHandler<T> handler) {
        handler.onClick(this);
    }
}
