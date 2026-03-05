package com.nongxinle.dao;

import com.nongxinle.entity.NxDistributerBlockEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配送商屏蔽 Dao
 *
 * @author lpy
 */
public interface NxDistributerBlockDao {

    /**
     * 查询某配送商屏蔽的配送商 id 列表（blocker 屏蔽了哪些配送商）
     *
     * @param blockerNxDistributerId 屏蔽者配送商 id
     * @return 被屏蔽的配送商 id 列表
     */
    List<Integer> queryBlockedDisIdsByBlocker(Integer blockerNxDistributerId);

    /**
     * 添加屏蔽
     */
    int save(NxDistributerBlockEntity entity);

    /**
     * 取消屏蔽
     */
    int deleteByBlockerAndBlocked(@Param("blockerNxDistributerId") Integer blockerNxDistributerId,
                                  @Param("blockedNxDistributerId") Integer blockedNxDistributerId);

    /**
     * 是否已存在屏蔽记录
     */
    int countByBlockerAndBlocked(@Param("blockerNxDistributerId") Integer blockerNxDistributerId,
                                 @Param("blockedNxDistributerId") Integer blockedNxDistributerId);

    /**
     * 删除两个配送商之间的所有屏蔽记录（双向）
     */
    int deleteByPartnerPair(@Param("disId1") Integer disId1, @Param("disId2") Integer disId2);

    /**
     * 查询谁屏蔽了我（blocked=我 的 blocker 列表）
     */
    List<Integer> queryBlockerDisIdsByBlocked(Integer blockedNxDistributerId);
}
