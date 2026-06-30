package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtMemberShareInviteEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtMemberShareInviteId;
    private String ygtMsiShareCode;
    private String ygtMsiSourceType;
    private Integer ygtMsiInviterCustomerUserId;
    private Long ygtMsiWecomGroupId;
    private Long ygtMsiCampaignId;
    private String ygtMsiCorpId;
    private String ygtMsiChatId;
    private Integer ygtMsiCommunityId;
    private String ygtMsiStatus;
    private Date ygtMsiExpireTime;
    private Date ygtMsiCreateTime;
    private Date ygtMsiUpdateTime;
}
