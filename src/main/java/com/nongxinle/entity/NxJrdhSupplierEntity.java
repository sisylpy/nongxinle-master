package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-11 21:54
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxJrdhSupplierEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer nxJrdhSupplierId;
	/**
	 *  供货商名称
	 */
	private String nxJrdhsSupplierName;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsGbDistributerId;
	/**
	 *  gbDepid
	 */
	private Integer nxJrdhsGbDepartmentId;
	/**
	 *  接单元id
	 */
	private Integer nxJrdhsUserId;
	private Integer nxJrdhsSysCityId;
	private Integer nxJrdhsSysMarketId;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsNxDistributerId;

	private Boolean isSelected = false;
	private String total;
	private int billCount;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsNxCommunityId;
	private Integer nxJrdhsStatus;


	private NxJrdhUserEntity jrdhUserEntity;
	private NxDistributerUserEntity nxPurUserEntity;
	private GbDepartmentUserEntity gbPurUserEntity;
	private NxCommunityUserEntity nxCommPurUserEntity;

	private Integer nxJrdhsNxPurUserId;
	private Integer nxJrdhsGbPurUserId;
	private Integer nxJrdhsCommPurUserId;
	private Integer nxJrdhsNxJrdhBuyUserId;
	private List<GbDistributerGoodsEntity> gbDistributerGoodsEntities;

	private NxJrdhUserEntity buyerUserEntity;
	private NxDistributerEntity nxDistributerEntity;
	private GbDistributerEntity gbDistributerEntity;
	private List<GbDistributerPurchaseBatchEntity> batchEntities;

	private Double purTimes = 0.0;
	private String purTimesString;

	private Double purGoodsTotal = 0.0;
	private String purGoodsTotalString;

	private Double stockGoodsTotal = 0.0;
	private String stockGoodsTotalString;

	private Double produceGoodsTotal = 0.0;
	private String produceGoodsTotalString;
	private Double wasteGoodsTotal = 0.0;
	private String wasteGoodsTotalString;
	private Double lossGoodsTotal = 0.0;
	private String lossGoodsTotalString;
	private Double returnGoodsTotal = 0.0;
	private String returnGoodsTotalString;

	private String freshStars;
	private int starGreen;
	private int starGray;
	private int starHalf;

	private String unPayTotal = "0.0";
	private String havePayTotal = "0.0";

	List<NxDistributerCouponEntity> couponEntities;
	List<NxGbDistibuterUserCouponEntity> myCouponEntities;
	List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities;
	private Map<String,Object> itemData;


}
