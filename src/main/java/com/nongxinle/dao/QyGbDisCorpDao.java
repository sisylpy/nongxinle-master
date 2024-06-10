package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-02 22:59
 */

import com.nongxinle.entity.QyGbDisCorpEntity;


public interface QyGbDisCorpDao extends BaseDao<QyGbDisCorpEntity> {

    QyGbDisCorpEntity queryQyCropByCropId(String corpid);

    void deleteCropByCropId(String corpid);

}
