package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCommunityUserPromotionEligibleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer communityUserId;
    private Integer enabled;
    private Date validStartAt;
    private Date validEndAt;
    private Date createdAt;
    private Date updatedAt;
}
