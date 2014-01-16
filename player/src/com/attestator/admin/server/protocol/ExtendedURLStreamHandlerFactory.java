package com.attestator.admin.server.protocol;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import com.attestator.common.shared.helper.CheckHelper;


public class ExtendedURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static ExtendedURLStreamHandlerFactory instance;
    
    private Map<String, Class<? extends URLStreamHandler>> handlers = new HashMap<String, Class<? extends URLStreamHandler>>();
    private URLStreamHandlerFactory baseFactory;
    
    private ExtendedURLStreamHandlerFactory(URLStreamHandlerFactory baseFactory) {
        super();
        this.baseFactory = baseFactory;
    }

    public void registerURLStreamHandler(Class<? extends URLStreamHandler> handler, String protocolName) {
        CheckHelper.throwIfNull(handler, "handler");
        CheckHelper.throwIfNull(protocolName, "protocolName");
        handlers.put(protocolName, handler);
    }
    
    public void registerURLStreamHandler(Class<? extends URLStreamHandler> handler) {
        String packageName = handler.getPackage().getName();
        String[] packageNameParts = packageName.split("\\.");
        String protocolName = packageNameParts[packageNameParts.length - 1];
        registerURLStreamHandler(handler, protocolName);
    }    
    
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        Class<? extends URLStreamHandler> handlerClass = handlers.get(protocol);
        if (handlerClass != null) {
            try {
                return (URLStreamHandler)handlerClass.newInstance();
            }
            catch (Throwable e) {                
            }
        }
        
        if (baseFactory != null) {
            return baseFactory.createURLStreamHandler(protocol);
        }
        
        return null;
    }
    
    public synchronized static ExtendedURLStreamHandlerFactory getInstance() {
        if (instance == null) {
            try {
                Field factoryField = URL.class.getDeclaredField("factory");
                
                factoryField.setAccessible(true);
                URLStreamHandlerFactory baseFactory = (URLStreamHandlerFactory)factoryField.get(null);
                instance = new ExtendedURLStreamHandlerFactory(baseFactory);
                factoryField.set(null, instance);
            }
            catch (Throwable e) {            
            }
        }
 
        return instance;
    }
}
