package com.attestator.admin.client.ui.widgets.tabpanel;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.theme.gray.client.tabs.GrayPlainTabPanelAppearance;

public class GrayPlainTabPanelAppearanceExt extends GrayPlainTabPanelAppearance {
    
    public interface GrayTabPlainPanelResourcesExt extends GrayPlainTabPanelResources {
        
        @Source({
            "com/sencha/gxt/theme/base/client/tabs/TabPanel.css", "com/sencha/gxt/theme/gray/client/tabs/GrayTabPanel.css",
            "com/sencha/gxt/theme/base/client/tabs/PlainTabPanel.css", "com/sencha/gxt/theme/gray/client/tabs/GrayPlainTabPanel.css",
            "GrayTabPanelExt.css"})
        GrayPlainTabPanelStyle style();
    }
    
    public   GrayPlainTabPanelAppearanceExt() {
        this(GWT.<GrayTabPlainPanelResourcesExt> create(GrayTabPlainPanelResourcesExt.class), 
                GWT.<PlainTabPanelTemplates> create(PlainTabPanelTemplates.class),
            GWT.<ItemTemplate> create(ItemTemplate.class));
    }

    public GrayPlainTabPanelAppearanceExt(GrayPlainTabPanelResources resources,
            PlainTabPanelTemplates template, ItemTemplate itemTemplate) {
        super(resources, template, itemTemplate);
    }
}
