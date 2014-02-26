package com.attestator.admin.client.helper;

import com.attestator.admin.client.Admin;
import com.attestator.common.shared.helper.CheckHelper;
import com.attestator.common.shared.vo.TenantableVO;

public class ClientHelper {
    public static boolean isOwnedByOthertUser(TenantableVO tenantable) {
        CheckHelper.throwIfNull(tenantable, "tenantable");
        boolean result = tenantable.getTenantId() != null 
                && !tenantable.getTenantId().equals(Admin.getLoggedUser().getTenantId());
        return result;
    }
}
