package com.brochure.cms.shared.util;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.security.TenantContext;
import java.util.UUID;

public final class TenantIds {

    private TenantIds() {}

    public static UUID current() {
        Tenant tenant = TenantContext.get();
        if (tenant == null) {
            throw new ValidationException("Tenant could not be resolved for this request");
        }
        return tenant.getId();
    }
}
