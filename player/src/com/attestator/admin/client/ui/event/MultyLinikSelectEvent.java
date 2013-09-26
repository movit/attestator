package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class MultyLinikSelectEvent<T> extends GwtEvent<MultyLinikSelectHandler<T>> {
    /**
     * This event handler should implement this interface
     */
    public static interface MultyLinikSelectHandler<T> extends EventHandler {
        void onSelect(MultyLinikSelectEvent<T> event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public static interface HasMultyLinikSelectHandlers<T> extends HasHandlers {
        HandlerRegistration addMultyLinikSelectHandler(MultyLinikSelectHandler<T> handler);
    }    
    
    private static Type<MultyLinikSelectHandler<?>> TYPE;    
    
    public static Type<MultyLinikSelectHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<MultyLinikSelectHandler<?>>());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<MultyLinikSelectHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(MultyLinikSelectHandler<T> handler) {
        handler.onSelect(this);
    }
    

    private final String linkType;
    private final T value;
    private final Context context;
    
    public String getLinkType() {
        return linkType;
    }
    
    public T getValue() {
        return value;
    }

    public Context getContext() {
        return context;
    }

    public MultyLinikSelectEvent(Context context, String linkType, T value) {
        this.context = context;
        this.linkType = linkType;
        this.value = value;
    }
}