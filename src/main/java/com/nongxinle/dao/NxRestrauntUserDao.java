package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxRestrauntEntity;
import com.nongxinle.entity.NxRestrauntUserEntity;

import java.util.List;


public interface NxRestrauntUserDao extends BaseDao<NxRestrauntUserEntity> {

    NxRestrauntUserEntity queryResUserByOpenId(String openid);

    NxRestrauntEntity queryAllResUsersByResId(Integer resId);

    NxRestrauntEntity queryAllRestrauntAndDepUsersByResId(Integer resId);

    NxRestrauntEntity queryChainRestrauntAndDepUsersByResId(Integer resId);
}
