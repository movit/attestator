package com.attestator.admin.client.ui.widgets;

import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.Loader;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class PageringGridFilters<T> extends GridFilters<T> {
    public PageringGridFilters() {
        super();
    }

    protected boolean local = false;
    protected PagingToolBar pager;

    public PageringGridFilters(Loader<FilterPagingLoadConfig, ?> loader) {
        super(loader);
    }

    public PageringGridFilters(Loader<FilterPagingLoadConfig, ?> loader,
            PagingToolBar pager) {
        super(loader);
        this.pager = pager;
    }

    @Override
    public boolean isAutoReload() {        
        return super.isAutoReload();
    }
    
    @Override
    public void setAutoReload(boolean autoLoad) {
        super.setAutoReload(autoLoad && pager == null);
    }
    
    @Override
    public boolean isLocal() {
        return super.isLocal();
    }

    @Override
    public void setLocal(boolean local) {
        super.setLocal(local && pager == null);        
    }

    protected void reload() {
        if (pager == null) {
            super.reload();
        } else {
            pager.first();
        }
    }
}
