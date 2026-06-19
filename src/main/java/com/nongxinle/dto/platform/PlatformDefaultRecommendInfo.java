package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformDefaultRecommendInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer defaultId;
    private Integer defaultDistributerId;
    private Integer defaultDisGoodsId;
    private String source;
}
