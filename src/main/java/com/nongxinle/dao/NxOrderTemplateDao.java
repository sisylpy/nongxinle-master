package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import com.nongxinle.entity.NxOrderTemplateEntity;

import java.util.Map;


public interface NxOrderTemplateDao extends BaseDao<NxOrderTemplateEntity> {

    NxOrderTemplateEntity queryTemplate(Map<String, Object> map);

}
