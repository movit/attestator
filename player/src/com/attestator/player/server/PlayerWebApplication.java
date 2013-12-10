package com.attestator.player.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.attestator.admin.server.protocol.ProtocolHandlers;

public final class PlayerWebApplication implements ServletContextListener {
    private static Logger logger = Logger.getLogger(PlayerWebApplication.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ProtocolHandlers.register();
            Singletons.initialize(sce.getServletContext());            
                               
        } catch (Exception ex) {
            logger.error("Application cannot be started.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            Singletons.shutDown();
        } catch (Exception ex) {
            logger.error("Cannot shutdown the application", ex);
        }
    }
}
