package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformSubmitLineResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private Integer platformAssignId;
    private String assignStatus;
    private String assignMode;
    private Integer nxDoDistributerId;
    private Integer nxDoDisGoodsId;
    private Integer nxDoCollaborativeNxDisId;
}
