package com.attestator.player.client.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.attestator.common.client.helper.SerializationHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.cache.co.TenantCacheVersionsCO;
import com.attestator.player.client.cache.co.VersionCO;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.storage.client.Storage;

public class PlayerStorageCache {
    private static final int VERSION = 3;
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
        VersionCO version = getItem(VersionCO.class, "version");
        if (version == null) {
            version = new VersionCO(); 
        }
        if (version.getVersion() < VERSION) {
            localStorage.clear();
        }
        setItem("version", new VersionCO(VERSION));
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
    
    public String valuesMarker(String paramName, String ... paramValues) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("(");
        int i = 0;
        for (String paramValue : paramValues) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append("(" + paramName + "=" + paramValue + ")");
            i++;
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    public String marker(String ... entries) {
        return StringHelper.toPairsString(".*", entries);
    }
    
    public void leaveOnlyThisItemsByRegex(String markerRegex, String leaveRegex) {
        RegExp marker = RegExp.compile(markerRegex);
        RegExp leave  = RegExp.compile(leaveRegex);
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!marker.test(storageKey)) {
                // Process only items identified by marker
                continue;
            }
            if (!leave.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }
    }
    
    public void removeThisItemsByRegex(String markerRegex, String removeRegex) {        
        RegExp remove  = RegExp.compile(removeRegex);
        RegExp marker = RegExp.compile(markerRegex);
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!marker.test(storageKey)) {
                continue;
            }
            if (remove.test(storageKey)) {
                localStorage.removeItem(storageKey);
            }
        }
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
            leaveOnlyThisItemsByRegex(marker("kind", "cache"), sb.toString());
        }
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

    public String renewKey(String ... keyEntries) {
        ArrayList<String> keyEntriesList = new ArrayList<String>(Arrays.asList(keyEntries));
        keyEntriesList.add("kind");
        keyEntriesList.add("renew");
        keyEntriesList.add("a");
        keyEntriesList.add("" + System.currentTimeMillis());
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntriesList.toArray(new String[0]));
    }
    
    public String reportKey(String ... keyEntries) {
        ArrayList<String> keyEntriesList = new ArrayList<String>(Arrays.asList(keyEntries));
        keyEntriesList.add("kind");
        keyEntriesList.add("report");
        keyEntriesList.add("a");
        keyEntriesList.add("" + System.currentTimeMillis());
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyEntriesList.toArray(new String[0]));
    }
    
    public Map<String, String> key(String key) {
        Map<String, String> result = new TreeMap<String, String>();
        String[] pairs = key.split(KEY_PAIR_SEPARATOR);
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i].split("=", 2);
            String pairKey = pair[0];
            String pairValue = pair.length > 1 ? pair[1] : null;
            result.put(pairKey, pairValue);            
        }
        return result;
    }
    
    public Set<String> getKeys() {
        Set<String> result = new TreeSet<String>();
        for (int i = 0; i < localStorage.getLength(); i++) {
            result.add(localStorage.key(i));
        }
        return result;
    }
    
    public Set<String> getKeys(String regex) {
        RegExp p = RegExp.compile(regex);
        Set<String> result = new TreeSet<String>();
        for (int i = 0; i < localStorage.getLength(); i++) {
            String key = localStorage.key(i);
            if (p.test(key)) {
                result.add(localStorage.key(i));
            }            
        }
        return result;
    }
    
}
