package com.nongxinle.dto.route;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** Phase 2b-3：GET /drivers/available 响应体（正式合同：driverCards[] + summary）。 */
@Setter
@Getter
@ToString
public class DriverDispatchListResponse {
    private String routeDate;
    private String dispatchBatch;
    private String dispatchBatchLabel;
    private DriverDispatchListSummaryDto summary = new DriverDispatchListSummaryDto();
    /** 内部构建用；HTTP 不序列化。 */
    @JSONField(serialize = false)
    private List<DriverDispatchCandidateDto> drivers = new ArrayList<DriverDispatchCandidateDto>();
    /** 司机可派状态页：统一司机卡模板 */
    private List<DispatchDriverCardDto> driverCards = new ArrayList<DispatchDriverCardDto>();
}
