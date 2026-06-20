package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.LineAmountConfirmResult;
import com.nongxinle.entity.NxDistributerGoodsEntity;

/**
 * 平台现金行级金额是否可直接确认（不依赖前端 expectPrice）。
 */
public interface PlatformLineAmountConfirmService {

    LineAmountConfirmResult isLineAmountConfirmable(String quantity, String orderStandard,
                                                    NxDistributerGoodsEntity goods);
}
