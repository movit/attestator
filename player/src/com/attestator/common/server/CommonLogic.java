package com.attestator.common.server;

import org.apache.log4j.Logger;

import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.BaseVO;
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
}
