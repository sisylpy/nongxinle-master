package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-09 21:11
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;

public interface NxDistributerGbDistributerService  {
    NxDistributerGbDistributerEntity queryObject(Integer nxDistributerGbDistributerId);


    List<GbDistributerEntity> queryAllGbDistribtuer(Integer disId);

    List<NxDistributerEntity> queryAllNxDistribtuer(Integer gbDisId);

    NxDistributerGbDistributerEntity queryObjectByParams(Map<String, Object> map);

    void save (NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity);

    List<NxDistributerEntity> queryGbDistributerNxDistribtuer(Integer gbDepId);

    void update(NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity);

    void delete(Integer id);

    List<NxDistributerEntity> queryGbDistributerNxDistribtuerGoods(Map<String, Object> map);

}
