package com.attestator.admin.server.protocol.membuffer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class MembufferURLStreamHandler extends URLStreamHandler {
    
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new MembufferConnection(url);
    }
}
