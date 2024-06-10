package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-15 15:52
 */

import com.nongxinle.entity.QyNxDisCorpEntity;


public interface QyNxDisCorpDao extends BaseDao<QyNxDisCorpEntity> {

    QyNxDisCorpEntity queryQyCropByCropId(String corpId);

    void deleteCrop(String corpid);
}
