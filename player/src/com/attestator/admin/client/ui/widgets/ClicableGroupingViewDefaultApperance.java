package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.widgets.GroupingViewExt.ClicableGroupingViewAppearance;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.base.client.grid.GroupingViewDefaultAppearance;

public class ClicableGroupingViewDefaultApperance extends
        GroupingViewDefaultAppearance implements ClicableGroupingViewAppearance {    
    
    public static interface LinkTemplate extends XTemplates{
       @XTemplate("<a id='{type}-{id}' class='{anchorClassName}' href='#'>{text}</a>")
       public SafeHtml link(String type, String id, String anchorClassName, String text);
    }
    
    public static interface GroupingViewExtResources extends ClientBundle {
        @Source("GroupingViewExt.css")
        GroupingViewExt.GroupingViewExtCss groupingViewExtCss();
    }
    
    public static final LinkTemplate LINK_TEMPLATE = GWT.create(LinkTemplate.class);
    public static final GroupingViewExtResources RESOURCES = GWT.create(GroupingViewExtResources.class);

    @Override
    public XElement findClicableElement(XElement element) {
        return element.findParent("." + RESOURCES.groupingViewExtCss().groupingViewExtLink(), 10);
    }
    
    static {
        RESOURCES.groupingViewExtCss().ensureInjected();
    }

    @Override
    public SafeHtml createClicableElement(String type, String id, String text) {
        return LINK_TEMPLATE.link(type, id, RESOURCES.groupingViewExtCss().groupingViewExtLink(), text);
    }        
}
