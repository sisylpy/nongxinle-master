package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-14 12:03
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bouncycastle.est.LimitedSource;


@Setter@Getter@ToString

public class NxDistributerGoodsShelfStockEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerGoodsShelfStockId;
	/**
	 *  
	 */
	private Integer nxDgssNxDistributerId;
	/**
	 *  
	 */
	private Integer nxDgssNxDisGoodsId;
	/**
	 *  
	 */
	private Integer nxDgssNxDisGoodsFatherId;
	/**
	 *  批次数量
	 */
	private String nxDgssWeight;
	/**
	 *  批次单价（最小单位单价）
	 */
	private String nxDgssPrice;
	/**
	 *  外包装采购单价（按箱/按件等）
	 */
	private String nxDgssPriceCarton;
	/**
	 *  销售单价（最小单位建议零售价）
	 */
	private String nxDgssSellingPrice;
	/**
	 *  外包装建议零售价（按箱/按件等）
	 */
	private String nxDgssSellingPriceCarton;
	/**
	 *  批次成本
	 */
	private String nxDgssSubtotal;
	/**
	 *  剩余数量
	 */
	private String nxDgssRestWeight;
	/**
	 *  剩余数量显示规格
	 */
	private String nxDgssRestWeightShowStandard;
	/**
	 *  批次剩余成本
	 */
	private String nxDgssRestSubtotal;
	/**
	 *  批次日期
	 */
	private String nxDgssDate;
	/**
	 *  盘库日期
	 */
	private String nxDgssFullTime;
	/**
	 *  出库日期
	 */
	private String nxDgssOutDate;
	/**
	 *  出库时间
	 */
	private String nxDgssOutFullTime;
	/**
	 *  出货小时
	 */
	private Integer nxDgssOutHour;
	/**
	 *  接收用户
	 */
	private Integer nxDgssReceiveUserId;
	/**
	 *  批次状态
	 */
	private Integer nxDgssStatus;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssNxPurGoodsId;
	/**
	 *  批次周
	 */
	private String nxDgssWeek;
	/**
	 *  批次月
	 */
	private String nxDgssMonth;
	/**
	 *  批次年
	 */
	private String nxDgssYear;
	/**
	 *  时间戳
	 */
	private String nxDgssTimeStamp;
	/**
	 *  盘库日期
	 */
	private String nxDgssInventoryFullTime;
	/**
	 *  盘库日期
	 */
	private String nxDgssInventoryDate;
	/**
	 *  盘库周
	 */
	private String nxDgssInventoryWeek;
	/**
	 *  盘库月
	 */
	private String nxDgssInventoryMonth;
	/**
	 *  盘库月
	 */
	private String nxDgssInventoryYear;
	/**
	 *  退货数量
	 */
	private String nxDgssReturnWeight;
	/**
	 *  退货成本
	 */
	private String nxDgssReturnSubtotal;
	/**
	 *  制作数量
	 */
	private String nxDgssProduceWeight;
	/**
	 *  制作成本
	 */
	private String nxDgssProduceSubtotal;
	/**
	 *  损耗数量
	 */
	private String nxDgssLossWeight;
	/**
	 *  损耗成本
	 */
	private String nxDgssLossSubtotal;
	/**
	 *  制作数量
	 */
	private String nxDgssWasteWeight;
	/**
	 *  制作数量
	 */
	private String nxDgssWasteSubtotal;
	/**
	 *  利润小计
	 */
	private String nxDgssProfitSubtotal;
	/**
	 *  利润重量
	 */
	private String nxDgssProfitWeight;
	/**
	 *  销售小计
	 */
	private String nxDgssSellingSubtotal;
	/**
	 *  销售利润
	 */
	private String nxDgssAfterProfitSubtotal;
	/**
	 *  成本率
	 */
	private String nxDgssCostRate;
	/**
	 *  剩余数量显示规格
	 */
	private String nxDgssRestWeightShowStandardName;
	/**
	 *  销售小计
	 */
	private String nxDgssProduceSellingSubtotal;
	private Integer nxDgssNxShelfGoodsId;
	private String nxDgssInventoryWeight;
	/**
	 *  部门父ID
	 */
	private Integer nxDgssNxDepartmentFatherId;
	/**
	 *  库存图片
	 */
	private String nxDgssStockImage;
	/**
	 *  库存说明
	 */
	private String nxDgssStockRemark;
	/**
	 *  返还积分（库存确认后返还给部门的积分）
	 */
	private String nxDgssReturnPoints;
	/**
	 *  返还积分时间（精确到分钟）
	 */
	private String nxDgssReturnPointsTime;
	/**
	 *  部门积分（关联查询返回，用于显示）
	 */
	private String departmentPoints;
	/**
	 *  部门等待积分（关联查询返回，用于显示）
	 */
	private String departmentWaitingPoints;
	/**
	 *  积分返还时间（关联查询返回，用于显示）
	 */
	private String pointsReturnTime;
	/**
	 *  货架名称（关联查询返回，用于显示）
	 */
	private String shelfName;
	/**
	 * 货架商品层级（与 shelfName 对应本批次所在货架商品行）
	 */
	private Integer nxDgsgShelfLayer;
	/**
	 * 货架商品同层序号
	 */
	private Integer nxDgsgShelfLayerSeq;
	/**
	 * 溯源报告ID
	 */
	private Integer nxDgssTraceReportId;
	/**
	 * 生产日期（如 2025-03-18）
	 */
	private String nxDgssProduceDate;
	/**
	 * 保质期（数值，如 3、7、15）
	 */
	private Integer nxDgssShelfLife;
	/**
	 * 保质期单位（天/月/年）
	 */
	private String nxDgssShelfLifeUnit;
	/**
	 * 过期日期（如 2025-03-21）
	 */
	private String nxDgssExpiryDate;

	private List<NxDistributerGoodsShelfStockReduceEntity> reduceEntityList;

	// 采购商品对象（该库存批次对应的采购商品）
	private NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity;

	// 溯源报告对象（该库存批次对应的溯源报告）
	private NxTraceReportEntity nxTraceReportEntity;

}
