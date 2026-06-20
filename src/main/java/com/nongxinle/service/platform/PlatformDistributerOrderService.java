package com.nongxinle.service.platform;

import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderIdRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderLinesRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderPriceRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderWeightRequest;

import java.util.List;
import java.util.Map;

/**
 * 配送商端平台订单处理（与自有 nx / G 地 GB 出库链隔离）。
 */
public interface PlatformDistributerOrderService {

    List<NxDepartmentOrdersSimpleDTO> listOrderLines(PlatformDistributerOrderLinesRequest request);

    Map<String, Object> saveWeight(PlatformDistributerOrderWeightRequest request);

    Map<String, Object> finishOutbound(PlatformDistributerOrderWeightRequest request);

    Map<String, Object> updatePrice(PlatformDistributerOrderPriceRequest request);

    Map<String, Object> cancelOutbound(PlatformDistributerOrderIdRequest request);
}
