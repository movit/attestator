package com.attestator.common.shared.helper;


public class Base62Helper {
    
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";    
    private static final int BASE = ALPHABET.length();
    
    private static long encode(long i, final StringBuilder sb) {
        int rem = (int)(i % BASE);
        sb.append(ALPHABET.charAt(rem));
        return i / BASE;
    }
    
    public static String encode(long i) {
        StringBuilder sb = new StringBuilder("");
        while (i > 0) {
            i = encode(i, sb);
        }
        return sb.reverse().toString();        
    }
    
    public static String getRandomBase62LongId() {
        long seed = (long)(Math.random() * Long.MAX_VALUE);
        return encode(seed);
    }
    
    public static String getRandomBase62IntId() {
        int seed = (int)(Math.random() * Integer.MAX_VALUE);
        return encode(seed);
    }
}
