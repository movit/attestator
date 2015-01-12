package com.attestator.player.client.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.attestator.common.client.helper.SerializationHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.CacheKind;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.player.client.cache.co.TenantCacheVersionCO;
import com.attestator.player.client.cache.co.TenantCacheVersionsCO;
import com.attestator.player.client.cache.co.VersionCO;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.storage.client.Storage;

public class PlayerStorageCache {
    
    public static final int VERSION = 22;
    private static final String KEY_PAIR_SEPARATOR = ", ";

    private Storage localStorage = Storage.getLocalStorageIfSupported();
    
    private PlayerStorageCache() {
        String key = key(CacheKind.internal, CacheType.cacheVersion);
        VersionCO version = getItem(VersionCO.class, key);
        if (version == null) {
            version = new VersionCO(); 
        }
        if (version.getVersion() < VERSION) {
            localStorage.clear();
        }
        setItem(key, new VersionCO(VERSION));
    }
    
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

    public void leaveOnlyThisItemsByRegex(String markerRegex, String leaveRegex) {
        RegExp marker = RegExp.compile(markerRegex);
        RegExp leave  = RegExp.compile(leaveRegex);
        
        for (int i = localStorage.getLength() - 1; i >= 0; i--) {
            String storageKey = localStorage.key(i);
            if (!marker.test(storageKey)) {
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
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key(CacheKind.internal, CacheType.tenantVersions)); 
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
            leaveOnlyThisItemsByRegex(marker(CacheKind.cache), sb.toString());
        }
    }

    public void pushTenantVersion(TenantCacheVersionCO version) {
        if (version.getTenantId() == null) {
            return;
        }        
        String key = key(CacheKind.internal, CacheType.tenantVersions);
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.push(version);
        setItem(key, versions);
    }

    public TenantCacheVersionCO getCurrentTenantVersion() {
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key(CacheKind.internal, CacheType.tenantVersions));
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        return versions.getCurrentTenantVersion();
    }
    
    public void setCurrentTenant(String tenantId) {
        if (tenantId == null) {
            return;
        }
        String key = key(CacheKind.internal, CacheType.tenantVersions);
        TenantCacheVersionsCO versions = getItem(TenantCacheVersionsCO.class, key);
        if (versions == null) {
            versions = new TenantCacheVersionsCO();
        }
        versions.setCurrentTenant(tenantId);
        setItem(key, versions);
    }
    
    public String marker(String paramName, String ... paramValues) {
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
    
    public String marker(CacheKind kind) {
        return marker(kind, null);
    }

    public String marker(CacheKind kind, CacheType type, String ... entries) {
        ArrayList<String> array = null;
        if (entries != null) {
            array = new ArrayList<String>(Arrays.asList(entries));
        }
        else {
            array = new ArrayList<String>();
        }
        
        if (kind != null) {
            array.add("kind");
            array.add(kind.toString());
        }
        
        if (type != null) {
            array.add("type");
            array.add(type.toString());
        }
        
        return StringHelper.toPairsString(".*", array.toArray(new String[0]));
    }
    
    public String keyWithTime(CacheKind kind, CacheType type, String ... keyEntries) {
        CheckHelper.throwIfNull(kind, "kind");
        CheckHelper.throwIfNull(type, "type");
        
        ArrayList<String> array = null;
        if (keyEntries != null) {
            array = new ArrayList<String>(Arrays.asList(keyEntries));
        }
        else {
            array = new ArrayList<String>();
        }
        
        array.add("kind");
        array.add(kind.toString());
    
        array.add("type");
        array.add(type.toString());
        
        array.add("a");
        array.add("" + System.currentTimeMillis());
        
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, array.toArray(new String[0]));
    }
    
    public String key(CacheKind kind, CacheType type, String ... keyEntries) {
        CheckHelper.throwIfNull(kind, "kind");
        CheckHelper.throwIfNull(type, "type");

        ArrayList<String> array = null;
        if (keyEntries != null) {
            array = new ArrayList<String>(Arrays.asList(keyEntries));
        }
        else {
            array = new ArrayList<String>();
        }
        
        array.add("kind");
        array.add(kind.toString());
        
        array.add("type");
        array.add(type.toString());
        
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, array.toArray(new String[0]));
    }
    
    public String key(ChangeMarkerVO marker) {
        TreeMap<String, String> keyMap = new TreeMap<String, String>(marker.getKey());
        
        keyMap.put("kind", CacheKind.cache.toString());
        keyMap.put("type", marker.getType().toString());
        keyMap.put("tenantId", marker.getTenantId());
        
        return StringHelper.toPairsString(KEY_PAIR_SEPARATOR, keyMap);
    }    
    
    public Map<String, String> keyMap(String key) {
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
    
    public String getKey(String regex) {
        Set<String> keys = getKeys(regex);
        if (!keys.isEmpty()) {
            return keys.iterator().next();
        }
        return null;
    }
}
