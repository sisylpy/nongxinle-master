package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class NxCustomerReferralReadStateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private Integer lastReadReferralId;
    private Date lastReadAt;
    private Date updatedAt;
}
