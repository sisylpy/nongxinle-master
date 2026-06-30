package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtGroupBuyCampaignEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtGroupBuyCampaignId;
    private String ygtGbcCorpId;
    private Long ygtGbcGroupId;
    private Integer ygtGbcNxCommunityId;
    private String ygtGbcTitle;
    private String ygtGbcStatus;
    private Date ygtGbcOpenTime;
    private Date ygtGbcCloseTime;
    private Date ygtGbcPickupTime;
    private String ygtGbcRemark;
    private Integer ygtGbcCreateUserId;
    private Date ygtGbcCreateTime;
    private Date ygtGbcUpdateTime;
}
