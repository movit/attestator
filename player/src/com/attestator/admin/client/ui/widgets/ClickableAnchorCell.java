package com.attestator.admin.client.ui.widgets;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.HasSelectHandlers;

public class ClickableAnchorCell<T> extends ResizeCell<T> implements
        HasSelectHandlers {
    
    public static class AnchorTemplates
    {
       public static final TextTemplates TEMPLATE = GWT.create(TextTemplates.class);

       public static interface TextTemplates extends XTemplates
       {
          @XTemplate("<a class='anchor' style='cursor: pointer;' href=''>{anchorText}</a>")
          public SafeHtml anchor(String anchorText);
       }
    }    
    
    public ClickableAnchorCell() {
        super(BrowserEvents.CLICK, BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT);
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
            if ("click".equals(eventType)) {
                XElement target = event.getEventTarget().cast();
                if (target.hasClassName("anchor")) {
                    onClick(context);
                }
            }
        }
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(AnchorTemplates.TEMPLATE.anchor(value.toString()));
        }
    }

    protected void onClick(Context context) {
        if (!isDisableEvents()
                && fireCancellableEvent(context, new BeforeSelectEvent(context))) {
            fireEvent(context, new SelectEvent(context));
        }
    }
}