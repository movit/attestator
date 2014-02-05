package com.attestator.admin.client.ui.widgets.tabpanel;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.theme.gray.client.tabs.GrayTabPanelAppearance;

public class GrayTabPanelAppearanceExt extends GrayTabPanelAppearance {
    
    public interface GrayTabPanelResourcesExt extends GrayTabPanelResources {
        @Source({"com/sencha/gxt/theme/base/client/tabs/TabPanel.css", "com/sencha/gxt/theme/gray/client/tabs/GrayTabPanel.css", "GrayTabPanelExt.css"})
        GrayTabPanelStyle style();
    }
    
    public   GrayTabPanelAppearanceExt() {
        this(GWT.<GrayTabPanelResourcesExt> create(GrayTabPanelResourcesExt.class), GWT.<Template> create(Template.class),
            GWT.<ItemTemplate> create(ItemTemplate.class));
    }

    public GrayTabPanelAppearanceExt(GrayTabPanelResources resources,
            Template template, ItemTemplate itemTemplate) {
        super(resources, template, itemTemplate);
    }
}
