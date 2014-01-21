package com.attestator.common.server.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.attestator.common.server.db.annotation.Reference;
import com.attestator.common.server.db.annotation.ReferenceCount;
import com.attestator.common.server.db.annotation.SetOnSave;
import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.AbstractEntityInterceptor;
import com.google.code.morphia.mapping.MappedClass;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.Query;
import com.mongodb.DBObject;

public class Interceptor extends AbstractEntityInterceptor {
    private static Logger logger = Logger.getLogger(Interceptor.class);    
    
    @SuppressWarnings({ "rawtypes"})
    @Override
    public void postLoad(Object obj, DBObject dbo, Mapper mapper) {
        try {
            // Load @Reference annotated fields
            MappedClass mappedObjClazz = mapper.getMappedClass(obj);
            Field[] refernceFields = ReflectionHelper
                    .getAnnotatedDeclaredFields(obj.getClass(),
                            Reference.class, true);

            for (Field refernceField : refernceFields) {
                refernceField.setAccessible(true);

                Reference ref = (Reference) refernceField
                        .getAnnotation(Reference.class);
                MappedField fromField = mappedObjClazz.getMappedField(ref
                        .fromField());
                if (fromField == null) {
                    logger.error("Can't load " + refernceField.getName()
                            + " for " + obj.getClass().getName()
                            + " fromField " + ref.fromField()
                            + " does not exists.");
                    continue;
                }

                MappedClass mappedToClazz = mapper.getMappedClass(refernceField
                        .getType());
                
                MappedField toField = mappedToClazz.getMappedField(ref
                        .toField());
                if (toField == null) {
                    logger.error("Can't load " + refernceField.getName()
                            + " for " + obj.getClass().getName() + " toField "
                            + ref.toField() + " does not exists in "
                            + refernceField.getType().getName());
                    continue;
                }

                Object referenceValue = fromField.getFieldValue(obj);
                if (referenceValue == null) {
                    continue;
                }

                Query q = Singletons.ds().createFetchQuery(refernceField.getType());
                q.field(toField.getNameToStore()).equal(referenceValue);

                List<String> excludeList = new ArrayList<String>(
                        Arrays.asList(ref.excludeFields()));
                List<String> includeList = new ArrayList<String>(
                        Arrays.asList(ref.includeFields()));

                if (!includeList.isEmpty()) {
                    if (!excludeList.isEmpty()) {
                        includeList.removeAll(excludeList);
                    }
                    q.retrievedFields(true, includeList.toArray(new String[0]));
                } else if (!excludeList.isEmpty()) {
                    q.retrievedFields(false, excludeList.toArray(new String[0]));
                }

                refernceField.set(obj, q.get());
            }
            
            
            // Load @ReferenceCount annotated fields
            Field[] refernceCountFields = ReflectionHelper
                    .getAnnotatedDeclaredFields(obj.getClass(),
                            ReferenceCount.class, true);

            for (Field refernceCountField : refernceCountFields) {
                refernceCountField.setAccessible(true);
                if (!refernceCountField.getType().isAssignableFrom(Long.class)) {
                    logger.error("Can't load " + refernceCountField.getName()
                            + " for " + obj.getClass().getName()
                            + " this field should be assignable from Long");
                    continue;
                }

                ReferenceCount refCount = (ReferenceCount) refernceCountField
                        .getAnnotation(ReferenceCount.class);
                MappedField fromField = mappedObjClazz.getMappedField(refCount
                        .fromField());
                if (fromField == null) {
                    logger.error("Can't load " + refernceCountField.getName()
                            + " for " + obj.getClass().getName()
                            + " fromField " + refCount.fromField()
                            + " does not exists.");
                    continue;
                }
                
                MappedClass mappedToClazz = mapper.getMappedClass(refCount.toClass());

                Object referenceValue = fromField.getFieldValue(obj);
                if (referenceValue == null) {
                    continue;
                }

                Query q = Singletons.ds().createFetchQuery(mappedToClazz.getClazz());
                q.field(refCount.toField()).equal(referenceValue);

                refernceCountField.set(obj, q.countAll());
            }
            
        } catch (Throwable e) {
            logger.error("postLoad error", e);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void prePersist(Object obj, DBObject dbo, Mapper mapper) {
        try {
            
            MappedClass objMappedClass = mapper.getMappedClass(obj);
            Field[] updatedFields = ReflectionHelper
                    .getAnnotatedDeclaredFields(obj.getClass(),
                            SetOnSave.class, true);

            for (Field updatedField : updatedFields) {
                updatedField.setAccessible(true);

                SetOnSave setAnnotation = (SetOnSave) updatedField
                        .getAnnotation(SetOnSave.class);

                // Get ref value
                MappedField refMappedField = objMappedClass.getMappedField(setAnnotation.refField());
                Field refField = refMappedField.getField();
                refField.setAccessible(true);
                String refValue = (String) refField.get(obj);
                if (refValue == null) {
                    continue;
                }

                // Explore target class
                MappedClass targetMappedClass = mapper.getMappedClass(setAnnotation.targetClass());
                Class<?> targetClass = targetMappedClass.getClazz();
                
                // Get target value field
                MappedField valueMappedField = targetMappedClass.getMappedField(setAnnotation.targetValueField());
                Field valueField = valueMappedField.getField();
                valueField.setAccessible(true);
                
                // Get target id field
                MappedField idMappedField = targetMappedClass.getMappedField(setAnnotation.targetIdField());
                Field idField = idMappedField.getField();
                idField.setAccessible(true);                

                Query q = Singletons.ds().createFetchQuery(targetClass);
                q.field(idMappedField.getNameToStore()).equal(refValue);
                q.retrievedFields(true, valueMappedField.getNameToStore());

                Object target = q.get();
                Object updateValue = null;

                if (target != null) {
                    updateValue = valueField.get(target);
                }

                updatedField.set(obj, updateValue);
            }
        } catch (Throwable e) {
            logger.error("PrePersist error", e);
        }
    }
}
