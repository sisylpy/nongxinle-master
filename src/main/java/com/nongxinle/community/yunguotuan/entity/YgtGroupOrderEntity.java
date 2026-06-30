package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
public class YgtGroupOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtGroupOrderId;
    private Long ygtGoCandidateId;
    private Long ygtGoCampaignId;
    private String ygtGoCorpId;
    private Long ygtGoGroupId;
    private Integer ygtGoNxCommunityId;
    private String ygtGoChatId;
    private Long ygtGoSourceChatMessageId;
    private String ygtGoFromUser;
    private String ygtGoMemberIdentifier;
    private String ygtGoExternalUserId;
    private String ygtGoCustomerNameSnapshot;
    private String ygtGoStatus;
    private String ygtGoPickupCode;
    private BigDecimal ygtGoTotalAmount;
    private Integer ygtGoConfirmUserId;
    private Date ygtGoConfirmTime;
    private String ygtGoRemark;
    private Date ygtGoCreateTime;
    private Date ygtGoUpdateTime;

    private List<YgtGroupOrderItemEntity> items;
}
