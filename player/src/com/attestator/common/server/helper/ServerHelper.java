package com.attestator.common.server.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

public class ServerHelper {
    public static String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue) {
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return defaultValue;
    }
    
    public static byte[] readBytes(InputStream ios) {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1)
                ous.write(buffer, 0, read);
        } 
        catch (IOException e) {
            // swallow, since not that important           
        } 
        finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
                // swallow, since not that important
            }
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                // swallow, since not that important
            }
        }
        
        if (ous != null) {
            return ous.toByteArray();
        }
        else {
            return null;
        }
    }
    
    public static String getInitParameter(ServletContext sc, String param, String defaultValue) {
        String result = sc.getInitParameter(param);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }
}
