package com.nongxinle.utils;


/**
 * 商品类型
 */
public class NxDistributerTypeUtils {


    public final static Integer NX_DIS_USER_ADMIN = 0; //管理员
    public final static Integer NX_DIS_USER_PURCHASE = 1; //采购员
    public final static Integer NX_DIS_USER_WEIGHTER = 3; //称重员
    public final static Integer NX_DIS_USER_DRIVER = 5; //司机

    public final static Integer NX_DEP_ORDER_UN_PURCHASE = 0; //订单未采购
    public final static Integer NX_DEP_ORDER_WITH_PURCHASE = 1; //订单进入备货状态
    public final static Integer NX_DEP_ORDER_IS_PURCHASE = 2; //订单买了
    public final static Integer NX_DEP_ORDER_FINISH_PURCHASE = 3; //订单完成采购
    public final static Integer NX_DEP_ORDER_FINISH_OUT = 4; //订单完成分拣

    public final static Integer NX_ORDER_STATUS_NEW = 0;  //新订单
    public final static Integer NX_ORDER_STATUS_PROCUREMENT = 1;  //有重量
    public final static Integer NX_ORDER_STATUS_HAS_FINISHED = 2;  //
    public final static Integer NX_ORDER_STATUS_HAS_BILL = 3;  // 生成送货单
    public final static Integer NX_ORDER_STATUS_RECEIVED = 4;  // 完成收货

    public final static Integer NX_DIS_GOODS_STOCK = -1; //出库
    public final static Integer NX_DIS_GOODS_ORDER = 0; // 自采
    public final static Integer NX_DIS_GOODS_SUPPLIER = 1; //订货
    public final static Integer NX_DIS_GOODS_PURCHASE = 2; //指定供货商

    public final static Integer NX_DIS_PURCHASE_GOODS_UN_BUY = 0; //未进货
    public final static Integer NX_DIS_PURCHASE_GOODS_WITH_BATCH = 1; // 订货中
    public final static Integer NX_DIS_PURCHASE_GOODS_IS_PURCHASE = 2; //买了
    public final static Integer NX_DIS_PURCHASE_GOODS_FINISH_BUY = 3; //进货完成
    public final static Integer NX_DIS_PURCHASE_GOODS_FINISH_PAY = 4; //进货完成

    public final static Integer NX_DIS_PURCHASE_BATCH_UN_Send = -2; //卖方未读
    public final static Integer NX_DIS_PURCHASE_BATCH_UN_READ = -1; //卖方未读
    public final static Integer NX_DIS_PURCHASE_BATCH_HAVE_READ = 0; //卖方已读
    public final static Integer NX_DIS_PURCHASE_BATCH_SELLER_REPLY = 1; //卖方回复
    public final static Integer NX_DIS_PURCHASE_BATCH_DIS_USER_FINISH = 2; //Dis用户完成
    public final static Integer NX_DIS_PURCHASE_BATCH_DIS_USER_FINISH_PAY = 3; //Dis用户完成




    public static Integer getNxDisUserAdmin() {
        return NX_DIS_USER_ADMIN;
    }
    public static Integer getNxDisUserPurchase() {
        return NX_DIS_USER_PURCHASE;
    }
    public static Integer getNxDisUserDriver() {
        return NX_DIS_USER_DRIVER;
    }
    public static Integer getNxDisUserWeighter() {
        return NX_DIS_USER_WEIGHTER;
    }

    public static Integer getNxDepOrderBuyStatusUnPurchase() {
        return NX_DEP_ORDER_UN_PURCHASE;
    }
    public static Integer getNxDepOrderBuyStatusWithPurchase() {
        return NX_DEP_ORDER_WITH_PURCHASE;
    }
    public static Integer getNxDepOrderBuyStatusIsPurchase() {
        return NX_DEP_ORDER_IS_PURCHASE;
    }
    public static Integer getNxDepOrderBuyStatusFinishPurchase() {
        return NX_DEP_ORDER_FINISH_PURCHASE;
    }
    public static Integer getNxDepOrderBuyStatusFinishOut() {
        return NX_DEP_ORDER_FINISH_OUT;
    }

    public static Integer getNxOrderStatusNew() {
        return NX_ORDER_STATUS_NEW;
    }
    public static Integer getNxOrderStatusProcurement() {
        return NX_ORDER_STATUS_PROCUREMENT;
    }
    public static Integer getNxOrderStatusHasFinished() {
        return NX_ORDER_STATUS_HAS_FINISHED;
    }
    public static Integer getNxOrderStatusHasBill() {
        return NX_ORDER_STATUS_HAS_BILL;
    }

    public static Integer getNxDisPurchaseGoodsUnBuy() {
        return NX_DIS_PURCHASE_GOODS_UN_BUY;
    }
    public static Integer getNxDisPurchaseGoodsWithBatch() {
        return NX_DIS_PURCHASE_GOODS_WITH_BATCH;
    }
    public static Integer getNxDisPurchaseGoodsIsPurchase() {
        return NX_DIS_PURCHASE_GOODS_IS_PURCHASE;
    }
    public static Integer getNxDisPurchaseGoodsFinishBuy() {
        return NX_DIS_PURCHASE_GOODS_FINISH_BUY;
    }
    public static Integer getNxDisPurchaseGoodsFinishPay() {
        return NX_DIS_PURCHASE_GOODS_FINISH_PAY;
    }


    public static Integer getNxDisPurchaseBatchUnSend() {
        return NX_DIS_PURCHASE_BATCH_UN_Send;
    }
    public static Integer getNxDisPurchaseBatchUnRead() {
        return NX_DIS_PURCHASE_BATCH_UN_READ;
    }
    public static Integer getNxDisPurchaseBatchHaveRead() {
        return NX_DIS_PURCHASE_BATCH_HAVE_READ;
    }
    public static Integer getNxDisPurchaseBatchSellerReply() {
        return NX_DIS_PURCHASE_BATCH_SELLER_REPLY;
    }
    public static Integer getNxDisPurchaseBatchDisUserFinish() {
        return NX_DIS_PURCHASE_BATCH_DIS_USER_FINISH;
    }
    public static Integer getNxDisPurchaseBatchDisUserFinishPay() {
        return NX_DIS_PURCHASE_BATCH_DIS_USER_FINISH_PAY;
    }
}
