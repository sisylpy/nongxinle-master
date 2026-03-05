package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerBlockDao;
import com.nongxinle.entity.NxDistributerBlockEntity;
import com.nongxinle.service.NxDistributerBlockService;
import com.nongxinle.service.NxDistributerNxDistributerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配送商屏蔽 Service 实现
 *
 * @author lpy
 */
@Service("nxDistributerBlockService")
public class NxDistributerBlockServiceImpl implements NxDistributerBlockService {

    @Autowired
    private NxDistributerBlockDao nxDistributerBlockDao;
    @Autowired
    private NxDistributerNxDistributerService nxDistributerNxDistributerService;

    @Override
    public List<Integer> queryBlockedDisIdsByBlocker(Integer blockerNxDistributerId) {
        if (blockerNxDistributerId == null) {
            return java.util.Collections.emptyList();
        }
        return nxDistributerBlockDao.queryBlockedDisIdsByBlocker(blockerNxDistributerId);
    }

    @Override
    public boolean blockPartner(Integer blockerNxDistributerId, Integer blockedNxDistributerId) {
        if (blockerNxDistributerId == null || blockedNxDistributerId == null) {
            return false;
        }
        if (blockerNxDistributerId.equals(blockedNxDistributerId)) {
            return false;
        }
        // 校验：被拉黑的必须是协作伙伴
        Map<String, Object> map = new HashMap<>();
        map.put("disId", blockerNxDistributerId);
        List<com.nongxinle.entity.NxDistributerEntity> partners =
                nxDistributerNxDistributerService.queryOfferNxDisByParams(map);
        boolean isPartner = partners.stream().anyMatch(p -> p.getNxDistributerId().equals(blockedNxDistributerId));
        if (!isPartner) {
            return false;
        }
        // 已拉黑则直接返回成功
        if (nxDistributerBlockDao.countByBlockerAndBlocked(blockerNxDistributerId, blockedNxDistributerId) > 0) {
            return true;
        }
        NxDistributerBlockEntity entity = new NxDistributerBlockEntity();
        entity.setBlockerNxDistributerId(blockerNxDistributerId);
        entity.setBlockedNxDistributerId(blockedNxDistributerId);
        return nxDistributerBlockDao.save(entity) > 0;
    }

    @Override
    public boolean unblockPartner(Integer blockerNxDistributerId, Integer blockedNxDistributerId) {
        if (blockerNxDistributerId == null || blockedNxDistributerId == null) {
            return false;
        }
        return nxDistributerBlockDao.deleteByBlockerAndBlocked(blockerNxDistributerId, blockedNxDistributerId) > 0;
    }

    @Override
    public boolean setHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId) {
        if (myDisId == null || partnerDisId == null || myDisId.equals(partnerDisId)) {
            return false;
        }
        // 不给他看我的商品 = block(blocker=对方, blocked=我)
        return blockPartner(partnerDisId, myDisId);
    }

    @Override
    public boolean unsetHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId) {
        if (myDisId == null || partnerDisId == null) {
            return false;
        }
        return nxDistributerBlockDao.deleteByBlockerAndBlocked(partnerDisId, myDisId) > 0;
    }

    @Override
    public int deleteByPartnerPair(Integer disId1, Integer disId2) {
        if (disId1 == null || disId2 == null) {
            return 0;
        }
        return nxDistributerBlockDao.deleteByPartnerPair(disId1, disId2);
    }

    @Override
    public List<Integer> queryBlockerDisIdsByBlocked(Integer blockedNxDistributerId) {
        if (blockedNxDistributerId == null) {
            return java.util.Collections.emptyList();
        }
        return nxDistributerBlockDao.queryBlockerDisIdsByBlocked(blockedNxDistributerId);
    }
}
