package com.nongxinle.utils;


/**
 * 商品类型
 */
public class NxCommunityTypeUtils {


    public final static Integer Nx_COMMUNITY_GOODS_TYPE_RICAI = 1;
    public final static Integer Nx_COMMUNITY_GOODS_TYPE_CHUKU = 2;
    public final static Integer Nx_COMMUNITY_GOODS_TYPE_SUPPLIER = 3;
    public final static Integer Nx_COMMUNITY_GOODS_TYPE_NXDISTRIBUTER = 4;


    public final static Integer NX_RESTRAUNT_ORDER_STATUS_NEW = 0;  //新订单
    public final static Integer NX_RESTRAUNT_ORDER_STATUS_PROCUREMENT = 1;  //有重量
    public final static Integer NX_RESTRAUNT_ORDER_STATUS_HAS_FINISHED = 2;  //
    public final static Integer NX_RESTRAUNT_ORDER_STATUS_HAS_BILL = 3;  // 生成送货单
    public final static Integer NX_RESTRAUNT_ORDER_STATUS_RECEIVED = 4;  // 完成收货

    public final static Integer NX_RESTRAUNT_ORDER_BUY_STATUS_NEW = 0;  //新订单
    public final static Integer NX_RESTRAUNT_ORDER_BUY_STATUS_PROCUREMENT = 1;  //采购员分享给供货商订单（生成一个采购批次Batch）
    public final static Integer NX_RESTRAUNT_ORDER_BUY_STATUS_HAS_PRINTED = 2;  //打印拣货单
    public final static Integer NX_RESTRAUNT_ORDER_BUY_STATUS_HAS_WEIGHT_AND_PRICE = 3;  //

    public static Integer getNxRestrauntOrderStatusNew(){ return NX_RESTRAUNT_ORDER_STATUS_NEW; }
    public static Integer getNxRestrauntOrderStatusProcurement(){ return NX_RESTRAUNT_ORDER_STATUS_PROCUREMENT; }
    public static Integer getNxRestrauntOrderStatusHasFinished(){ return NX_RESTRAUNT_ORDER_STATUS_HAS_FINISHED; }
    public static Integer getNxRestrauntOrderStatusHasBill(){ return NX_RESTRAUNT_ORDER_STATUS_HAS_BILL; }
    public static Integer getNxRestrauntOrderStatusReceived(){ return NX_RESTRAUNT_ORDER_STATUS_RECEIVED; }


    public static Integer getNxRestrauntOrderBuyStatusNew(){ return NX_RESTRAUNT_ORDER_BUY_STATUS_NEW; }
    public static Integer getNxRestrauntOrderBuyStatusProcurement(){ return NX_RESTRAUNT_ORDER_BUY_STATUS_PROCUREMENT; }
    public static Integer getNxRestrauntOrderBuyStatusHasPrinted(){ return NX_RESTRAUNT_ORDER_BUY_STATUS_HAS_PRINTED; }
    public static Integer getNxRestrauntOrderBuyStatusHasWeightAndPrice(){ return NX_RESTRAUNT_ORDER_BUY_STATUS_HAS_WEIGHT_AND_PRICE; }


    public static Integer getNXCOMMUNITYGOODSTYPERICAI() {
        return Nx_COMMUNITY_GOODS_TYPE_RICAI;
    }

    public static Integer getNXCOMMUNITYGOODSTYPECHUKU() {
        return Nx_COMMUNITY_GOODS_TYPE_CHUKU;
    }

    public static Integer getNXCOMMUNITYGOODSTYPESUPPLIER() {
        return Nx_COMMUNITY_GOODS_TYPE_SUPPLIER;
    }

    public static Integer getNXCOMMUNITYGOODSTYPENXDISTRIBUTER() {
        return Nx_COMMUNITY_GOODS_TYPE_NXDISTRIBUTER;
    }
}
