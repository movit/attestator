package com.attestator.common.server;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.attestator.common.server.helper.DatastoreHelper;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.CronTaskVO;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

/**
 * 
 * @author Vitaly
 * Only this class is allowed use raw datastore;
 */
public class SystemLogic {
    private Datastore rawDs;

    public SystemLogic(Datastore rawDs) {
        super();
        this.rawDs = rawDs;
    }
    
    public UserVO createNewUser(String email, String password) {
        return Singletons.sl().createNewUser(email, password, null); 
    }    

    public UserVO createNewUser(String email, String password, String tenantId) {
        CheckHelper.throwIfNullOrEmpty(email, "email");
        CheckHelper.throwIfNullOrEmpty(password, "password");
        
        UserVO result = new UserVO();
        result.setEmail(email);
        result.setPassword(password);
        
        if (tenantId != null) {
            result.setId(tenantId);
            result.setTenantId(tenantId);
            result.setDefaultGroupId(tenantId);
        }
        
        rawDs.save(result);
        
        GroupVO defaultGroup = new GroupVO();
        defaultGroup.setId(result.getDefaultGroupId());
        defaultGroup.setTenantId(result.getTenantId());
        defaultGroup.setName(GroupVO.DEFAULT_GROUP_INITIAL_NAME);
        
        rawDs.save(defaultGroup);
        
        return result;
    }
    
    public UserVO getUserByLoginPassword(String email, String password) {
        CheckHelper.throwIfNullOrEmpty(email, "email");
        CheckHelper.throwIfNullOrEmpty(password, "password");
        
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        q.field("email").equal(email);
        q.field("password").equal(password);
        UserVO user = q.get();
        
        return user;
    }
    
    public UserVO getUserByTenantId(String tenantId) {
        CheckHelper.throwIfNullOrEmpty(tenantId, "tenantId");
        
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        q.field("tenantId").equal(tenantId);
        UserVO user = q.get();        
        
        return user;
    }
    
    public void deletePublicationsForMetatest(String metetestId) {
        CheckHelper.throwIfNullOrEmpty(metetestId, "metetestId");
        
        Query<PublicationVO> q = rawDs.createQuery(PublicationVO.class);
        q.field("metatestId").equal(metetestId);
        
        rawDs.delete(q);
    }

    public void deletePritingPropertiesForMetatest(String metetestId) {
        CheckHelper.throwIfNullOrEmpty(metetestId, "metetestId");
        
        Query<PrintingPropertiesVO> q = rawDs.createQuery(PrintingPropertiesVO.class);
        q.field("metatestId").equal(metetestId);
        
        rawDs.delete(q);
    }

    public PagingLoadResult<UserVO> loadUserPage(FilterPagingLoadConfig loadConfig, String ... excludFields) {
        CheckHelper.throwIfNull(loadConfig, "loadConfig");
        
        // Create query
        Query<UserVO> q = rawDs.createQuery(UserVO.class);
        
        HashSet<String> usedExcludeFields = new HashSet<String>();        
        // Exclude fields if any
        if (excludFields != null) {
            usedExcludeFields.addAll(Arrays.asList(excludFields));
        }
        // Don't load password. Never!
        usedExcludeFields.add("password");
        
        q.retrievedFields(false, usedExcludeFields.toArray(new String[0]));
        
        // Add load config
        DatastoreHelper.addLoadConfig(q, loadConfig);

        // Get total count (without offset and limit)
        long count = q.countAll();        
                
        q.offset(loadConfig.getOffset());
        q.limit(loadConfig.getLimit());
        
        List<UserVO> qRes = q.asList();
        
        PagingLoadResultBean<UserVO> result = new PagingLoadResultBean<UserVO>();
        result.setData(qRes);
        result.setOffset(loadConfig.getOffset());
        result.setTotalLength((int)count);
        
        return result;
    }
    
    public void scheduleCronTask(CronTaskVO cronTask) {
        rawDs.save(cronTask);
    }
    
    public List<CronTaskVO> getActiveCronTasks() {
        Query<CronTaskVO> q = rawDs.createQuery(CronTaskVO.class);
        q.field("time").lessThanOrEq(new Date());
        q.order("time");
        List<CronTaskVO> result = q.asList();
        return result;
    }
    
    public void removeCronTask(String cronTaskId) {
        CheckHelper.throwIfNullOrEmpty(cronTaskId, "cronTaskId");
        
        Query<CronTaskVO> q = rawDs.createQuery(CronTaskVO.class);
        q.field("_id").equal(cronTaskId);
        rawDs.delete(q);
    }
}
