package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 05-09 21:11
 */

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerGbDistributerEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGbDistributerDao extends BaseDao<NxDistributerGbDistributerEntity> {

    NxDistributerGbDistributerEntity queryObject (Integer nxDistributerGbDistributerId);

    List<GbDistributerEntity> queryAllGbDistribtuer(Integer disId);

    List<NxDistributerEntity> queryAllNxDistribtuer(Integer gbDisId);

    NxDistributerGbDistributerEntity queryObjectByParams(Map<String, Object> map);

    void save(NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity);

    List<NxDistributerEntity> queryGbDistributerNxDistribtuer(Integer gbDepId);

    List<NxDistributerEntity> queryGbDistributerNxDistribtuerGoods(Map<String, Object> map);
}
