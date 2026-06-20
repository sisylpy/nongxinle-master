package com.nongxinle.dto;

import com.nongxinle.entity.NxDistributerStandardEntity;
import com.nongxinle.entity.NxGoodsEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;

/**
 * 部门订单简化DTO
 * 用于phoneGetToFillDepOrders接口，只包含页面需要的字段，减少数据传输量
 * 
 * @author lpy
 */
@Setter
@Getter
@ToString
public class NxDepartmentOrdersSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门订单ID
     */
    private Integer nxDepartmentOrdersId;

    /**
     * 订单商品名称
     */
    private String nxDoGoodsName;
    private String nxDoGoodsOriginalName;

    /**
     * 订单数量
     */
    private String nxDoQuantity;

    /**
     * 订单规格
     */
    private String nxDoStandard;

    /**
     * 订单备注
     */
    private String nxDoRemark;

    /**
     * 订单状态
     */
    private Integer nxDoStatus;

    /**
     * 订单重量
     */
    private String nxDoWeight;

    /**
     * 订单单价
     */
    private String nxDoPrice;

    /**
     * 平台分配参考价 / 期望单价
     */
    private String nxDoExpectPrice;

    /**
     * 实际价 - 期望价
     */
    private String nxDoPriceDifferent;

    /** 平台订单：1 平台 / 0 自有 */
    private Integer isPlatformOrder;
    private String orderSource;
    private Integer platformAssignId;
    private String platformFulfillmentStatus;
    private String platformLabel;
    private Integer priceEditable;
    private Integer platformSort;
    /** 展示别名 */
    private String expectPrice;
    private String actualPrice;
    private String priceDifferent;

    /**
     * 订单小计
     */
    private String nxDoSubtotal;

    /**
     * 打印规格
     */
    private String nxDoPrintStandard;

    /**
     * 采购状态
     */
    private Integer nxDoPurchaseStatus;

    /**
     * 分销商商品ID
     */
    private Integer nxDoDisGoodsId;
    private Integer nxDoOcrTaskId;

    /**
     * 部门ID
     */
    private Integer nxDoDepartmentId;

    /**
     * 是否代理订单
     */
    private Integer nxDoIsAgent;


    /**
     * 商品信息（简化版）
     */
    private DistributerGoodsSimpleDTO nxDistributerGoodsEntity;

    List<DistributerGoodsSimpleDTO> nxDistributerGoodsEntityList;
    private TreeSet<NxGoodsEntity> nxGoodsEntities;



    /**
     * 部门商品信息（简化版）
     */
    private DepartmentDisGoodsSimpleDTO nxDepartmentDisGoodsEntity;

    /**
     * 分销商商品简化DTO
     */
    @Setter
    @Getter
    @ToString
    public static class DistributerGoodsSimpleDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 分销商商品ID
         */
        private Integer nxDistributerGoodsId;

        /**
         * 商品名称
         */
        private String nxDgGoodsName;

        /**
         * 商品品牌
         */
        private String nxDgGoodsBrand;

        /**
         * 商品产地
         */
        private String nxDgGoodsPlace;

        /**
         * 商品详情
         */
        private String nxDgGoodsDetail;

        /**
         * 商品规格名称
         */
        private String nxDgGoodsStandardname;

        /**
         * 商品标准重量
         */
        private String nxDgGoodsStandardWeight;

        /**
         * 外箱单位
         */
        private String nxDgCartonUnit;

        /**
         * 每箱数量
         */
        private String nxDgItemsPerCarton;

        /**
         * 阶梯建议零售价规格名 / 重量 / 价格（与 nx_distributer_goods 一致）
         */
        private String nxDgWillPriceTwoStandard;
        private String nxDgWillPriceThreeStandard;
        private String nxDgWillPriceTwoAboutPrice;
        private String nxDgWillPriceThreeAboutPrice;
        private String nxDgWillPriceTwoWeight;
        private String nxDgWillPriceThreeWeight;
        private String nxDgWillPriceOne;
        private String nxDgWillPriceTwo;
        private String nxDgWillPriceThree;
        private String nxDgBuyingPriceOneUpdate;
        private String nxDgBuyingPriceTwoUpdate;
        private String nxDgBuyingPriceThreeUpdate;

        /**
         * 商品父级颜色
         */
        private String nxDgNxGoodsFatherColor;

        private String nxDgDistributerName;
        private String nxDgDistributerId;

        private String goodsNxDistributerName;

        /**
         * 分销商商品订货规格列表（与 NxDistributerGoodsEntity.nxDistributerStandardEntities 一致）
         */
        private List<NxDistributerStandardEntity> nxDistributerStandardEntities;
    }

    /**
     * 部门商品简化DTO
     */
    @Setter
    @Getter
    @ToString
    public static class DepartmentDisGoodsSimpleDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 订单商品名称
         */
        private String nxDdgOrderGoodsName;
    }
}

