package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-29 16:35
 */

import com.nongxinle.entity.GbDepartmentGoodsStockReduceAttachmentEntity;

import java.util.List;
import java.util.Map;

public interface GbDepartmentGoodsStockReduceAttachmentService {
	
	GbDepartmentGoodsStockReduceAttachmentEntity queryObject(Integer gbDepartmentGoodsStockReduceAttachId);
	
	List<GbDepartmentGoodsStockReduceAttachmentEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(GbDepartmentGoodsStockReduceAttachmentEntity gbDepartmentGoodsStockReduceAttachment);
	
	void update(GbDepartmentGoodsStockReduceAttachmentEntity gbDepartmentGoodsStockReduceAttachment);
	
	void delete(Integer gbDepartmentGoodsStockReduceAttachId);
	
	void deleteBatch(Integer[] gbDepartmentGoodsStockReduceAttachIds);
}
