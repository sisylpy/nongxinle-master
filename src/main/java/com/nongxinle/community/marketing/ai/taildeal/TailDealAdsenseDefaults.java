package com.nongxinle.community.marketing.ai.taildeal;

/**
 * AI 尾货广告默认配置。
 * 新建商品统一挂到社区「清库存」分类下。
 */
public final class TailDealAdsenseDefaults {

    private TailDealAdsenseDefaults() {
    }

    /** 清库存 — 尾货 AI 默认 3 级父类 ID */
    public static final int DEFAULT_FATHER_GOODS_ID = 9;

    /** 配送方式：外卖 */
    public static final int DEFAULT_SERVICE_TYPE_TAKEOUT = 1;

    /** 销售时段：分时段 */
    public static final int DEFAULT_SELL_TYPE_PART_TIME = 1;

    /** 上架状态：0=上架 */
    public static final int DEFAULT_PULL_OFF_ON = 0;
}
