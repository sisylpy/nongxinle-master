package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxManualDispatchDriverPanoramaResponse {
    private Integer disId;
    private String routeDate;
    private String batchCode;
    private DispatchStoreCardDto storeCard;
    private List<DispatchDriverCardDto> drivers = new ArrayList<DispatchDriverCardDto>();
    private SandboxManualDispatchPanoramaSummaryDto summary;
}
