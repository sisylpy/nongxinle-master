package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YgtOrderCandidateItemDao extends BaseDao<YgtOrderCandidateItemEntity> {
    List<YgtOrderCandidateItemEntity> queryByCandidateId(@Param("candidateId") Long candidateId);

    int deleteByCandidateId(@Param("candidateId") Long candidateId);
}
