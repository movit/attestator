package com.attestator.player.client.cache.co;

import java.util.ArrayList;
import java.util.List;

import com.attestator.common.shared.helper.NullHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class TenantCacheVersionsCO implements JsonSerializable {
    private static final int MAX_SIZE = 3;
    
    private List<TenantCacheVersionCO> items = new ArrayList<TenantCacheVersionCO>();

    public List<TenantCacheVersionCO> getItems() {
        return items;
    }

    public void setItems(List<TenantCacheVersionCO> items) {
        this.items = items;
    }

    public void setCurrentTenant(final String tenantId) {
        // Put version to push on the top
        int index = Iterables.indexOf(items, new Predicate<TenantCacheVersionCO>() {
            @Override
            public boolean apply(TenantCacheVersionCO version) {
                return NullHelper.nullSafeEquals(version.getTenantId(), tenantId);
            }
        });
        
        TenantCacheVersionCO version = null;
        if (index >= 0) {
            version = items.remove(index);
        }
        else {
            version = new TenantCacheVersionCO(tenantId, null);
        }
        
        items.add(0, version);        
        
        // Shrink items to MAX_SIZE
        if (items.size() > MAX_SIZE) {
            items.subList(MAX_SIZE, items.size()).clear();
        }
    }
    
    public void push(final TenantCacheVersionCO versionToPush) {
        
        // Put version to push on the top
        int index = Iterables.indexOf(items, new Predicate<TenantCacheVersionCO>() {
            @Override
            public boolean apply(TenantCacheVersionCO version) {
                return NullHelper.nullSafeEquals(version.getTenantId(), versionToPush.getTenantId());
            }
        });
        
        if (index >= 0) {
            items.remove(index);
        }
        
        items.add(0, versionToPush);
        
        // Shrink items to MAX_SIZE
        if (items.size() > MAX_SIZE) {
            items.subList(MAX_SIZE, items.size()).clear();
        }
    }
    
    public TenantCacheVersionCO getCurrentTenantVersion() {
        if (items.size() > 0) {
            return items.get(0);
        }
        return null;
    } 
}
