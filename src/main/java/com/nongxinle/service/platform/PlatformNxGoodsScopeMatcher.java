package com.nongxinle.service.platform;

import com.nongxinle.dto.coupon.CartLineSnapshot;

import java.util.Set;

/**
 * 京采平台券适用范围：分类/商品均基于 nx_goods 树（非 Community 分类）。
 */
public interface PlatformNxGoodsScopeMatcher {

    boolean matchesScope(CartLineSnapshot line, String scopeType, String scopeRefIds);

    boolean matchesNxGoods(String scopeType, Integer nxGoodsId, String scopeRefIds);

    Set<Integer> collectCategoryAncestorIds(Integer nxGoodsId);

    Set<Integer> parseScopeRefIds(String scopeRefIds);
}
