package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-22 15:25
 */

import com.nongxinle.entity.NxRetailerEntity;

import java.util.Map;


public interface NxRetailerDao extends BaseDao<NxRetailerEntity> {


    NxRetailerEntity queryRetailerInfo(Integer nxRetuRetailerId);
}
