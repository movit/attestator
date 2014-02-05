package com.attestator.common.server.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ReflectionHelper {

    /**
     * Create new instance of specified class and type
     * 
     * @param clazz
     *            of instance
     * @param <T>
     *            type of object
     * @return new Class instance
     */
    public static <T> T getInstance(Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return t;
    }

    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by this <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field.  Note that this method
     * will not reflect the <code>length</code> field of an array class.
     *
     * @param name the name of the field
     * @param recursively look in superclasses too
     * @return the <code>Field</code> object for the specified field in this
     * class
     * @exception NoSuchFieldException if a field with the specified name is
     *              not found.
     * @exception NullPointerException if <code>name</code> is <code>null</code>
     * @exception  SecurityException
     *             If a security manager, <i>s</i>, is present and any of the
     *             following conditions is met:
     *
     *             <ul>
     *
     *             <li> invocation of 
     *             <tt>{@link SecurityManager#checkMemberAccess
     *             s.checkMemberAccess(this, Member.DECLARED)}</tt> denies
     *             access to the declared field
     *
     *             <li> the caller's class loader is not the same as or an
     *             ancestor of the class loader for the current class and
     *             invocation of <tt>{@link SecurityManager#checkPackageAccess
     *             s.checkPackageAccess()}</tt> denies access to the package
     *             of this class
     *
     *             </ul>
     *
     * @since JDK1.1
     */
    public static Field getDeclaredField(Class clazz, String name, boolean recursively) throws NoSuchFieldException {        
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        }
        catch (NoSuchFieldException e) {
            if (!recursively) {
                throw e;
            }
        }
        
        if (field == null) {
            Class superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                return getDeclaredField(superClazz, name, recursively);
            }
            else {
                throw new NoSuchFieldException(name);
            }
        }
        
        return field;
    }

    /**
     * Retrieving fields list of specified class 
     * retrieving fields from all class hierarchy
     * stopping on stopSuperclass
     * 
     * @param clazz
     *            where fields are searching
     * @param recursively
     *            param
     * @return list of fields
     */
    public static Field[] getDeclaredFields(Class clazz, Class stopSuperlass) {
        List<Field> fields = new LinkedList<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        Collections.addAll(fields, declaredFields);

        Class superClass = clazz.getSuperclass();

        if (superClass != null && !superClass.equals(stopSuperlass)) {
            Field[] declaredFieldsOfSuper = getDeclaredFields(superClass,
                    stopSuperlass);
            if (declaredFieldsOfSuper.length > 0)
                Collections.addAll(fields, declaredFieldsOfSuper);
        }

        return fields.toArray(new Field[fields.size()]);
    }
    

    /**
     * Retrieving fields list of specified class If recursively is true,
     * retrieving fields from all class hierarchy
     * 
     * @param clazz
     *            where fields are searching
     * @param recursively
     *            param
     * @return list of fields
     */
    public static Field[] getDeclaredFields(Class clazz, boolean recursively) {
        List<Field> fields = new LinkedList<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        Collections.addAll(fields, declaredFields);

        Class superClass = clazz.getSuperclass();

        if (superClass != null && recursively) {
            Field[] declaredFieldsOfSuper = getDeclaredFields(superClass,
                    recursively);
            if (declaredFieldsOfSuper.length > 0)
                Collections.addAll(fields, declaredFieldsOfSuper);
        }

        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * Retrieving fields list of specified class and which are annotated by
     * incoming annotation class If recursively is true, retrieving fields from
     * all class hierarchy
     * 
     * @param clazz
     *            - where fields are searching
     * @param annotationClass
     *            - specified annotation class
     * @param recursively
     *            param
     * @return list of annotated fields
     */
    public static Field[] getAnnotatedDeclaredFields(Class clazz,
            Class<? extends Annotation> annotationClass, boolean recursively) {
        Field[] allFields = getDeclaredFields(clazz, recursively);
        List<Field> annotatedFields = new LinkedList<Field>();

        for (Field field : allFields) {
            if (field.isAnnotationPresent(annotationClass))
                annotatedFields.add(field);
        }

        return annotatedFields.toArray(new Field[annotatedFields.size()]);
    }

    /**
     * Create object with all fields set to null
     * 
     * @param clazz class of instance
     * @return empty object
     */
    public static <T> T createEmpty(Class<T> clazz) {
        T result = getInstance(clazz);
        for (Field field : getDeclaredFields(clazz, true)) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            field.setAccessible(true);
            try {
                field.set(result, null);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return result;
    }
    
    public static Method getMethod(Class<?> clazz, String name, Class<?> ... params) {
        try {
            return clazz.getMethod(name, params);
        }
        catch (Throwable e) {
            return null;
        }
    }

}