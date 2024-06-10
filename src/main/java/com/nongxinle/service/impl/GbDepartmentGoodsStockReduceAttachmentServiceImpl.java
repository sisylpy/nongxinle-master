package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepartmentGoodsStockReduceAttachmentDao;
import com.nongxinle.entity.GbDepartmentGoodsStockReduceAttachmentEntity;
import com.nongxinle.service.GbDepartmentGoodsStockReduceAttachmentService;



@Service("gbDepartmentGoodsStockReduceAttachmentService")
public class GbDepartmentGoodsStockReduceAttachmentServiceImpl implements GbDepartmentGoodsStockReduceAttachmentService {
	@Autowired
	private GbDepartmentGoodsStockReduceAttachmentDao gbDepartmentGoodsStockReduceAttachmentDao;
	
	@Override
	public GbDepartmentGoodsStockReduceAttachmentEntity queryObject(Integer gbDepartmentGoodsStockReduceAttachId){
		return gbDepartmentGoodsStockReduceAttachmentDao.queryObject(gbDepartmentGoodsStockReduceAttachId);
	}
	
	@Override
	public List<GbDepartmentGoodsStockReduceAttachmentEntity> queryList(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceAttachmentDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentGoodsStockReduceAttachmentDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentGoodsStockReduceAttachmentEntity gbDepartmentGoodsStockReduceAttachment){
		gbDepartmentGoodsStockReduceAttachmentDao.save(gbDepartmentGoodsStockReduceAttachment);
	}
	
	@Override
	public void update(GbDepartmentGoodsStockReduceAttachmentEntity gbDepartmentGoodsStockReduceAttachment){
		gbDepartmentGoodsStockReduceAttachmentDao.update(gbDepartmentGoodsStockReduceAttachment);
	}
	
	@Override
	public void delete(Integer gbDepartmentGoodsStockReduceAttachId){
		gbDepartmentGoodsStockReduceAttachmentDao.delete(gbDepartmentGoodsStockReduceAttachId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentGoodsStockReduceAttachIds){
		gbDepartmentGoodsStockReduceAttachmentDao.deleteBatch(gbDepartmentGoodsStockReduceAttachIds);
	}
	
}
