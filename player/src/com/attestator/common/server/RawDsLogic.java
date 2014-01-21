package com.attestator.common.server;

import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.UserVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

/**
 * 
 * @author Vitaly
 * Only this class is allowed use raw datastore;
 */
public class RawDsLogic {
    private Datastore rawDs;

    public RawDsLogic(Datastore rawDs) {
        super();
        this.rawDs = rawDs;
    }
    
    public UserVO createNewUser(String email, String password) {
        return Singletons.rl().createNewUser(email, password, null); 
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

}
