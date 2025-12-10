package com.nongxinle.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 企业微信监控的客户群
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Data
public class QyGbDisCorpMonitoredGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long qyGbDisCorpMonitoredGroupId;
    
    /**
     * 企业微信企业ID
     */
    private String qyGbDisQyCorpId;
    
    /**
     * 客户群ID
     */
    private String qyGbDisCorpMonitoredGroupChatId;
    
    /**
     * 客户群名称
     */
    private String qyGbDisCorpMonitoredGroupChatName;
    
    /**
     * 群主UserID
     */
    private String qyGbDisCorpMonitoredGroupOwner;
    
    /**
     * 群成员数量
     */
    private Integer qyGbDisCorpMonitoredGroupMemberCount;
    
    /**
     * 监控状态 1启用 0禁用
     */
    private Integer qyGbDisCorpMonitoredGroupStatus;
    
    /**
     * 创建时间
     */
    private Date qyGbDisCorpMonitoredGroupCreateDate;
    
    /**
     * 更新时间
     */
    private Date qyGbDisCorpMonitoredGroupUpdateDate;
}



