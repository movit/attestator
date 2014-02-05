package com.attestator.admin.server;

import java.lang.reflect.Field;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.attestator.common.server.helper.ReflectionHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.LockVO;
import com.attestator.player.server.Singletons;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class LockManager {
    private static Logger logger = Logger.getLogger(LockManager.class);  
    
    private static String createId(LockVO lock) {
        StringBuilder sb = new StringBuilder();
        sb.append("class: ");
        sb.append(lock.getClass().getName());
        sb.append(", ");
        Field[] fields = ReflectionHelper.getDeclaredFields(lock.getClass(), LockVO.class);
        for (Field field: fields) {
            field.setAccessible(true);
            sb.append(field.getName());
            sb.append(": ");
            try {
                sb.append(String.valueOf(field.get(lock)));
            } catch (Throwable e) {                
                logger.warn("Unable to access " + field.getName() + " in " + lock.getClass().getName() + " while generating id for lock");
            }
            sb.append(", ");
        }        
        String result = DigestUtils.md5Hex(sb.toString());
        return result;
    }
    
    private static DBObject createDBObject(LockVO lock) {
        String id = createId(lock);
        lock.setId(id);
        DBObject result = Singletons.morphia().getMapper().toDBObject(lock);
        return result;
    }
    
    private static boolean isLockPresent(DBObject lockObject) {
        DBCollection collection = Singletons.db().getCollection("lock");
        boolean result = collection.count(lockObject) == 0;
        return result;
    }
    
    private static boolean obtainLock(DBObject lockObject) {
        DBCollection collection = Singletons.db().getCollection("lock");        
        boolean result = false;
        try {
            WriteResult insertResult = collection.insert(lockObject);
            result = insertResult.getError() == null;
        }
        catch (Throwable e) {
        }        
        return result;
    }
    
    /**
     * If lock can't be obtained instantly wait until 
     * lock is released and obtain this lock
     */
    public static void lockBlocking(LockVO lock) {
        CheckHelper.throwIfNull(lock, "lock");
        
        DBObject lockObject = createDBObject(lock);
        long sleepTime = 250;
        while (true) {
            if (obtainLock(lockObject)) {
                break;
            }
            
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {                
            }
            
            if (sleepTime < 4000) {
                sleepTime = sleepTime * 2;
            }
        }
    }
    
    /**
     * Try to obtain lock instantly and return true
     * if this is possible, otherwise return false
     */
    public static boolean lock(LockVO lock) {
        CheckHelper.throwIfNull(lock, "lock");
        
        DBObject lockObject = createDBObject(lock);
        return obtainLock(lockObject);
    }
    
    /**
     * Release lock if them was obtained before
     */
    public static void releaseLock(LockVO lock) {
        CheckHelper.throwIfNull(lock, "lock");
        
        String lockCollectionName = Singletons.morphia().getMapper().getCollectionName(lock);
        DBCollection collection = Singletons.db().getCollection(lockCollectionName);
        DBObject lockObject = createDBObject(lock);
        collection.remove(lockObject);
    }
    
    public static void blockUntilLockReleased(LockVO lock) {
        CheckHelper.throwIfNull(lock, "lock");
        
        DBObject lockObject = createDBObject(lock);
        long sleepTime = 250;
        while (true) {
            if (!isLockPresent(lockObject)) {
                break;
            }
            
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {                
            }
            
            if (sleepTime < 4000) {
                sleepTime = sleepTime * 2;
            }
        }
    }
}
