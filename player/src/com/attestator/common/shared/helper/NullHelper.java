package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.attestator.common.shared.vo.BaseVO;

@SuppressWarnings("rawtypes")
public final class NullHelper {

    public static boolean nullSafeIsEmpty( Collection collection) {
        return collection == null ? true : collection.isEmpty();
    }

    public static int nullSafeSize( Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    public static boolean isEmptyOrNull(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmptyOrNull(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmptyOrNull(Map map) {
        return !isEmptyOrNull(map);
    }

    public static <T> boolean isEmptyOrNull(T[] array) {
        return array == null || array.length == 0;
    }
    
    public static <T> boolean isEmptyOrNull(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmptyOrNull(Collection collection) {
        return !isEmptyOrNull(collection);
    }

    public static <T> boolean isNotEmptyOrNull(T[] array) {
        return !isEmptyOrNull(array);
    }

    public static boolean nullSafeEquals(Object arg1, Object arg2) {
        if (arg1 == null) {
            return arg2 == null;
        } else {
            return arg1.equals(arg2);
        }
    }

    public static boolean atLeastOneNull(Object... args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    return true;
                }
            }
        }
        return false;
    }

//    public static String printableNull(Displayable value) {
//        return value != null ? value.getDisplayString() : "";
//    }

    public static String printableNull(String value) {
        return value != null ? value : "";
    }

    public static String nullSafeGetId(BaseVO vo) {
        return vo != null ? vo.getId() : null;
    }

    public static <T> List<T> nonNullsAsList(T... args) {
        List<T> res = null;
        if (args != null) {
            for (T arg : args) {
                if (arg != null) {
                    if (res == null) {
                        res = new ArrayList<T>();
                    }
                    res.add(arg);
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public static void nullSafeAddAll(Collection target, Collection source) {
        if (target != null && !isEmptyOrNull(source)) {
            target.addAll(source);
        }
    }

    public static <K, V> void nullSafeAddAll(Map<K, V> target, Map<K, V> source) {
        if (target != null && !isEmptyOrNull(source)) {
            target.putAll(source);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> nullSafeCollection(Collection<T> collection) {
        return collection == null ? Collections.EMPTY_LIST : collection;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> nullSafeList(List<T> list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public static <T> T emptyToNull(T object) {
        if (object instanceof String && ((String) object).isEmpty()) {
            return null;
        } else if (object instanceof Collection && ((Collection) object).isEmpty()) {
            return null;
        }
        return object;
    }

    public static <K, V> V nullSafeGet(Map<K, V> map, K key) {
        if (map == null || key == null) {
            return null;
        }
        return map.get(key);
    }

    public static <T> T nullSafeGet(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    public static <T> T nullSafeGet(T[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return null;
        }
        return array[index];
    }

    public static <T> List<T> nullToEmptyList(List<T> list) {
        return list != null ? list : new ArrayList<T>();
    }

    public static <K, V> Map<K, V> nullToEmptyMap(Map<K, V> map) {
        return map != null ? map : new HashMap<K, V>();
    }

    public static <T> List<T> nullSafeCloneList(List<T> list) {
        return list == null ? list : new ArrayList<T>(list);
    }

    public static <T> boolean nullSafeContains(Collection<T> where, T what) {
        return where != null && where.contains(what);
    }

    public static <T> void nullSafeRemove(Collection<T> where, T what) {
        if (where != null) {
            where.remove(what);
        }
    }

    public static <T> void nullSafeRemoveAll(Collection<T> where, T what) {
        if (where != null) {
            where.removeAll(Collections.singleton(what));
        }
    }

    public static <T> void nullSafeAdd(List<T> where, T what) {
        if (where != null) {
            where.add(what);
        }
    }

    public static <T> T firstNotNull(T... args) {
        if (args != null) {
            for (T arg : args) {
                if (arg != null) {
                    return arg;
                }
            }
        }
        return null;
    }

    public static <T> boolean allNotNull(T... args) {
        if (args != null) {
            for (T arg : args) {
                if (arg == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean nullSafeTrue(Boolean arg) {
        return Boolean.TRUE.equals(arg);
    }

    public static void nullSafeRun(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

//    public static String nullSafeDisplayString(Displayable displayable) {
//        return displayable != null ? displayable.getDisplayString() : "";
//    }
//
    public static <T> int nullSafeSize(T[] array) {
        return array != null ? array.length : 0;
    }

    public static int nullSafeSize(double[] array) {
        return array != null ? array.length : 0;
    }
    
    public static double nullSafeDoubleOrZerro(Number num) {
        return num != null ? num.doubleValue() : 0;
    }

    public static int nullSafeIntegerOrZerro(Number num) {
        return num != null ? num.intValue() : 0;
    }
    
    public static long nullSafeLongOrZerro(Number num) {
        return num != null ? num.longValue() : 0;
    }
    
    private NullHelper() {
    }
}
