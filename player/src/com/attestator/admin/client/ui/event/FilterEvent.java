package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.FilterEvent.FilterHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class FilterEvent<T> extends GwtEvent<FilterHandler<T>> {

    public interface FilterHandler<T> extends EventHandler {
        void onFilter(FilterEvent<T> event);
    }
    
    public interface HasFilterEventHandlers<T> extends HasHandlers {
        HandlerRegistration addFilterChangeHandler(FilterHandler<T> handler);
    }

    private static Type<FilterHandler<?>> TYPE;
    
    private final String value;
    
    /**
     * Gets the type associated with this event.
     * 
     * @return returns the handler type
     */
    public static Type<FilterHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<FilterHandler<?>>());
    }
    
    public FilterEvent(String value) {
        super();
        this.value = value;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<FilterHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(FilterHandler<T> handler) {
        handler.onFilter(this);
    }

    public String getValue() {
        return value;
    }

}
