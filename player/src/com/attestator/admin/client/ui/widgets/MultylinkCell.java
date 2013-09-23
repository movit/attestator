package com.attestator.admin.client.ui.widgets;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.HasSelectHandlers;

public abstract class MultylinkCell<T> extends ResizeCell<T> implements
        HasSelectHandlers {
    
    public static class MultyLinikSelectEvent<T> extends SelectEvent {
        private String linkType;
        private T value;
        
        public String getLinkType() {
            return linkType;
        }

        public void setLinkType(String linkType) {
            this.linkType = linkType;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public MultyLinikSelectEvent(Context context, String linkType, T value) {
            super(context);
            this.linkType = linkType;
            this.value = value;
        }
    }
    
    public static interface LinkTemplate extends XTemplates {
        @XTemplate("<span class='{anchorClassName}'><a class='{typeClassName}' href='#'>{text}</a></span>")
        public SafeHtml link(String anchorClassName, String typeClassName, String text);
    }
    
    public static interface MultyLinkCellCss extends CssResource {
        String multyLink();
    }
    
    public static interface MultyLinkCellResources extends ClientBundle {
        @Source("MultyLinkCell.css")
        MultyLinkCellCss multyLinkCellCss();
    }
     
    public static final LinkTemplate LINK_TEMPLATE = GWT.create(LinkTemplate.class);
    public static final MultyLinkCellResources RESOURCES = GWT.create(MultyLinkCellResources.class);
    
    public MultylinkCell() {
        super(BrowserEvents.CLICK);
    }

    @Override
    public HandlerRegistration addSelectHandler(
            SelectEvent.SelectHandler handler) {
        return addHandler(handler, SelectEvent.getType());
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value,
            NativeEvent event, ValueUpdater<T> valueUpdater) {
        if (!isDisableEvents()) {
            String eventType = event.getType();
            if (BrowserEvents.CLICK.equals(eventType)) {
                XElement target = event.getEventTarget().cast();
                XElement linkOuter = target.findParent("." + RESOURCES.multyLinkCellCss().multyLink(), 10);  
                
                if (linkOuter != null) {
                    XElement link = linkOuter.getChild(0).cast();
                    onClick(context, value, link.getClassName());
                    return;
                }
            }
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
    
    public SafeHtml createClicableElement(String type, String text) {
        return LINK_TEMPLATE.link(RESOURCES.multyLinkCellCss().multyLink(), type, text);
    }        

    protected void onClick(Context context, T value, String linkType) {
        if (!isDisableEvents()
                && fireCancellableEvent(context, new BeforeSelectEvent(context))) {
            fireEvent(context, new MultyLinikSelectEvent<T>(context, linkType, value));
        }
    }
    
    static {
        RESOURCES.multyLinkCellCss().ensureInjected();
    }

}