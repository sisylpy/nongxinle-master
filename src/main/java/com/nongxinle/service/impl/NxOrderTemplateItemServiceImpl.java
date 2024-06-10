package com.nongxinle.service.impl;

import com.nongxinle.dao.NxOrderTemplateDao;
import com.nongxinle.entity.NxOrderTemplateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxOrderTemplateItemDao;
import com.nongxinle.entity.NxOrderTemplateItemEntity;
import com.nongxinle.service.NxOrderTemplateItemService;



@Service("nxOrderTemplateItemService")
public class NxOrderTemplateItemServiceImpl implements NxOrderTemplateItemService {
	@Autowired
	private NxOrderTemplateItemDao nxOrderTemplateItemDao;

	@Autowired
	private NxOrderTemplateDao nxOrderTemplateDao;
	
	@Override
	public NxOrderTemplateItemEntity queryObject(Integer nxOtItemId){
		return nxOrderTemplateItemDao.queryObject(nxOtItemId);
	}
	
	@Override
	public List<NxOrderTemplateItemEntity> queryList(Map<String, Object> map){
		return nxOrderTemplateItemDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxOrderTemplateItemDao.queryTotal(map);
	}


	@Override
	public void save(NxOrderTemplateItemEntity nxOrderTemplateItem){

		Integer nxOtDisGoodsId = nxOrderTemplateItem.getNxOtDisGoodsId();
		Integer nxOtOrderTemplateId = nxOrderTemplateItem.getNxOtOrderTemplateId();

		Map<String, Object> map = new HashMap<>();
		map.put("nxOtDisGoodsId", nxOtDisGoodsId);
		map.put("nxOtTemplateId", nxOtOrderTemplateId);
		NxOrderTemplateItemEntity entity = nxOrderTemplateItemDao.queryDisGoods(map);
		if(entity != null){
			nxOrderTemplateItemDao.update(nxOrderTemplateItem);
		}else{
			nxOrderTemplateItemDao.save(nxOrderTemplateItem);
			NxOrderTemplateEntity nxOrderTemplateEntity = nxOrderTemplateDao.queryObject(nxOtOrderTemplateId);
			nxOrderTemplateEntity.setNxOdItemAmount(nxOrderTemplateEntity.getNxOdItemAmount() + 1);
			nxOrderTemplateDao.update(nxOrderTemplateEntity);
		}
	}
	
	@Override
	public void update(NxOrderTemplateItemEntity nxOrderTemplateItem){
		nxOrderTemplateItemDao.update(nxOrderTemplateItem);
	}
	
	@Override
	public void delete(Integer nxOtItemId){
		nxOrderTemplateItemDao.delete(nxOtItemId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOtItemIds){
		nxOrderTemplateItemDao.deleteBatch(nxOtItemIds);
	}

    @Override
    public List<NxOrderTemplateItemEntity> queryUserItem(Map<String, Object> map1) {
        return nxOrderTemplateItemDao.queryUserItem(map1);
    }

    @Override
    public List<NxOrderTemplateItemEntity> queryCustomerUserItems(Integer nxCustomerUserId) {
        return nxOrderTemplateItemDao.queryCustomerUserItems(nxCustomerUserId);
    }

}
