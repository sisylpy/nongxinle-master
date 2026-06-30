package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtChatMessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtChatMessageId;
    private String ygtCmCorpId;
    private Long ygtCmGroupId;
    private String ygtCmChatId;
    private String ygtCmMsgId;
    private Long ygtCmSeq;
    private Integer ygtCmPublicKeyVer;
    private String ygtCmAction;
    private String ygtCmFromUser;
    private Long ygtCmMsgTime;
    private String ygtCmMsgType;
    private String ygtCmContent;
    private String ygtCmRawJson;
    private String ygtCmParseStatus;
    private String ygtCmParseError;
    private Date ygtCmCreateTime;
    private Date ygtCmUpdateTime;
}
