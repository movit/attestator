package com.attestator.common.shared.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import com.google.code.morphia.annotations.Id;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class BaseVO implements Serializable, JsonSerializable{
	private static final long serialVersionUID = 6796925180284904176L;
	
	
	@Id
	private String id = idString();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void resetIdentity() {
	    this.id = idString();
	}
	
    public static String idString() {
        byte b[] = idByteArray();

        StringBuilder buf = new StringBuilder(24);

        for (int i = 0; i < b.length; i++) {
            int x = b[i] & 0xFF;
            String s = Integer.toHexString(x);
            if (s.length() == 1) {
                buf.append("0");
            }
            buf.append(s);
        }

        return buf.toString();
    }

    public static byte[] idByteArray() {
        Random rnd = new Random();
        byte b[] = new byte[12];
        putInt(rnd.nextInt(), b, 0);
        putInt(rnd.nextInt(), b, 4);
        putInt(flip((int) ((new Date()).getTime() / 1000)), b, 8);
        reverse(b);
        return b;
    }

    private static void putInt(int val, byte[] bytes, int startIndex) {
        final int BYTE_MASK = 0x000000FF;
        for (int i = 0; i < 4; i++) {
            bytes[i + startIndex] = (byte) (val & BYTE_MASK);
            val = val >> 8;
        }
    }

    private static void reverse(byte[] b) {
        for (int i = 0; i < b.length / 2; i++) {
            byte t = b[i];
            b[i] = b[b.length - (i + 1)];
            b[b.length - (i + 1)] = t;
        }
    }

    private static int flip(int x) {
        int z = 0;
        z |= ((x << 24) & 0xFF000000);
        z |= ((x << 8) & 0x00FF0000);
        z |= ((x >> 8) & 0x0000FF00);
        z |= ((x >> 24) & 0x000000FF);
        return z;
    }
}
