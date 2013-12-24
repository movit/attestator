package com.attestator.admin.server.protocol.membuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MembufferConnection extends URLConnection {
    private static MembufferManager membufferManager = new MembufferManager();
    
    protected MembufferConnection(URL url) {
        super(url);        
    }

    @Override
    public void connect() throws IOException {        
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return membufferManager.getOutputStream(getURL().toString());
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return membufferManager.getInputStream(getURL().toString());
    }
    
    public static MembufferManager getMembufferManager() {
        return membufferManager;
    }
}
