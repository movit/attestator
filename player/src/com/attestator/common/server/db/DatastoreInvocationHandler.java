package com.attestator.common.server.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.TenantableVO;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class DatastoreInvocationHandler implements InvocationHandler {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DatastoreInvocationHandler.class);    
    private Datastore rawDs;
    
    private static final Method CREATE_QUERY = getMethod("createQuery", Class.class);
    private static final Method CREATE_UPDATE_OPERATIONS = getMethod("createUpdateOperations", Class.class);
    private static final Method UPDATE = getMethod("update", Query.class, UpdateOperations.class);
    private static final Method SAVE = getMethod("save", Object.class);
    private static final Method DELETE_QUERY = getMethod("delete", Query.class);
    private static final Method DELETE_OBJECT = getMethod("delete", Object.class);
    
    public DatastoreInvocationHandler(Datastore ds) {
        this.rawDs = ds;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        
        if (StringHelper.isEmptyOrNull(LoginManager.getThreadLocalTenatId())) {
            throw new IllegalAccessException("No current tenantId is set. Looks like you not logged in.");
        }
        
        if (CREATE_QUERY.equals(method) ) {
            Query<?> result = (Query<?>)method.invoke(rawDs, args);
            Class<?> clazz = (Class<?>)args[0];
            if (TenantableVO.class.isAssignableFrom(clazz)) {
                result.field("tenantId").equal(LoginManager.getThreadLocalTenatId());
            }
            return result;
        }
        else if (CREATE_UPDATE_OPERATIONS.equals(method)) {
            UpdateOperations<?> result = (UpdateOperations<?>)method.invoke(rawDs, args);
            Class<?> clazz = (Class<?>)args[0];
            if (ModificationDateAwareVO.class.isAssignableFrom(clazz)) {
                result.set("modified", new Date());
            }
            return result;
        }
        else if (UPDATE.equals(method)) {
            return method.invoke(rawDs, args);
        }
        else if (SAVE.equals(method)) {
            Object obj = args[0];
            if (obj instanceof TenantableVO) {
                ((TenantableVO) obj).setTenantId(LoginManager.getThreadLocalTenatId());
            }
            if (obj instanceof ModificationDateAwareVO) {
                Date now = new Date();
                if (((ModificationDateAwareVO) obj).getCreated() == null) {
                    ((ModificationDateAwareVO) obj).setCreated(now);
                }
                ((ModificationDateAwareVO) obj).setModified(now);
            }
            return method.invoke(rawDs, obj);
        }
        else if (DELETE_QUERY.equals(method)) {
            return method.invoke(rawDs, args);
        }
        else if (DELETE_OBJECT.equals(method)) {
            Object obj = args[0];
            if (obj instanceof TenantableVO) {
                ((TenantableVO) obj).setTenantId(LoginManager.getThreadLocalTenatId());
            }
            return method.invoke(rawDs, obj);
        }        
        else {
            throw new UnsupportedOperationException("Method: " + method + " not supported");
        }
    }    
    
    private static Method getMethod(String name, Class<?> ... params) {
        try {
            return Datastore.class.getMethod(name, params);
        }
        catch (Throwable e) {
            return null;
        }
    }
}
