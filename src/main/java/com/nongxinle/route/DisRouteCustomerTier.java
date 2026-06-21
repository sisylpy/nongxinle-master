package com.nongxinle.route;

/** Phase 2b-5：客户调度分类 */
public final class DisRouteCustomerTier {
    public static final String VIP = "VIP";
    public static final String NORMAL = "NORMAL";
    public static final String SMALL = "SMALL";
    public static final String NEW = "NEW";

    private DisRouteCustomerTier() {
    }

    public static String normalize(String tier) {
        if (tier == null || tier.trim().isEmpty()) {
            return NORMAL;
        }
        return tier.trim().toUpperCase();
    }
}
