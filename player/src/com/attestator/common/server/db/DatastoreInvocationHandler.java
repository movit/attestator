package com.attestator.common.server.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.server.db.SafeQuery.QueryType;
import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.ShareableVO;
import com.attestator.common.shared.vo.TenantableVO;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class DatastoreInvocationHandler implements InvocationHandler {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DatastoreInvocationHandler.class);    
    private Datastore rawDs;
    
    private static final Method CREATE_QUERY = ReflectionHelper.getMethod(SafeDatastore.class, "createQuery", Class.class);
    private static final Method CREATE_UPDATE_QUERY = ReflectionHelper.getMethod(SafeDatastore.class, "createUpdateQuery", Class.class);
    private static final Method CREATE_FETCH_QUERY = ReflectionHelper.getMethod(SafeDatastore.class, "createFetchQuery", Class.class);
    private static final Method CREATE_UPDATE_OPERATIONS = ReflectionHelper.getMethod(SafeDatastore.class, "createUpdateOperations", Class.class);
    private static final Method UPDATE = ReflectionHelper.getMethod(SafeDatastore.class, "update", Query.class, UpdateOperations.class);
    private static final Method SAVE = ReflectionHelper.getMethod(SafeDatastore.class, "save", Object.class);
    private static final Method SAVE_IT = ReflectionHelper.getMethod(SafeDatastore.class, "save", Iterable.class);
    private static final Method DELETE_QUERY = ReflectionHelper.getMethod(SafeDatastore.class, "delete", Query.class);
    private static final Method DELETE_OBJECT = ReflectionHelper.getMethod(SafeDatastore.class, "delete", Object.class);
    
    public DatastoreInvocationHandler(Datastore ds) {
        this.rawDs = ds;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        
        if (StringHelper.isEmptyOrNull(LoginManager.getThreadLocalTenatId())) {
            throw new IllegalAccessException("No current tenantId is set. Looks like you not logged in.");
        }
        
        if (CREATE_FETCH_QUERY.equals(method) ) {
            Query<?> rawQ = (Query<?>)CREATE_QUERY.invoke(rawDs, args);
            SafeQuery<?> result = cretaeSafeQueryProxy(rawQ, QueryType.queryForFetch);
            
            Class<?> clazz = (Class<?>)args[0];
            if (ShareableVO.class.isAssignableFrom(clazz)) {
                result.field("sharedForTenantIds").hasThisOne(LoginManager.getThreadLocalTenatId());
            }
            else if (TenantableVO.class.isAssignableFrom(clazz)) {
                result.field("tenantId").equal(LoginManager.getThreadLocalTenatId());
            }
            
            return result;
        }
        else if (CREATE_UPDATE_QUERY.equals(method)) {
            Query<?> rawQ = (Query<?>)CREATE_QUERY.invoke(rawDs, args);
            SafeQuery<?> result = cretaeSafeQueryProxy(rawQ, QueryType.queryForUpdate);
            
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
            Query<?> q = (Query<?>)args[0];
            throwIfNotQueryForUpdate(q);
            args[0] = ((SafeQuery<?>)q).getRawQuery();
            return method.invoke(rawDs, args);
        }
        else if (SAVE.equals(method)) {
            Object obj = args[0];
            thowIfNotWriteable(obj);
            prepareForSave(obj);
            return method.invoke(rawDs, obj);
        }
        else if (SAVE_IT.equals(method)) {
            //TODO is iterator should be reset here?
            Iterable<?> it = (Iterable<?>)args[0];
            for (Object obj : it) {
                thowIfNotWriteable(obj);
                prepareForSave(obj);
            }
            return method.invoke(rawDs, it);
        }
        else if (DELETE_QUERY.equals(method)) {
            Query<?> q = (Query<?>)args[0];
            throwIfNotQueryForUpdate(q);
            args[0] = ((SafeQuery<?>)q).getRawQuery();
            return method.invoke(rawDs, args);
        }
        else if (DELETE_OBJECT.equals(method)) {
            Object obj = args[0];
            thowIfNotWriteable(obj);
            return method.invoke(rawDs, obj);
        }        
        else {
            throw new UnsupportedOperationException("Method: " + method + " not supported");
        }
    }   
    
    private void throwIfNotQueryForUpdate(Query<?> q) {
        if (getQueryType(q) != QueryType.queryForUpdate) {
            throw new IllegalArgumentException("query should be queryForUpdate");
        }
    }
    
    private QueryType getQueryType(Query<?> q) {
        CheckHelper.throwIfNullOrNotImplement(q, SafeQuery.class, "query");
       
        return ((SafeQuery<?>) q).getQueryType();
    }
    
    private SafeQuery<?> cretaeSafeQueryProxy(Query<?> rawQ, QueryType queryType) {
        return (SafeQuery<?>) Proxy.newProxyInstance(SafeQuery.class.getClassLoader(), new Class[] {SafeQuery.class}, new QueryInvocationHandler(rawQ, queryType));
    }
    
    private void thowIfNotWriteable(Object obj) {        
        if (obj instanceof TenantableVO) {
            Query<?> q = rawDs.createQuery(obj.getClass());
            q.field("_id").equal(((TenantableVO) obj).getId());
            q.retrievedFields(true, "tenantId");
            
            TenantableVO dbObj = (TenantableVO)q.get();
            
            if (dbObj != null && !LoginManager.getThreadLocalTenatId().equals(dbObj.getTenantId())) {
                throw new IllegalArgumentException("Object us not writeable for tenant: " + LoginManager.getThreadLocalTenatId() + ", obj: " + obj.toString());               
            }
        }
    }
    
    private void prepareForSave(Object obj) {        
        if (obj instanceof TenantableVO) {
            ((TenantableVO) obj).setTenantId(LoginManager.getThreadLocalTenatId());
        }
        if (obj instanceof ShareableVO) {
            ((ShareableVO) obj).getSharedForTenantIds().add(LoginManager.getThreadLocalTenatId());
        }
        if (obj instanceof ModificationDateAwareVO) {
            Date now = new Date();
            if (((ModificationDateAwareVO) obj).getCreated() == null) {
                ((ModificationDateAwareVO) obj).setCreated(now);
            }
            ((ModificationDateAwareVO) obj).setModified(now);
        }
    }
}
