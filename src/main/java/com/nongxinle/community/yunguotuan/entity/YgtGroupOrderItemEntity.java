package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtGroupOrderItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtGroupOrderItemId;
    private Long ygtGoiOrderId;
    private Long ygtGoiCampaignId;
    private Long ygtGoiCandidateItemId;
    private Long ygtGoiCampaignGoodsId;
    private Integer ygtGoiNxCommunityGoodsId;
    private String ygtGoiGoodsNameSnapshot;
    private String ygtGoiSpecSnapshot;
    private BigDecimal ygtGoiQuantity;
    private String ygtGoiUnit;
    private String ygtGoiUnitSnapshot;
    private BigDecimal ygtGoiPriceSnapshot;
    private BigDecimal ygtGoiAmount;
    private String ygtGoiRemark;
    private Date ygtGoiCreateTime;
}
