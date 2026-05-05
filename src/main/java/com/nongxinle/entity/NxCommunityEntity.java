package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityId;
	/**
	 *  
	 */
	private String nxCommunityName;
	/**
	 *  
	 */
	private String nxCommunityLat;
	/**
	 *  
	 */
	private String nxCommunityLng;

	private NxLocation nxCommunityLocation;

	/**
	 *
	 */
	private Integer nxCommunityRouteId;

	private Integer nxCommunityCommerceId;
	private Integer nxCommunityType;

	private String nxCommunityPolygon;

	private String nxCommunityRegion;
	private String nxCommunityDeliveryAddress;
	private String nxCommunityWxFile;
	private String nxCommunityDoorFile;
	private String nxCommunityOpenTime;
	private String nxCommunityCloseTime;
	private String nxCommunityPrePrintTimes;

	private NxCommunityUserEntity nxCommunityUserEntity;
	private NxECommerceCommunityEntity nxECommerceCommunityEntity;
	private NxECommerceEntity nxECommerceEntity;

	private String nxCommunityDeliveryMixSubtotal;
	private String nxCommunityDeliveryFeeFreeDistance;
	private String nxCommunityDeliveryPayPercent;
	private String nxCommunityDeliveryPayMaxFee;

	private String nxCommunitySysCityId;
	private String nxCommunitySysBusinessAreaId;
	private String nxCommunitySysDistrictId;
	private String nxCommunitySysProvinceId;
	private String nxCommunityBillPrintSn;
	private String nxCommunityBusinessPhone;

	/**
	 * 取货日期偏移（从下单当天算起第几天取货，如1=次日，2=隔日）
	 */
	private Integer nxCommunityDeliveryDayOffset;
	/**
	 * 取货开始时间（分钟，如540=9:00，1080=18:00）
	 */
	private Integer nxCommunityDeliveryStartTime;
	/**
	 * 取货结束时间（分钟）
	 */
	private Integer nxCommunityDeliveryStopTime;

	public String getNxCommunityLocation() {
		return nxCommunityLocation != null ? "POINT(" + nxCommunityLocation.getLng() + " " + nxCommunityLocation.getLat() + ")" : null;
	}

	public void setNxCommunityLocation(String nxCommunityLocation) {
		if (nxCommunityLocation != null && nxCommunityLocation.startsWith("POINT(") && nxCommunityLocation.endsWith(")")) {
			String coordinates = nxCommunityLocation.substring(6, nxCommunityLocation.length() - 1); // "117.07822 39.98246"
			String[] coords = coordinates.split(" ");
			Double lng = Double.valueOf(coords[0]);
			Double lat = Double.valueOf(coords[1]);

			// 将经纬度转换为 NxLocation 对象
			this.nxCommunityLocation = new NxLocation(lng, lat);
		}
	}

}
