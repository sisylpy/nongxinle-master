package com.nongxinle.platform.admin;

import lombok.Getter;

/**
 * 当前请求的市场后台用户上下文（ThreadLocal）。
 */
public final class PlatformMarketAdminContext {

    private static final ThreadLocal<Holder> HOLDER = new ThreadLocal<>();

    private PlatformMarketAdminContext() {
    }

    public static void set(Integer pmuId, Integer marketId, String roleType) {
        HOLDER.set(new Holder(pmuId, marketId, roleType));
    }

    public static Integer getCurrentMarketUserId() {
        Holder h = HOLDER.get();
        return h == null ? null : h.pmuId;
    }

    public static Integer getMarketId() {
        Holder h = HOLDER.get();
        return h == null ? null : h.marketId;
    }

    public static String getRoleType() {
        Holder h = HOLDER.get();
        return h == null ? null : h.roleType;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Getter
    private static final class Holder {
        private final Integer pmuId;
        private final Integer marketId;
        private final String roleType;

        private Holder(Integer pmuId, Integer marketId, String roleType) {
            this.pmuId = pmuId;
            this.marketId = marketId;
            this.roleType = roleType;
        }
    }
}
