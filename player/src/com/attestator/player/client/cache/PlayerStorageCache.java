package com.attestator.player.client.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.attestator.common.client.helper.SerializationHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.cache.co.TenantCacheVersionsCO;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.storage.client.Storage;

public class PlayerStorageCache {
    private static final String KEY_PAIR_SEPARATOR = ", ";

    private Storage localStorage = Storage.getLocalStorageIfSupported();
    
    public static PlayerStorageCache getPlayerStorageIfSupported() {
        if (Storage.isLocalStorageSupported()) {
            return new PlayerStorageCache();
        }
        else {
            return null;
        }
    }

    public <T> void setItem(String key, T value) {
        String serializedValue = SerializationHelper.serialize(value);
        localStorage.setItem(key, serializedValue);
    }

    public <T> T getItem(Class<T> clazz, String key) {
        String serializedValue = localStorage.getItem(key);
        if (serializedValue == null) {
            return null;
        }
        T result = SerializationHelper.deserialize(clazz, serializedValue);
        return result;
    }

    public <T> T getItem(Class<T> clazz, String key, T defaultValue) {
        T result = getItem(clazz, key);
        return result != null ? result : defaultValue;
    }
    
    public void leaveOnlyThisClientItemsByRegex(String regex) {
        RegExp p = RegExp.compile(regex);
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!storageKey.contains("clientId")) {
                continue;
            }
            if (storageKey.contains("tenantVersions")) {
                continue;
            }            
            if (!p.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }        
    }
    
    public void leaveOnlyThisClientItems(String ... keyEntries) {
        String regex = StringHelper.toPairsString(".*", keyEntries);
        leaveOnlyThisClientItemsByRegex(regex);
    }
    
    public void leaveOnlyThisClientTenantItems(String clientId) {
        StringBuilder sb = new StringBuilder();
        sb.append("clientId=" + clientId);
        
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key("clientId", clientId, "type", "tenantVersions")); 
        if (versions != null) {
            sb.append(".*");
            sb.append("(");
            int i = 0;
            for (TenantCacheVersionCO version : versions.getItems()) {
                if (i > 0) {
                    sb.append("|");
                }
                sb.append("(tenantId=" + version.getTenantId() + ")");
                i++;
            }
            sb.append(")");
        }
        
        leaveOnlyThisClientItemsByRegex(sb.toString());
    }

    public void removeThisClientItemsByRegex(String regex) {        
        RegExp p = RegExp.compile(regex);
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!storageKey.contains("clientId")) {
                continue;
            }
            if (storageKey.contains("tenantVersions")) {
                continue;
            }
            if (p.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }
    }

    public void removeThisClientItems(String ... keyEntries) {
        String regex = StringHelper.toPairsString(".*", keyEntries);
        removeThisClientItemsByRegex(regex);
    }
    
    public void pushTenantVersion(String clientId, TenantCacheVersionCO version) {
        String key = key("clientId", clientId, "type", "tenantVersions");
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.push(version);
        setItem(key, versions);
    }

    public TenantCacheVersionCO getCurrentTenantVersion(String clientId) {
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key("clientId", clientId, "type", "tenantVersions"));
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        return versions.getCurrentTenantVersion();
    }
    
    public void setCurrentTenant(String clientId, String tenantId) {
        String key = key("clientId", clientId, "type", "tenantVersions");
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.setCurrentTenant(tenantId);
        setItem(key, versions);
    }
    
    public String key(String ... keyEntries) {
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntries);
    }
    
    public String key(ChangeMarkerVO marker) {
        Map<String, String> map = new TreeMap<String, String>(marker.getKey());
        map.put("tenantId", marker.getTenantId());
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, map);
    }
    
    public Set<String> getKeys() {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < localStorage.getLength(); i++) {
            result.add(localStorage.key(i));
        }
        return result;
    }
}
