package com.nongxinle.community.yunguotuan.service;

import java.util.List;
import java.util.Map;

public interface YgtGroupOrderService {
    List<Map<String, Object>> queryOrders(Map<String, Object> params);

    Map<String, Object> orderDetail(Long id);
}
