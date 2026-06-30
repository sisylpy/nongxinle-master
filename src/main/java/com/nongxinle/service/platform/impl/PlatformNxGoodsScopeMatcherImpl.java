package com.nongxinle.service.platform.impl;

import com.nongxinle.dao.NxGoodsDao;
import com.nongxinle.dto.coupon.CartLineSnapshot;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.platform.PlatformNxGoodsScopeMatcher;
import com.nongxinle.utils.CouponRuleConstants;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service("platformNxGoodsScopeMatcher")
public class PlatformNxGoodsScopeMatcherImpl implements PlatformNxGoodsScopeMatcher {

    private static final int MAX_PARENT_DEPTH = 16;

    @Autowired
    private NxGoodsDao nxGoodsDao;

    @Override
    public boolean matchesScope(CartLineSnapshot line, String scopeType, String scopeRefIds) {
        if (line == null) {
            return false;
        }
        String normalizedScope = scopeType == null ? CouponRuleConstants.SCOPE_ALL : scopeType;
        Set<Integer> refIds = parseScopeRefIds(scopeRefIds);
        if (CouponRuleConstants.SCOPE_ALL.equals(normalizedScope)) {
            return true;
        }
        if (CouponRuleConstants.SCOPE_GOODS.equals(normalizedScope)) {
            return line.getGoodsId() != null && refIds.contains(line.getGoodsId());
        }
        if (CouponRuleConstants.SCOPE_CATEGORY.equals(normalizedScope)) {
            if (line.getGoodsId() != null && categoryMatches(line.getGoodsId(), refIds)) {
                return true;
            }
            return line.getCategoryId() != null && categoryMatches(line.getCategoryId(), refIds);
        }
        return false;
    }

    @Override
    public boolean matchesNxGoods(String scopeType, Integer nxGoodsId, String scopeRefIds) {
        CartLineSnapshot line = new CartLineSnapshot();
        line.setGoodsId(nxGoodsId);
        return matchesScope(line, scopeType, scopeRefIds);
    }

    @Override
    public Set<Integer> collectCategoryAncestorIds(Integer nxGoodsId) {
        Set<Integer> ids = new HashSet<>();
        if (nxGoodsId == null || nxGoodsId <= 0) {
            return ids;
        }
        NxGoodsEntity goods = nxGoodsDao.queryObject(nxGoodsId);
        if (goods == null) {
            ids.add(nxGoodsId);
            return ids;
        }
        ids.add(nxGoodsId);
        addHierarchyIds(goods, ids);
        Integer fatherId = goods.getNxGoodsFatherId();
        int depth = 0;
        while (fatherId != null && fatherId > 0 && depth++ < MAX_PARENT_DEPTH) {
            ids.add(fatherId);
            NxGoodsEntity parent = nxGoodsDao.queryObject(fatherId);
            if (parent == null) {
                break;
            }
            addHierarchyIds(parent, ids);
            fatherId = parent.getNxGoodsFatherId();
        }
        return ids;
    }

    @Override
    public Set<Integer> parseScopeRefIds(String scopeRefIds) {
        Set<Integer> ids = new HashSet<>();
        if (scopeRefIds == null || scopeRefIds.trim().isEmpty()) {
            return ids;
        }
        try {
            JSONArray array = JSONArray.fromObject(scopeRefIds);
            for (int i = 0; i < array.size(); i++) {
                ids.add(array.getInt(i));
            }
        } catch (Exception ignored) {
            // 非法 JSON 视为无命中
        }
        return ids;
    }

    private boolean categoryMatches(Integer nxGoodsId, Set<Integer> refCategoryIds) {
        if (nxGoodsId == null || refCategoryIds.isEmpty()) {
            return false;
        }
        Set<Integer> ancestors = collectCategoryAncestorIds(nxGoodsId);
        for (Integer refId : refCategoryIds) {
            if (ancestors.contains(refId)) {
                return true;
            }
        }
        return false;
    }

    private void addHierarchyIds(NxGoodsEntity goods, Set<Integer> ids) {
        if (goods.getNxGoodsFatherId() != null && goods.getNxGoodsFatherId() > 0) {
            ids.add(goods.getNxGoodsFatherId());
        }
        if (goods.getNxGoodsGrandId() != null && goods.getNxGoodsGrandId() > 0) {
            ids.add(goods.getNxGoodsGrandId());
        }
        if (goods.getNxGoodsGreatGrandId() != null && goods.getNxGoodsGreatGrandId() > 0) {
            ids.add(goods.getNxGoodsGreatGrandId());
        }
    }
}
