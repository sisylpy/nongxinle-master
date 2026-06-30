package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtWecomGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtWecomGroupId;
    private String ygtWgCorpId;
    private String ygtWgChatId;
    private String ygtWgChatName;
    private String ygtWgOwnerUserId;
    private Integer ygtWgMemberCount;
    private Integer ygtWgStatus;
    private Integer ygtWgNxCommunityId;
    private Integer ygtWgLeaderUserId;
    private Integer ygtWgAdminUserId;
    private String ygtWgSource;
    private Date ygtWgCreateTime;
    private Date ygtWgUpdateTime;
}
