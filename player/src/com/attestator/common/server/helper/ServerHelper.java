package com.attestator.common.server.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

public class ServerHelper {
    private static final Logger logger = Logger
            .getLogger(ServerHelper.class);
    
    public static String getCookieValue(Cookie[] cookies, String cookieName,
            String defaultValue) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return defaultValue;
    }

    public static byte[] readBytes(InputStream ios) {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = ios.read(buffer)) != -1)
                ous.write(buffer, 0, read);
        } catch (IOException e) {
            return null;
        } finally {
            try {
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

        return ous.toByteArray();
    }

    public static String getInitParameter(ServletContext sc, String param,
            String defaultValue) {
        String result = sc.getInitParameter(param);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    public static URL saveToMembuffer(byte[] content) {
        try {
            URL url = new URL("membuffer://" + UUID.randomUUID().toString());

            URLConnection connection = url.openConnection();
            OutputStream out = connection.getOutputStream();

            out.write(content);
            out.close();

            return url;
        } catch (Throwable e) {
            logger.error("Membuffer saving error", e);
        }
        return null;
    }
    
    public static OutputStream createNonClosingProxy(final OutputStream out) {
        return new OutputStream() {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
            }
            @Override
            public void flush() throws IOException {
                out.flush();
            }
            @Override
            public void close() throws IOException {
                out.flush();
            }
            @Override
            public void write(int b) throws IOException {
                out.write(b);                
            }
        };
    }
}
