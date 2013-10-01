package com.attestator.player.server;

import java.lang.reflect.Proxy;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletContext;

import com.attestator.admin.server.AdminLogic;
import com.attestator.common.server.db.DatabaseUpdater;
import com.attestator.common.server.db.DatastoreInvocationHandler;
import com.attestator.common.server.db.Interceptor;
import com.attestator.common.server.helper.ServerHelper;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class Singletons {
    private static Morphia      morphia;
    private static Mongo        mongo;
    private static Datastore    rawDs;
    private static Datastore    ds;
    private static DB           db;
    private static PlayerLogic  pl;
    private static AdminLogic   al;
    
    public static Datastore rawDs() {
        return rawDs;
    }

    public static Datastore ds() {
        return ds;
    }
    
    public static DB db() {
        return db;
    }

    public static PlayerLogic pl() {
        return pl;
    }

    public static AdminLogic al() {
        return al;
    }
    
    /**
     * Invoked when the web application starts.
     */
    public static void initialize(ServletContext sc) throws Exception {
        // Connect to database
        mongo = new MongoClient(ServerHelper.getInitParameter(sc, "mongo.host", "localhost"));
        db = mongo.getDB("attestator");
        if ("true".equals(ServerHelper.getInitParameter(sc, "mongo.auth", "false"))) {
            boolean auth = db.authenticate(
                    ServerHelper.getInitParameter(sc, "mongo.login", "attestator"), 
                    ServerHelper.getInitParameter(sc, "mongo.password", "attestator").toCharArray()
            );
            if (!auth) {
                throw new LoginException("Can't authenticate to mongo.");
            }
        }
        morphia = new Morphia();
        rawDs   = morphia.createDatastore(mongo, "attestator");
        ds      = (Datastore) Proxy.newProxyInstance(Datastore.class.getClassLoader(), new Class[] {Datastore.class}, new DatastoreInvocationHandler(rawDs)); 
        morphia.mapPackage("com.attestator.common.shared.vo");
        morphia.getMapper().addInterceptor(new Interceptor());
        pl      = new PlayerLogic();
        al      = new AdminLogic();
        DatabaseUpdater.updateDatabase();
    }    
    
    /**
     * Invoked when the web application stops.
     */
    public static void shutDown() throws Exception {
        if (mongo != null) {
            mongo.close();
        }
    }
}