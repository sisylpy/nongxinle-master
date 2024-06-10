package com.nongxinle.utils;


import com.fasterxml.jackson.annotation.JsonIgnoreType;

import java.lang.annotation.Retention;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 商品类型
 * 
 */
public class GbTypeUtils {

    /** GreatBegin  */

//    NxPrinterUserAdmin: 1 txs; 2 gbStock; 3 gbPurchase; 4  

    public final static Integer GB_PURCHASE_BATCH_PAY_TYPE_CASH = 0;
    public final static Integer GB_PURCHASE_BATCH_PAY_TYPE_ACCOUNT = 1;

    public final static Integer GB_DEPARTMENT_TYPE_MENDIAN = 1;  //门店部门
    public final static Integer GB_DEPARTMENT_TYPE_JICAI = 2; //集采部门
    public final static Integer GB_DEPARTMENT_TYPE_KUFNG = 3; //库房部门
    public final static Integer GB_DEPARTMENT_TYPE_KITCHEN = 4; //中央厨房部门
    public final static Integer GB_DEPARTMENT_TYPE_APP_SUPPLIER = 5; //配送商部门
    public final static Integer GB_DEPARTMENT_TYPE_JIAMENG = 11;  //加盟店部门

    public final static Integer GB_DIS_GOODS_TYPE_ZICAI = 1; //自采商品
    public final static Integer GB_DIS_GOODS_TYPE_JICAI = 2; //集采商品
    public final static Integer GB_DIS_GOODS_TYPE_CHUKU = 3; //出库商品
    public final static Integer GB_DIS_GOODS_TYPE_KITCHEN = 4; //中央厨房商品
    public final static Integer GB_DIS_GOODS_TYPE_APP_SUPPLIER = 5; //配送商商品
    public final static Integer GB_DIS_GOODS_TYPE_WINDOW = 23; //窗口商品

    public final static Integer GB_DIS_WEIGHT_TOTAL_TYPE_STOCK = 3; //窗口商品
    public final static Integer GB_DIS_WEIGHT_TOTAL_TYPE_WINDOW = 23; //窗口商品


    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_MONTH = 3;
    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_WEEK = 2;
    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_DAILY = 1;

    public final static Integer GB_ORDER_TYPE_ZICAI = 1;  //自采订单
    public final static Integer GB_ORDER_TYPE_JICAI = 2; // 集采订单
    public final static Integer GB_ORDER_TYPE_CHUKU = 3;  //出库订单
    public final static Integer GB_ORDER_TYPE_CHUKU_CAIGOU = 31; // 库房采购订单  中央厨房采购订单
    public final static Integer GB_ORDER_TYPE_KITCHEN = 4; // 中央厨房订单
    public final static Integer GB_ORDER_TYPE_APP_SUPPLIER = 5; // 供货商订单
    public final static Integer GB_ORDER_TYPE_TUIHUO = 9;  // 退货订单
//    public final static Integer GB_ORDER_TYPE_WINDOW = 23;  //窗口订单

    public final static Integer GB_DEP_USER_ADMIN_MENDIANDCAIGOU = 1; //门店采购端
    public final static Integer GB_DEP_USER_ADMIN_MENDIAN = 11;  //门店管理端
    public final static Integer GB_DEP_USER_ADMIN_MENDIANDINGHUO = 12; //门店订货端
    public final static Integer GB_DEP_USER_ADMIN_WINDOWDINGHUO = 13; //订货端
    public final static Integer GB_DEP_USER_ADMIN_JICAI = 2; //采购端
    public final static Integer GB_DEP_USER_ADMIN_KUFANG = 3; //库房端
    public final static Integer GB_DEP_USER_ADMIN_KUFANGCAIGOUYUAN = 31; //库房采购员
    public final static Integer GB_DEP_USER_ADMIN_KITCHEN = 4; // 中央厨房端
    public final static Integer GB_DEP_USER_ADMIN_APP_SUPPLIER = 5; // 中央厨房端
    public final static Integer GB_DEP_USER_ADMIN_PEISONGYUAN = 6; // 配送员

    //1,cost;2waste;3loass;4return
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_PRODUCE = 1;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_WASTE = 2;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_LOSS = 3;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_RETURN = 4;


    public final static Integer GB_ORDER_STATUS_NEW = 0;  //新订单
    public final static Integer GB_ORDER_STATUS_PROCUREMENT = 1;  //有重量
    public final static Integer GB_ORDER_STATUS_HAS_FINISHED = 2;  //
    public final static Integer GB_ORDER_STATUS_HAS_BILL = 3;  // 生成送货单
    public final static Integer GB_ORDER_STATUS_RECEIVED = 4;  // 完成收货


    public final static Integer GB_ORDER_BUY_STATUS_NEW = 0;  //新订单
    public final static Integer GB_ORDER_BUY_STATUS_PROCUREMENT = 1;  //采购员分享给供货商订单（生成一个采购批次Batch）
    public final static Integer GB_ORDER_BUY_STATUS_HAS_PRINTED = 2;  //打印拣货单
    public final static Integer GB_ORDER_BUY_STATUS_HAS_WEIGHT_AND_PRICE = 3;  //
    public final static Integer GB_ORDER_BUY_STATUS_HAS_FINISH_PUR_GOODS = 4;  //


    public final static Integer GB_PURCHASE_GOODS_STATUS_NEW = 0;  //新采购商品
    public final static Integer GB_PURCHASE_GOODS_STATUS_PROCUREMENT = 1;  //采购员分享给供货商订单（生成一个采购批次Batch）
    public final static Integer GB_PURCHASE_GOODS_STATUS_FINISHED = 2;  //采购员分享给供货商订单（生成一个采购批次Batch）
    public final static Integer GB_PURCHASE_GOODS_STATUS_RECEIVE = 3;  //采购员分享给供货商订单（生成一个采购批次Batch）


    public final static Integer GB_DIS_PURCHASE_BATCH_UN_Send = -2; //卖方未读
    public final static Integer GB_DIS_PURCHASE_BATCH_UN_READ = -1; //卖方未读
    public final static Integer GB_DIS_PURCHASE_BATCH_HAVE_READ = 0; //卖方已读
    public final static Integer GB_DIS_PURCHASE_BATCH_SELLER_REPLY = 1; //卖方回复
    public final static Integer GB_DIS_PURCHASE_BATCH_DIS_USER_FINISH = 2; //Dis用户完成
    public final static Integer GB_DIS_PURCHASE_BATCH_DIS_USER_FINISH_PAY = 3; //Dis用户完成
    
    
    public final static Integer GB_WEIGHT_GOODS_STATUS_PREPARE = -1;  //
    public final static Integer GB_WEIGHT_GOODS_STATUS_PRINTED = 0;  //

    public final static Integer GB_WEIGHT_TOTAL_STATUS_UN_FINISHED = 0;  //
    public final static Integer GB_WEIGHT_TOTAL_STATUS_FINISHED = 1;  //




    public static Integer getDISGoodsInventroyMonth(){
        return GB_DIS_GOODS_INVENTORY_TYPE_MONTH;
    }
    public static Integer getDISGoodsInventroyWeek(){ return GB_DIS_GOODS_INVENTORY_TYPE_WEEK; }
    public static Integer getDISGoodsInventroyDaily(){ return GB_DIS_GOODS_INVENTORY_TYPE_DAILY; }

//    public static Integer getGbDepartGoodsStockReduceTypeCost(){
//        return GB_DEPART_GOODS_STOCK_REDUCE_TYPE_COST;
//    }
    public static Integer getGbDepartGoodsStockReduceTypeProduce(){
        return GB_DEPART_GOODS_STOCK_REDUCE_TYPE_PRODUCE;
    }
    public static Integer getGbDepartGoodsStockReduceTypeWaste(){
        return GB_DEPART_GOODS_STOCK_REDUCE_TYPE_WASTE;
    }
    public static Integer getGbDepartGoodsStockReduceTypeLoss(){
        return GB_DEPART_GOODS_STOCK_REDUCE_TYPE_LOSS;
    }
    public static Integer getGbDepartGoodsStockReduceTypeReturn(){
        return GB_DEPART_GOODS_STOCK_REDUCE_TYPE_RETURN;
    }


    public static Integer getGbDepartmentTypeJicai(){
        return GB_DEPARTMENT_TYPE_JICAI;
    }
    public static Integer getGbDepartmentTypeKufang(){
        return GB_DEPARTMENT_TYPE_KUFNG;
    }
    public static Integer getGbDepartmentTypeMendian(){ return GB_DEPARTMENT_TYPE_MENDIAN; }
    public static Integer getGbDepartmentTypeJiameng(){
        return GB_DEPARTMENT_TYPE_JIAMENG;
    }
    public static Integer getGbDepartmentTypeKitchen(){
        return GB_DEPARTMENT_TYPE_KITCHEN;
    }
    public static Integer getGbDepartmentTypeAppSupplier(){
        return GB_DEPARTMENT_TYPE_APP_SUPPLIER;
    }


    public static Integer getGbDisGoodsTypeJicai(){ return GB_DIS_GOODS_TYPE_JICAI; }
    public static Integer getGbDisGoodsTypeChuku(){ return GB_DIS_GOODS_TYPE_CHUKU; }
    public static Integer getGbDisGoodsTypeZicai(){ return GB_DIS_GOODS_TYPE_ZICAI; }
    public static Integer getGbDisGoodsTypeKitchen(){ return GB_DIS_GOODS_TYPE_KITCHEN; }
    public static Integer getGbDisGoodsTypeAppSupplier(){ return GB_DIS_GOODS_TYPE_APP_SUPPLIER; }
    public static Integer getGbDisGoodsTypeWindow(){ return GB_DIS_GOODS_TYPE_WINDOW; }

//    public static Integer getGbDisWeightTotalTypeStock(){ return GB_DIS_WEIGHT_TOTAL_TYPE_STOCK; }
//    public static Integer getGbDisWeightTotalTypeWindow(){ return GB_DIS_WEIGHT_TOTAL_TYPE_WINDOW; }

    public static  Integer getGbOrderTypeJiCai() {
        return GB_ORDER_TYPE_JICAI;
    }
    public static  Integer getGbOrderTypeChuKu() { return GB_ORDER_TYPE_CHUKU; }
    public static  Integer getGbOrderTypeChuKuCaiGou() {
        return GB_ORDER_TYPE_CHUKU_CAIGOU;
    }
    public static  Integer getGbOrderTypeZiCai() {
        return GB_ORDER_TYPE_ZICAI;
    }
    public static  Integer getGbOrderTypeKitchen() {
        return GB_ORDER_TYPE_KITCHEN;
    }
    public static  Integer getGbOrderTypeAppSupplier() {
        return GB_ORDER_TYPE_APP_SUPPLIER;
    }

    public static  Integer getGbOrderTypeTuihuo() {
        return GB_ORDER_TYPE_TUIHUO;
    }
//    public static  Integer getGbOrderTypeWindow() {
//        return GB_ORDER_TYPE_WINDOW;
//    }



    public static Integer getGbDepUserAdminMendianjingli(){ return GB_DEP_USER_ADMIN_MENDIAN; }
    public static Integer getGbDepUserAdminMendiandinghuoyuan(){ return GB_DEP_USER_ADMIN_MENDIANDINGHUO; }
    public static Integer getGbDepUserAdminWindowdinghuo(){ return GB_DEP_USER_ADMIN_WINDOWDINGHUO; }
    public static Integer getGbDepUserAdminMendiancaigouyuan(){ return GB_DEP_USER_ADMIN_MENDIANDCAIGOU; }
    public static Integer getGbDepUserAdminJicaiyuan(){ return GB_DEP_USER_ADMIN_JICAI; }
    public static Integer getGbDepUserAdminKufangguanliyuan(){ return GB_DEP_USER_ADMIN_KUFANG; }
    public static Integer getGbDepUserAdminKitchenguanliyuan(){ return GB_DEP_USER_ADMIN_KITCHEN; }
    public static Integer getGbDepUserAdminKufangcaigouyuan(){ return GB_DEP_USER_ADMIN_KUFANGCAIGOUYUAN; }
    public static Integer getGbDepUserAdminAppSupplier(){ return GB_DEP_USER_ADMIN_APP_SUPPLIER; }
    public static Integer getGbDepUserAdminPeisongyuan(){ return GB_DEP_USER_ADMIN_PEISONGYUAN; }


    public static Integer getGbOrderStatusNew(){ return GB_ORDER_STATUS_NEW; }
    public static Integer getGbOrderStatusProcurement(){ return GB_ORDER_STATUS_PROCUREMENT; }
    public static Integer getGbOrderStatusHasFinished(){ return GB_ORDER_STATUS_HAS_FINISHED; }
    public static Integer getGbOrderStatusHasBill(){ return GB_ORDER_STATUS_HAS_BILL; }
    public static Integer getGbOrderStatusReceived(){ return GB_ORDER_STATUS_RECEIVED; }

    public static Integer getGbOrderBuyStatusNew(){ return GB_ORDER_BUY_STATUS_NEW; }
    public static Integer getGbOrderBuyStatusProcurement(){ return GB_ORDER_BUY_STATUS_PROCUREMENT; }
    public static Integer getGbOrderBuyStatusPrepareing(){ return GB_ORDER_BUY_STATUS_HAS_PRINTED; }
    public static Integer getGbOrderBuyStatusHasWeightAndPrice(){ return GB_ORDER_BUY_STATUS_HAS_WEIGHT_AND_PRICE; }
    public static Integer getGbOrderBuyStatusHasFinishPurGoods(){ return GB_ORDER_BUY_STATUS_HAS_FINISH_PUR_GOODS; }

    public static Integer getGbPurchaseGoodsStatusNew(){ return GB_PURCHASE_GOODS_STATUS_NEW; }
    public static Integer getGbPurchaseGoodsStatusProcurement(){ return GB_PURCHASE_GOODS_STATUS_PROCUREMENT; }
    public static Integer getGbPurchaseGoodsStatusFinished(){ return GB_PURCHASE_GOODS_STATUS_FINISHED; }
    public static Integer getGbPurchaseGoodsStatusReceive(){ return GB_PURCHASE_GOODS_STATUS_RECEIVE; }

    public static Integer getGbWeightGoodsStatusPrepare(){ return GB_WEIGHT_GOODS_STATUS_PREPARE; }
    public static Integer getGbWeightGoodsStatusPrinted(){ return GB_WEIGHT_GOODS_STATUS_PRINTED; }


    public static Integer getGbWeightTotalStatusUnFinished(){ return GB_WEIGHT_TOTAL_STATUS_UN_FINISHED; }
    public static Integer getGbWeightTotalStatusFinished(){ return GB_WEIGHT_TOTAL_STATUS_FINISHED; }

    public static Integer getGbDisPurchaseBatchUnSend() {
        return GB_DIS_PURCHASE_BATCH_UN_Send;
    }
    public static Integer getGbDisPurchaseBatchUnRead() {
        return GB_DIS_PURCHASE_BATCH_UN_READ;
    }
    public static Integer getGbDisPurchaseBatchHaveRead() {
        return GB_DIS_PURCHASE_BATCH_HAVE_READ;
    }
    public static Integer getGbDisPurchaseBatchSellerReply() {
        return GB_DIS_PURCHASE_BATCH_SELLER_REPLY;
    }
    public static Integer getGbDisPurchaseBatchDisUserFinish() {
        return GB_DIS_PURCHASE_BATCH_DIS_USER_FINISH;
    }
    public static Integer getGbDisPurchaseBatchDisUserFinishPay() {
        return GB_DIS_PURCHASE_BATCH_DIS_USER_FINISH_PAY;
    }
}
