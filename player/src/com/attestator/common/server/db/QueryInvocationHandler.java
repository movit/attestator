package com.attestator.common.server.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

import com.attestator.common.server.db.SafeQuery.QueryType;
import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.common.shared.helper.CheckHelper;

public class QueryInvocationHandler implements InvocationHandler {
    private static Method GET_QUERY_TYPE = ReflectionHelper.getMethod(
            SafeQuery.class, "getQueryType");
    
    private static Method GET_RAW_QUERY = ReflectionHelper.getMethod(
            SafeQuery.class, "getRawQuery");

    private QueryType queryType;
    private Query<?> rawQ;

    public QueryInvocationHandler(Query<?> rawQ, QueryType queryType) {
        CheckHelper.throwIfNull(rawQ, "rawQ");
        CheckHelper.throwIfNull(queryType, "queryType");

        this.rawQ = rawQ;
        this.queryType = queryType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (GET_QUERY_TYPE.equals(method)) {
            return queryType;
        }
        else if (GET_RAW_QUERY.equals(method)) {
            return rawQ;
        }
        if (QueryResults.class.equals(method.getDeclaringClass())) {
            if (queryType != QueryType.queryForFetch) {
                throw new IllegalArgumentException("query should be queryForFetch");
            }
        }
        return method.invoke(rawQ, args);
    }
}
