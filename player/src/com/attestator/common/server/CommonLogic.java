package com.attestator.common.server;

import org.apache.log4j.Logger;

import com.attestator.admin.server.LoginManager;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.CacheType;
import com.attestator.common.shared.vo.ChangeMarkerVO;
import com.attestator.player.server.Singletons;
import com.google.code.morphia.query.Query;

public class CommonLogic {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CommonLogic.class);
    
    public <T extends BaseVO> T getById(Class<T> clazz, String id) {
        CheckHelper.throwIfNull(clazz, "clazz");
        CheckHelper.throwIfNullOrEmpty(id, "id");
        
        Query<T> q = Singletons.ds().createQuery(clazz);
        q.field("_id").equal(id);
        T result = q.get();
        return result;
    }
    
    protected void putGlobalChangesMarker() {
        putChangesMarker(null, null);
    }

    protected void putChangesMarker(String clientId, CacheType type, String ... entries) {
        ChangeMarkerVO marker = new ChangeMarkerVO(clientId, LoginManager.getThreadLocalTenatId(), type, entries);
        Singletons.ds().save(marker);
    }
}
