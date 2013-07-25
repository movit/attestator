package com.attestator.admin.client.ui.event;

import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public class DeleteEvent extends GwtEvent<DeleteHandler> {

    /**
     * This event handler should implement this interface
     */
    public interface DeleteHandler extends EventHandler {
        void onDelete(DeleteEvent event);
    }
    
    /**
     * This event producer should implement this interface
     */
    public interface HasDeleteEventHandlers extends HasHandlers {
        HandlerRegistration addDeleteHandler(DeleteHandler handler);
    }    

    private static Type<DeleteHandler> TYPE;    
    
    public static Type<DeleteHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<DeleteHandler>());
    }
    
    public DeleteEvent() {
        super();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Type<DeleteHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(DeleteHandler handler) {
        handler.onDelete(this);
    }
}
