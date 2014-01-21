package com.attestator.common.server.db;

import com.google.code.morphia.query.Query;

interface SafeQuery<T> extends Query<T> {
    static enum QueryType {
        queryForFetch,
        queryForUpdate
    }
    QueryType getQueryType();
    Query<?> getRawQuery();
}
