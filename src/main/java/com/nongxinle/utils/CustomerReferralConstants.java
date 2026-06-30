package com.nongxinle.utils;

/**
 * C端推广业务编码（展示文案来自数据库配置）
 */
public final class CustomerReferralConstants {

    private CustomerReferralConstants() {
    }

    public static final int HOME_NOTICE_DISPLAY_DAYS = 7;

    public static final String SHARE_ENTRY_MINIPROGRAM = "miniprogram_share";
    public static final String SHARE_PARAM_PROMOTION_CODE = "promotionCode";

    /** 推广主体类型 */
    public static final String OWNER_TYPE_CUSTOMER_USER = "CUSTOMER_USER";
    public static final String OWNER_TYPE_COMMUNITY_USER = "COMMUNITY_USER";
    public static final String OWNER_TYPE_EXTERNAL_PROMOTER = "EXTERNAL_PROMOTER";

    /** 社区工作人员在职状态：有效 */
    public static final int COMMUNITY_USER_WORKING_ACTIVE = 1;

    /** 外部推广员类型 */
    public static final String PROMOTER_TYPE_FULL_TIME = "FULL_TIME";
    public static final String PROMOTER_TYPE_PART_TIME = "PART_TIME";
    public static final String PROMOTER_TYPE_PARTNER = "PARTNER";

    /** 外部推广员状态 */
    public static final String PROMOTER_STATUS_ACTIVE = "ACTIVE";
    public static final String PROMOTER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String PROMOTER_STATUS_TERMINATED = "TERMINATED";

    /** 推广码状态 */
    public static final String CODE_STATUS_ACTIVE = "ACTIVE";
    public static final String CODE_STATUS_SUSPENDED = "SUSPENDED";
    public static final String CODE_STATUS_DISABLED = "DISABLED";

    /** 推广资格 */
    public static final String QUALIFICATION_QUALIFIED = "QUALIFIED";
    public static final String QUALIFICATION_UNQUALIFIED = "UNQUALIFIED";

    /** 无效原因 */
    public static final String INVALID_CODE_NOT_FOUND = "CODE_NOT_FOUND";
    public static final String INVALID_CODE_DISABLED = "CODE_DISABLED";
    public static final String INVALID_CODE_SUSPENDED = "CODE_SUSPENDED";
    public static final String INVALID_CODE_NOT_STARTED = "CODE_NOT_STARTED";
    public static final String INVALID_CODE_EXPIRED = "CODE_EXPIRED";
    public static final String INVALID_OWNER_DISABLED = "OWNER_DISABLED";
    public static final String INVALID_PROMOTER_TERMINATED = "PROMOTER_TERMINATED";
    public static final String INVALID_PROMOTER_SUSPENDED = "PROMOTER_SUSPENDED";
    public static final String INVALID_COMMUNITY_USER_INACTIVE = "COMMUNITY_USER_INACTIVE";
    public static final String INVALID_COMMUNITY_USER_NOT_ELIGIBLE = "COMMUNITY_USER_NOT_ELIGIBLE";
    /** 推广活动状态 */
    public static final String CAMPAIGN_STATUS_ACTIVE = "ACTIVE";
    public static final String CAMPAIGN_STATUS_SUSPENDED = "SUSPENDED";
    public static final String CAMPAIGN_STATUS_TERMINATED = "TERMINATED";

    public static final String INVALID_CAMPAIGN_NOT_ACTIVE = "CAMPAIGN_NOT_ACTIVE";
    public static final String INVALID_CAMPAIGN_AMBIGUOUS = "CAMPAIGN_AMBIGUOUS";
    public static final String INVALID_RULE_AMBIGUOUS = "RULE_AMBIGUOUS";
    public static final String INVALID_RULE_NOT_ACTIVE = "RULE_NOT_ACTIVE";
    public static final String INVALID_CROSS_COMMUNITY = "CROSS_COMMUNITY";
    public static final String INVALID_CROSS_COMMERCE = "CROSS_COMMERCE";
    public static final String INVALID_SELF_REFERRAL = "SELF_REFERRAL";
    public static final String INVALID_ALREADY_REFERRED = "ALREADY_REFERRED";

    /** 奖励受益人类型 */
    public static final String BENEFICIARY_TYPE_CUSTOMER_USER = "CUSTOMER_USER";
    public static final String BENEFICIARY_TYPE_COMMUNITY_USER = "COMMUNITY_USER";
    public static final String BENEFICIARY_TYPE_EXTERNAL_PROMOTER = "EXTERNAL_PROMOTER";

    /** 奖励资产类型 */
    public static final String TRIGGER_REGISTER = "REGISTER";
    /** 推广活动业务场景（注册拉新），注册匹配时由服务端常量注入，非请求参数 */
    public static final String CAMPAIGN_SCENE_REGISTER_ACQUISITION = "REGISTER_ACQUISITION";
    public static final String CAMPAIGN_OWNER_SCOPE_ALL = "ALL";
    public static final String RULE_DIRECT_REGISTER = "direct_register";
    public static final String TARGET_DIRECT_INVITER = "direct_inviter";
    public static final String REWARD_KIND_COUPON = "COUPON";
    public static final String REWARD_KIND_POINTS = "POINTS";
    public static final String REWARD_KIND_COMMISSION = "COMMISSION";
    public static final String REWARD_KIND_PERFORMANCE = "PERFORMANCE";
    public static final String REWARD_KIND_INTERNAL_SETTLEMENT = "INTERNAL_SETTLEMENT";
    public static final String REWARD_KIND_MANUAL_SETTLEMENT = "MANUAL_SETTLEMENT";

    public static final int REWARD_STATUS_PENDING = 0;
    public static final int REWARD_STATUS_CLAIMED = 1;
    public static final int REWARD_STATUS_EXPIRED = 2;
    public static final int REWARD_STATUS_FAILED = 3;

    public static final int REWARD_LEVEL_DIRECT = 1;

    public static final String BIZ_PURPOSE_MARKETING = "marketing";
    public static final String BIZ_PURPOSE_REFERRAL_REGISTER = "referral_register";
    public static final String CLAIM_STRATEGY_PUBLIC_ACTIVE = "public_active";
    public static final String CLAIM_STRATEGY_REWARD_ONLY = "reward_only";

    public static final String VALIDITY_FIXED_DATE = "FIXED_DATE";
    public static final String VALIDITY_DAYS_AFTER_CLAIM = "DAYS_AFTER_CLAIM";

    public static final String SOURCE_ACTIVE_CLAIM = "active_claim";
    public static final String SOURCE_GIFT_TRANSFER = "gift_transfer";
    public static final String SOURCE_REFERRAL_REWARD = "referral_reward";
}
