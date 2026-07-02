package com.brochure.cms.shared.security;

import com.brochure.cms.domain.tenant.Tenant;

public final class TenantContext {

    private static final ThreadLocal<Tenant> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Tenant tenant) {
        CURRENT.set(tenant);
    }

    public static Tenant get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
