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
public class YgtCampaignGoodsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtCampaignGoodsId;
    private Long ygtCgCampaignId;
    private Integer ygtCgNxCommunityGoodsId;
    private String ygtCgGoodsNameSnapshot;
    private String ygtCgStandardSnapshot;
    private String ygtCgUnitSnapshot;
    private BigDecimal ygtCgPriceSnapshot;
    private Integer ygtCgSort;
    private Integer ygtCgStatus;
    private Date ygtCgCreateTime;
    private Date ygtCgUpdateTime;
}
