package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SandboxComputeRequest {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private String depotLat;
    private String depotLng;
    /** 正式 pageViewModel 路径：跳过配送偏好、缩小 eligible 订单查询等。 */
    private boolean formalPageContractMode;
    /** 装车/配送页：只读已落库路线，跳过沙盘优化与地图矩阵重算。 */
    private boolean persistedRoutesOnlyMode;
}
