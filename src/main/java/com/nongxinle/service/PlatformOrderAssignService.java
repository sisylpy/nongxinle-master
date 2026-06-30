package com.nongxinle.service;

import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformAssignResponse;
import com.nongxinle.dto.platform.PlatformOrderDetailRequest;
import com.nongxinle.dto.platform.PlatformOrderDetailResponse;
import com.nongxinle.dto.platform.PlatformPendingRequest;
import com.nongxinle.dto.platform.PlatformPendingResponse;
import com.nongxinle.dto.platform.PlatformUnassignRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineResponse;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;

public interface PlatformOrderAssignService {

    PlatformSubmitLineResponse submitLine(PlatformSubmitLineRequest request);

    PlatformPendingResponse listPending(PlatformPendingRequest request);

    PlatformOrderDetailResponse getDetail(PlatformOrderDetailRequest request);

    PlatformAssignResponse assign(PlatformAssignRequest request);

    PlatformAssignResponse unassign(PlatformUnassignRequest request);

    NxPlatformOrderAssignEntity queryByOrderId(Integer orderId);
}
