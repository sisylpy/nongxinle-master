package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-27 12:56
 */

import com.nongxinle.entity.NxLabelEntity;
import com.nongxinle.entity.NxLabelTypeEntity;

import java.util.List;


public interface NxLabelTypeDao extends BaseDao<NxLabelTypeEntity> {

    List<NxLabelEntity> queryAllLabel();
    
}
