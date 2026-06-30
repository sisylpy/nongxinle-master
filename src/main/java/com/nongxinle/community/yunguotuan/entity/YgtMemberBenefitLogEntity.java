package com.nongxinle.community.yunguotuan.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class YgtMemberBenefitLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ygtMemberBenefitLogId;
    private Long ygtMblJoinSourceId;
    private Integer ygtMblCustomerUserId;
    private String ygtMblBenefitCode;
    private String ygtMblBenefitName;
    private String ygtMblDiscountText;
    private String ygtMblUseChannel;
    private String ygtMblStatus;
    private Date ygtMblCreateTime;
}
