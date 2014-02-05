package com.attestator.common.server.db;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

public interface SafeDatastore extends Datastore {
    <T> Query<T> createUpdateQuery(Class<T> clazz);
    <T> Query<T> createFetchQuery(Class<T> clazz);
}
