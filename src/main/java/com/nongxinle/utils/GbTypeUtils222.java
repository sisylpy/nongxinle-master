package com.nongxinle.utils;


import com.fasterxml.jackson.annotation.JsonIgnoreType;

import java.lang.annotation.Retention;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 商品类型
 * 
 */
public class GbTypeUtils222 {

    /** GreatBegin  */


    public final static Integer GB_PURCHASE_BATCH_PAY_TYPE_CASH = 0;
    public final static Integer GB_PURCHASE_BATCH_PAY_TYPE_ACCOUNT = 1;


    public final static Integer GB_DEPARTMENT_TYPE_MENDIAN = 3;
    public final static Integer GB_DEPARTMENT_TYPE_JICAI = 1;
    public final static Integer GB_DEPARTMENT_TYPE_KUFNG = 2;
    public final static Integer GB_DEPARTMENT_TYPE_ZHONGYANGCHUFANG = 4;
    public final static Integer GB_DEPARTMENT_TYPE_JIAMENG = 5;


    public final static Integer GB_DIS_GOODS_TYPE_JICAI = 1;
    public final static Integer GB_DIS_GOODS_TYPE_CHUKU = 2;
    public final static Integer GB_DIS_GOODS_TYPE_ZICAI = 3;
    public final static Integer GB_DIS_GOODS_TYPE_ZHONGYINGCHUFANG = 4;
    public final static Integer GB_DIS_GOODS_TYPE_GONGHUOSHANG = 6;
    public final static Integer GB_DIS_GOODS_TYPE_PEISONGSHANG = 7;

    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_MONTH = 3;
    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_WEEK = 2;
    public final static Integer GB_DIS_GOODS_INVENTORY_TYPE_DAILY = 1;

    public final static Integer GB_ORDER_TYPE_JICAI = 1;
    public final static Integer GB_ORDER_TYPE_CHUKU = 2;
    public final static Integer GB_ORDER_TYPE_ZICAI = 3;
    public final static Integer GB_ORDER_TYPE_CHUKU_CAIGOU = 4;
    public final static Integer GB_ORDER_TYPE_ZHONGYIANGCHUFANG = 5;
    public final static Integer GB_ORDER_TYPE_GONGHUOSHANG = 6;
    public final static Integer GB_ORDER_TYPE_TUIHUO = 7;

    public final static Integer GB_DEP_USER_ADMIN_MENDIAN = 1;  //门店管理端
    public final static Integer GB_DEP_USER_ADMIN_MENDIANDINGHUO = 2; //门店订货端
    public final static Integer GB_DEP_USER_ADMIN_MENDIANDCAIGOU = 3; //门店采购端
    public final static Integer GB_DEP_USER_ADMIN_JICAI = 4; //采购端
    public final static Integer GB_DEP_USER_ADMIN_KUFANG = 5; //库房端
    public final static Integer GB_DEP_USER_ADMIN_KUFANGCAIGOUYUAN = 6; //库房采购员
    public final static Integer GB_DEP_USER_ADMIN_PEISONGYUAN = 7; //
    public final static Integer GB_DEP_USER_ADMIN_KITCHEN = 8; //

    //1,cost;2waste;3loass;4return
//    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_COST = 1;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_PRODUCE = 1;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_WASTE = 2;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_LOSS = 3;
    public final static Integer GB_DEPART_GOODS_STOCK_REDUCE_TYPE_RETURN = 4;


    public static Integer getDISGoodsInventroyMonth(){
        return GB_DIS_GOODS_INVENTORY_TYPE_MONTH;
    }
    public static Integer getDISGoodsInventroyWeek(){
        return GB_DIS_GOODS_INVENTORY_TYPE_WEEK;
    } public static Integer getDISGoodsInventroyDaily(){
        return GB_DIS_GOODS_INVENTORY_TYPE_DAILY;
    }

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
    public static Integer getGbDepartmentTypeMendian(){
        return GB_DEPARTMENT_TYPE_MENDIAN;
    }
    public static Integer getGbDepartmentTypeJiameng(){
        return GB_DEPARTMENT_TYPE_JIAMENG;
    }
    public static Integer getGbDepartmentTypeZhongyangchufang(){
        return GB_DEPARTMENT_TYPE_ZHONGYANGCHUFANG;
    }


    public static Integer getGbDisGoodsTypeJicai(){ return GB_DIS_GOODS_TYPE_JICAI; }
    public static Integer getGbDisGoodsTypeChuku(){ return GB_DIS_GOODS_TYPE_CHUKU; }
    public static Integer getGbDisGoodsTypeZicai(){ return GB_DIS_GOODS_TYPE_ZICAI; }
    public static Integer getGbDisGoodsTypeZhongyingchufang(){ return GB_DIS_GOODS_TYPE_ZHONGYINGCHUFANG; }
    public static Integer getGbDisGoodsTypeGonghuoshang(){ return GB_DIS_GOODS_TYPE_GONGHUOSHANG; }
    public static Integer getGbDisGoodsTypePeisongshang(){ return GB_DIS_GOODS_TYPE_PEISONGSHANG; }

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
    public static  Integer getGbOrderTypeZhongyangchufang() {
        return GB_ORDER_TYPE_ZHONGYIANGCHUFANG;
    }
    public static  Integer getGbOrderTypeGonghuoshang() {
        return GB_ORDER_TYPE_GONGHUOSHANG;
    }
    public static  Integer getGbOrderTypeTuihuo() {
        return GB_ORDER_TYPE_TUIHUO;
    }


    public static Integer getGbDepUserAdminMendianjingli(){ return GB_DEP_USER_ADMIN_MENDIAN; }
    public static Integer getGbDepUserAdminMendiandinghuoyuan(){ return GB_DEP_USER_ADMIN_MENDIANDINGHUO; }
    public static Integer getGbDepUserAdminMendiancaigouyuan(){ return GB_DEP_USER_ADMIN_MENDIANDCAIGOU; }
    public static Integer getGbDepUserAdminJicaiyuan(){ return GB_DEP_USER_ADMIN_JICAI; }
    public static Integer getGbDepUserAdminKufangguanliyuan(){ return GB_DEP_USER_ADMIN_KUFANG; }
    public static Integer getGbDepUserAdminKitchenguanliyuan(){ return GB_DEP_USER_ADMIN_KITCHEN; }
    public static Integer getGbDepUserAdminKufangcaigouyuan(){ return GB_DEP_USER_ADMIN_KUFANGCAIGOUYUAN; }
    public static Integer getGbDepUserAdminPeisongyuan(){ return GB_DEP_USER_ADMIN_PEISONGYUAN; }



}
