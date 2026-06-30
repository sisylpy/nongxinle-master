package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtArchiveCursorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtArchiveCursorId;
    private String ygtAcCorpId;
    private String ygtAcChatId;
    private Long ygtAcLastSeq;
    private String ygtAcLastPullStatus;
    private String ygtAcLastError;
    private Date ygtAcLastPullTime;
    private Date ygtAcLastSuccessTime;
    private Date ygtAcCreateTime;
    private Date ygtAcUpdateTime;
}
