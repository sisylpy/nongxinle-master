package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-16 12:25
 */

import com.nongxinle.entity.QyNxDisCorpUserEntity;

import java.util.List;


public interface QyNxDisCorpUserDao extends BaseDao<QyNxDisCorpUserEntity> {

    QyNxDisCorpUserEntity queryQyUserByUserId(String userId);

    List<QyNxDisCorpUserEntity> queryCorpUserListByCorpId(Integer corpid);
}
