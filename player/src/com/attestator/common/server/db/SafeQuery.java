package com.attestator.common.server.db;

import org.mongodb.morphia.query.Query;

interface SafeQuery<T> extends Query<T> {
    static enum QueryType {
        queryForFetch,
        queryForUpdate
    }
    QueryType getQueryType();
    Query<?> getRawQuery();
}
