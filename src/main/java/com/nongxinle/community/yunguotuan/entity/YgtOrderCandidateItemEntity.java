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
public class YgtOrderCandidateItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtOrderCandidateItemId;
    private Long ygtOciCandidateId;
    private Long ygtOciCampaignGoodsId;
    private Integer ygtOciNxCommunityGoodsId;
    private String ygtOciGoodsNameSnapshot;
    private BigDecimal ygtOciQuantity;
    private String ygtOciUnit;
    private BigDecimal ygtOciPriceSnapshot;
    private BigDecimal ygtOciAmount;
    private String ygtOciRemark;
    private BigDecimal ygtOciConfidence;
    private Integer ygtOciManualAdjusted;
    private Date ygtOciCreateTime;
    private Date ygtOciUpdateTime;
}
