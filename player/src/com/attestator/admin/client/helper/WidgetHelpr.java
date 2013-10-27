package com.attestator.admin.client.helper;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

public class WidgetHelpr {    
    public static int widgetIndex(Container parent, Widget child) {
        for (int i = 0; i < parent.getWidgetCount(); i++) {
            if (parent.getWidget(i) == child) {
                return i;
            }
        }
        return -1;
    }

    //TODO add widget context
    public static int width(List<String> strings) {
        TextMetrics tm = TextMetrics.get();
        int minListWidth = 0;
        for (String str: strings) {
            minListWidth = Math.max(minListWidth, tm.getWidth(str));
        }
        return Math.min(minListWidth, 700);
    }
    
    public static <M> void disableColumnHeaderOperations(List<ColumnConfig<M, ?>> l) {
        if (l == null) {
            return;
        }
        
        for (ColumnConfig<M, ?> columnConfig : l) {
            columnConfig.setSortable(false);
            columnConfig.setMenuDisabled(true);
        }
    }
}
