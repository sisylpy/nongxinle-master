package com.nongxinle.utils;


/**
 * 商品类型
 */
public class NxCommunityTypeUtils {


    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_ALL_TIME = 1; //全时段
    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_PART_TIME = 2; //部分时段
    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_COUPON = 3; //优惠卷商品

    public final static Integer NX_COMMUNITY_ORDER_STATUS_PINDAN_NEW = -1;  //新拼单
    public final static Integer NX_COMMUNITY_ORDER_STATUS_NEW = 0;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_STATUS_TO_PAY = 1;  //提交微信支付
    public final static Integer NX_COMMUNITY_ORDER_STATUS_PAYED = 2;  //微信支付成功
    public final static Integer NX_COMMUNITY_ORDER_STATUS_LABELS_PRINT = 3;  // 标签打印完成已打印
    public final static Integer NX_COMMUNITY_ORDER_STATUS_BILL_PRINT = 4;  // 总单已打印
    public final static Integer NX_COMMUNITY_ORDER_STATUS_ORDER_FINISH = 5;  //顾客完成
    public final static Integer NX_COMMUNITY_ORDER_STATUS_ORDER_CANCEL = 99;  //取消订单

    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_SAVE = -1;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_ORDER_SAVE = 0;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_PAYED = 1; //微信支付成功
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_ANDROID_PRINTED = 2;  //打印Label
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_BILL_PRINT = 3;// 总单已打印
    public final static Integer NX_COMMUNITY_ORDER_sUB_STATUS_ORDER_FINISH = 4;// 顾客完成


    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_NEW = 0;  //好友打开链接，生成新拼单
    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_ORDERING = 1;  //好友正在下单
    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_ORDER_FINISH = 2;  //好友下单完成
    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_ORDER_SAVE = 3;  //保存拼订
    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_ORDER_TO_PAY = 4;  //提交微信支付
    public final static Integer NX_COMMUNITY_SPLICING_ORDER_STATUS_ORDER_PAYED = 5;  //微信支付成功

    public final static Integer NX_COMMUNITY_ORDER_SUB_TYPE_SINGLE = 0;  //自提
    public final static Integer NX_COMMUNITY_ORDER_SUB_TYPE_PINDAN = 1;  //拼单自提

    public final static Integer NX_COMMUNITY_ORDER_PAYMENT_STATUS_TO_PAY = 0;  ///提交微信支付
    public final static Integer NX_COMMUNITY_ORDER_PAYMENT_STATUS_PAYED = 1;  ///微信支付成功

    public final static Integer NX_COMMUNITY_GOODS_TYPE_COMMON = 0;  //普通商品
    public final static Integer NX_COMMUNITY_GOODS_TYPE_DUOPIN = 1;  //多拼
    public final static Integer NX_COMMUNITY_GOODS_TYPE_TAOCAN = 2;  //套餐
    public final static Integer NX_COMMUNITY_GOODS_TYPE_ZIYOUPIN = 3;  //自由拼


    public final static Integer NX_COMMUNITY_GOODS_PULL_OFF_ON = 0;  //上架
    public final static Integer NX_COMMUNITY_GOODS_PULL_OFF_YES = 1;  //下架

    public final static Integer NX_COMMUNITY_GOODS_PROMOTION_TYPE_NONE = 0;  //无活动
    public final static Integer NX_COMMUNITY_GOODS_PROMOTION_TYPE_HUAXIAN = 1;  //划线价格
    public final static Integer NX_COMMUNITY_GOODS_PROMOTION_TYPE_MANJIAN = 2;  //满减
    public final static Integer NX_COMMUNITY_GOODS_PROMOTION_TYPE_PINDANMANJIAN = 3;  //拼单满减


    public static Integer getNxCommunityGoodsSellTypeAllTime() {
        return Nx_COMMUNITY_GOODS_SELL_TYPE_ALL_TIME;
    }
    public static Integer getNxCommunityGoodsSellTypePratTime() {
        return Nx_COMMUNITY_GOODS_SELL_TYPE_PART_TIME;
    }

    public static Integer getNxCommunityGoodsPromotionNone() {
        return NX_COMMUNITY_GOODS_PROMOTION_TYPE_NONE;
    }
    public static Integer getNxCommunityGoodsPromotionTypeHuaxian() {
        return NX_COMMUNITY_GOODS_PROMOTION_TYPE_HUAXIAN;
    }
    public static Integer getNxCommunityGoodsPromotionTypeManjian() {
        return NX_COMMUNITY_GOODS_PROMOTION_TYPE_MANJIAN;
    }
    public static Integer getNxCommunityGoodsPromotionTypePindanManjian() {
        return NX_COMMUNITY_GOODS_PROMOTION_TYPE_PINDANMANJIAN;
    }

    public static Integer getNxCommunityGoodsTypeCommon() {
        return NX_COMMUNITY_GOODS_TYPE_COMMON;
    }
    public static Integer getNxCommunityGoodsTypeDuopin() {
        return NX_COMMUNITY_GOODS_TYPE_DUOPIN;
    }
    public static Integer getNxCommunityGoodsTypeTaocan() {
        return NX_COMMUNITY_GOODS_TYPE_TAOCAN;
    }
    public static Integer getNxCommunityGoodsTypeZiyoupin() {
        return NX_COMMUNITY_GOODS_TYPE_ZIYOUPIN;
    }




}
