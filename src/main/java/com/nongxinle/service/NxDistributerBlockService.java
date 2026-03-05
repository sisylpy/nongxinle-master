package com.nongxinle.service;

import java.util.List;

/**
 * 配送商屏蔽 Service
 *
 * @author lpy
 */
public interface NxDistributerBlockService {

    /**
     * 查询某配送商屏蔽的配送商 id 列表
     *
     * @param blockerNxDistributerId 屏蔽者配送商 id
     * @return 被屏蔽的配送商 id 列表
     */
    List<Integer> queryBlockedDisIdsByBlocker(Integer blockerNxDistributerId);

    /**
     * 拉黑协作伙伴（当前配送商屏蔽某协作伙伴，查询商品时将看不到该伙伴的商品）
     *
     * @param blockerNxDistributerId 拉黑者配送商 id
     * @param blockedNxDistributerId 被拉黑的协作伙伴 id
     * @return true 成功，false 失败（如非协作伙伴或已拉黑）
     */
    boolean blockPartner(Integer blockerNxDistributerId, Integer blockedNxDistributerId);

    /**
     * 取消拉黑
     */
    boolean unblockPartner(Integer blockerNxDistributerId, Integer blockedNxDistributerId);

    /**
     * 设置「不给他看我的商品」：对方看不到我的商品
     * @param myDisId 当前配送商 id
     * @param partnerDisId 协作伙伴 id
     */
    boolean setHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId);

    /**
     * 取消「不给他看我的商品」
     */
    boolean unsetHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId);

    /**
     * 删除两个配送商之间的所有屏蔽记录（解除协作时调用）
     */
    int deleteByPartnerPair(Integer disId1, Integer disId2);

    /**
     * 查询谁屏蔽了我（谁看不到我的商品）
     */
    List<Integer> queryBlockerDisIdsByBlocked(Integer blockedNxDistributerId);
}
