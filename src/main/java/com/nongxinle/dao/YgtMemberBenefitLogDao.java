package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtMemberBenefitLogEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YgtMemberBenefitLogDao extends BaseDao<YgtMemberBenefitLogEntity> {
    YgtMemberBenefitLogEntity queryByJoinSourceAndCode(@Param("joinSourceId") Long joinSourceId,
                                                       @Param("benefitCode") String benefitCode);

    List<YgtMemberBenefitLogEntity> queryByJoinSourceId(@Param("joinSourceId") Long joinSourceId);
}
