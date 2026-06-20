package com.nongxinle.service.platform;

import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;

/**
 * 平台选批发商下单：确保 GB 配送商商品存在（无映射时自动建档，与 saveOrdersGbJjAndSaveGoodsSx 一致）。
 */
public interface PlatformGbGoodsEnsureService {

    GbDistributerGoodsEntity ensureForNxDisGoods(
            Integer gbDistributerId,
            Integer catalogDepartmentId,
            NxDistributerGoodsEntity nxGoods);
}
