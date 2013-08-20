package com.attestator.player.client.cache;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int VERSION = 1;
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

    private PlayerStorageCache() {
        String versionStr = localStorage.getItem("version");
        Integer version = 0;
        if (versionStr == null) {
            version = SerializationHelper.deserialize(Integer.class, versionStr); 
        }
        if (version < VERSION) {
            localStorage.clear();
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
    
    public void leaveOnlyThisKindItemsByRegex(String kind, String regex) {
        RegExp p = RegExp.compile(regex);
        String kindMarker = "kind=" + kind;
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!storageKey.contains(kindMarker)) {
                // Process only items of specified kind
                continue;
            }
            if (!p.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }        
    }
    
    public void leaveOnlyThisKindItems(String kind, String ... keyEntries) {
        String regex = StringHelper.toPairsString(".*", keyEntries);
        leaveOnlyThisKindItemsByRegex(kind, regex);
    }
    
    public void removeOrphanCacheTenantItems() {
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key("type", "tenantVersions")); 
        if (versions != null && versions.getItems().size() > 0) {
            StringBuilder sb = new StringBuilder();
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
            leaveOnlyThisKindItemsByRegex("cache", sb.toString());
        }
    }

    public void removeThisKindItemsByRegex(String kind, String regex) {        
        RegExp p = RegExp.compile(regex);
        String kindMarker = "kind=" + kind;
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!storageKey.contains(kindMarker)) {
                continue;
            }
            if (p.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }
    }
    
    public void removeCacheItems(String ... keyEntries) {
        removeThisKindItems("cache", keyEntries);
    }

    public void removeThisKindItems(String kind, String ... keyEntries) {
        String regex = StringHelper.toPairsString(".*", keyEntries);
        removeThisKindItemsByRegex(kind, regex);
    }
    
    public void pushTenantVersion(TenantCacheVersionCO version) {
        if (version.getTenantId() == null) {
            return;
        }        
        String key = key("type", "tenantVersions");
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.push(version);
        setItem(key, versions);
    }

    public TenantCacheVersionCO getCurrentTenantVersion() {
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key("type", "tenantVersions"));
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        return versions.getCurrentTenantVersion();
    }
    
    public void setCurrentTenant(String tenantId) {
        if (tenantId == null) {
            return;
        }
        String key = key("type", "tenantVersions");
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.setCurrentTenant(tenantId);
        setItem(key, versions);
    }
    
    
    public String key(Map<String, String> map) {
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, map);
    }
    
    public String key(String ... keyEntries) {
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntries);
    }
    
    public String cacheKey(String ... keyEntries) {
        ArrayList<String> keyEntriesList = new ArrayList<String>(Arrays.asList(keyEntries));
        keyEntriesList.add("kind");
        keyEntriesList.add("cache");
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntriesList.toArray(new String[0]));
    }
    
    public String cacheKey(ChangeMarkerVO marker) {
        Map<String, String> map = new TreeMap<String, String>(marker.getKey());
        map.put("tenantId", marker.getTenantId());
        map.put("kind", "cache");
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, map);
    }

    public String reportKey(String ... keyEntries) {
        ArrayList<String> keyEntriesList = new ArrayList<String>(Arrays.asList(keyEntries));
        keyEntriesList.add("kind");
        keyEntriesList.add("report");
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntriesList.toArray(new String[0]));
    }
    
    public Set<String> getKeys() {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < localStorage.getLength(); i++) {
            result.add(localStorage.key(i));
        }
        return result;
    }
}
