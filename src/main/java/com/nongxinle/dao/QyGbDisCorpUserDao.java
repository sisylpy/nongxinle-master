package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-02 17:14
 */

import com.nongxinle.entity.QyGbDisCorpUserEntity;

import java.util.List;


public interface QyGbDisCorpUserDao extends BaseDao<QyGbDisCorpUserEntity> {

    List<QyGbDisCorpUserEntity> queryCorpUserListByCorpId(Integer qyNxDisCorpId);
}
