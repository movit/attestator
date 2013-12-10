package com.attestator.admin.server.protocol;

import org.apache.log4j.Logger;

import com.attestator.common.shared.helper.StringHelper;

public class ProtocolHandlers {
    private static final Logger logger = Logger.getLogger(ProtocolHandlers.class);
    
    public static void register() {
        String pkgs = System.getProperty("java.protocol.handler.pkgs");
        logger.debug("Was java.protocol.handler.pkgs: " + System.getProperty("java.protocol.handler.pkgs"));
        if (StringHelper.isEmptyOrNull(pkgs)) {
            pkgs = ProtocolHandlers.class.getPackage().getName();
        }
        else {
            pkgs = pkgs + "|" + ProtocolHandlers.class.getPackage().getName();
        }
        System.setProperty("java.protocol.handler.pkgs", pkgs);
        logger.debug("Now java.protocol.handler.pkgs: " + System.getProperty("java.protocol.handler.pkgs"));
    }
}
