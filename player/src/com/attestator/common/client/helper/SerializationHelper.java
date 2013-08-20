package com.attestator.common.client.helper;

import org.dellroad.lzma.client.LZMAByteArrayCompressor;
import org.dellroad.lzma.client.LZMAByteArrayDecompressor;
import org.dellroad.lzma.client.UTF8;

import com.google.gwt.core.shared.GWT;
import com.kfuntak.gwt.json.serialization.client.Serializer;


public class SerializationHelper {

    public static <T> byte[] serializeCompressed(T obj) {
        String uncompressed = serializeJSON(obj);
        LZMAByteArrayCompressor compressor = new LZMAByteArrayCompressor(UTF8.encode(uncompressed));
        while (compressor.execute());
        byte[] result = compressor.getCompressedData();        
        return result;
    }

    public static <T> T deserializeCompressed(Class<T> clazz, byte[] bytes) {
        try {
            LZMAByteArrayDecompressor decompressor = new LZMAByteArrayDecompressor(bytes);
            while (decompressor.execute());
            byte[] uncompressedBytes = decompressor.getUncompressedData();
            String uncompressedString = UTF8.decode(uncompressedBytes);
            T result = deserializeJSON(clazz, uncompressedString);
            return result;
        }
        catch (Throwable e) {
            return null;
        }
    }
        
    public static <T> String serializeCompressedBase64(T obj) {
        byte[] comressedBytes = serializeCompressed(obj);
        String result = Base64.toBase64(comressedBytes);
        return result;
    }

    public static <T> T deserializeCompressedBase64(Class<T> clazz, String str) {
        byte[] compressedBytes = Base64.fromBase64(str);
        T result = deserializeCompressed(clazz, compressedBytes);
        return result;
    }
    
    public static <T> String serializeJSON(T obj) {        
        Serializer serializer = GWT.create(Serializer.class);
        String result = serializer.serialize(obj);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeJSON(Class<T> clazz, String str) {
        Serializer serializer = GWT.create(Serializer.class);
        T result = (T)serializer.deSerialize(str, clazz.getName());
        return result;
    }
    
    public static <T> String serialize(T obj) {        
        return serializeJSON(obj);
    }

    public static <T> T deserialize(Class<T> clazz, String str) {        
        return deserializeJSON(clazz, str);
    }
}
