package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2B-1：人工调度司机全景响应。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchDriverPanoramaResponse {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    /** 统一分店卡模板 */
    private DispatchStoreCardDto storeCard;
    private List<DispatchDriverCardDto> drivers = new ArrayList<DispatchDriverCardDto>();
    private SandboxManualDispatchPanoramaSummaryDto summary;
}
