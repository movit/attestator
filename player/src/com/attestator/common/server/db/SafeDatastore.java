package com.attestator.common.server.db;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

public interface SafeDatastore extends Datastore {
    <T> Query<T> createUpdateQuery(Class<T> clazz);
    <T> Query<T> createFetchQuery(Class<T> clazz);
}
