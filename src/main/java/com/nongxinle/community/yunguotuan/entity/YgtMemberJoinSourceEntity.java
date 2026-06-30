package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtMemberJoinSourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtMemberJoinSourceId;
    private Integer ygtMjsCustomerUserId;
    private String ygtMjsOpenId;
    private String ygtMjsUnionId;
    private String ygtMjsWecomExternalUserId;
    private String ygtMjsWecomUserId;
    private String ygtMjsPhoneSnapshot;
    private String ygtMjsNicknameSnapshot;
    private Long ygtMjsSourceCampaignId;
    private Long ygtMjsSourceWecomGroupId;
    private String ygtMjsSourceShareCode;
    private Integer ygtMjsInviterCustomerUserId;
    private String ygtMjsBindStatus;
    private String ygtMjsBindSource;
    private String ygtMjsMatchMethod;
    private Date ygtMjsCreateTime;
    private Date ygtMjsUpdateTime;
}
