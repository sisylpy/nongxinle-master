package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRestrauntOrdersHistoryDao;
import com.nongxinle.entity.NxRestrauntOrdersHistoryEntity;
import com.nongxinle.service.NxRestrauntOrdersHistoryService;



@Service("nxRestrauntOrdersHistoryService")
public class NxRestrauntOrdersHistoryServiceImpl implements NxRestrauntOrdersHistoryService {
	@Autowired
	private NxRestrauntOrdersHistoryDao nxRestrauntOrdersHistoryDao;
	
	@Override
	public NxRestrauntOrdersHistoryEntity queryObject(Integer nxRestrauntOrdersHistoryId){
		return nxRestrauntOrdersHistoryDao.queryObject(nxRestrauntOrdersHistoryId);
	}
	
	@Override
	public List<NxRestrauntOrdersHistoryEntity> queryList(Map<String, Object> map){
		return nxRestrauntOrdersHistoryDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRestrauntOrdersHistoryDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRestrauntOrdersHistoryEntity nxRestrauntOrdersHistory){
		nxRestrauntOrdersHistoryDao.save(nxRestrauntOrdersHistory);
	}
	
	@Override
	public void update(NxRestrauntOrdersHistoryEntity nxRestrauntOrdersHistory){
		nxRestrauntOrdersHistoryDao.update(nxRestrauntOrdersHistory);
	}
	
	@Override
	public void delete(Integer nxRestrauntOrdersHistoryId){
		nxRestrauntOrdersHistoryDao.delete(nxRestrauntOrdersHistoryId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRestrauntOrdersHistoryIds){
		nxRestrauntOrdersHistoryDao.deleteBatch(nxRestrauntOrdersHistoryIds);
	}

    @Override
    public List<NxRestrauntOrdersHistoryEntity> queryHistoryOrdersByParams(Map<String, Object> map) {

		return nxRestrauntOrdersHistoryDao.queryHistoryOrdersByParams(map);
    }

}
