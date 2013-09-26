package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.HasMultyLinikSelectHandlers;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
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

public abstract class MultylinkCell<T> extends ResizeCell<T> implements
        HasMultyLinikSelectHandlers<T> {
    
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
    public HandlerRegistration addMultyLinikSelectHandler(MultyLinikSelectHandler<T> handler) {
        return addHandler(handler, MultyLinikSelectEvent.getType());        
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
                    fireEvent(context, new MultyLinikSelectEvent<T>(context, link.getClassName(), value));
                    return;
                }
            }
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
    
    public SafeHtml createClickableElement(String type, String text) {
        return LINK_TEMPLATE.link(RESOURCES.multyLinkCellCss().multyLink(), type, text);
    }        

    static {
        RESOURCES.multyLinkCellCss().ensureInjected();
    }

}