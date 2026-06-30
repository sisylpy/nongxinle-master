package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtOrderCandidateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtOrderCandidateId;
    private String ygtOcCorpId;
    private Long ygtOcGroupId;
    private String ygtOcChatId;
    private Long ygtOcMessageId;
    private Long ygtOcCampaignId;
    private String ygtOcFromUser;
    private String ygtOcExternalUserId;
    private String ygtOcCustomerNameSnapshot;
    private String ygtOcMemberIdentifier;
    private Long ygtOcMsgTime;
    private String ygtOcOriginalText;
    private String ygtOcParseStatus;
    private String ygtOcStatus;
    private String ygtOcReviewRemark;
    private Integer ygtOcReviewerUserId;
    private Date ygtOcReviewedTime;
    private Date ygtOcCreateTime;
    private Date ygtOcUpdateTime;
}
