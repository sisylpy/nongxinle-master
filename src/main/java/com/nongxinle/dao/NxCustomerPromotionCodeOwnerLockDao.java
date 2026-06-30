package com.nongxinle.dao;

import org.apache.ibatis.annotations.Param;

public interface NxCustomerPromotionCodeOwnerLockDao {

    void ensureLockRow(@Param("ownerType") String ownerType, @Param("ownerId") Integer ownerId);

    void lockOwnerForUpdate(@Param("ownerType") String ownerType, @Param("ownerId") Integer ownerId);

    int deleteByOwner(@Param("ownerType") String ownerType, @Param("ownerId") Integer ownerId);
}
