package com.attestator.admin.server.protocol.membuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

class MembufferManager {
    
    private Cache<String, ByteArrayOutputStream> cache = CacheBuilder.from("maximumSize=1000, expireAfterWrite=10s").build();
    
    public byte[] getBytes(String key) {
        ByteArrayOutputStream out = cache.getIfPresent(key);
        if (out != null) {
            return out.toByteArray();
        }
        return null;
    }
    
    public ByteArrayOutputStream getOutputStream(String key) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        cache.put(key, result);
        return result;
    }
    
    public ByteArrayInputStream getInputStream(String key) {
        byte[] bytes = getBytes(key);
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }    
}