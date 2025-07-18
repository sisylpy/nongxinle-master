package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerPayListDao;
import com.nongxinle.entity.NxDistributerPayListEntity;
import com.nongxinle.service.NxDistributerPayListService;



@Service("nxDistributerPayListService")
public class NxDistributerPayListServiceImpl implements NxDistributerPayListService {
	@Autowired
	private NxDistributerPayListDao nxDistributerPayListDao;
	
	@Override
	public NxDistributerPayListEntity queryObject(Integer nxDistributerPayListId){
		return nxDistributerPayListDao.queryObject(nxDistributerPayListId);
	}
	
	@Override
	public List<NxDistributerPayListEntity> queryList(Map<String, Object> map){
		return nxDistributerPayListDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerPayListDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerPayListEntity nxDistributerPayList){
		nxDistributerPayListDao.save(nxDistributerPayList);
	}
	
	@Override
	public void update(NxDistributerPayListEntity nxDistributerPayList){
		nxDistributerPayListDao.update(nxDistributerPayList);
	}
	
	@Override
	public void delete(Integer nxDistributerPayListId){
		nxDistributerPayListDao.delete(nxDistributerPayListId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerPayListIds){
		nxDistributerPayListDao.deleteBatch(nxDistributerPayListIds);
	}

    @Override
    public double queryDisPayListSubtotal(Map<String, Object> map) {

		return nxDistributerPayListDao.queryDisPayListSubtotal(map);
    }

	@Override
	public int queryDisPayListCount(Map<String, Object> map) {

		return nxDistributerPayListDao.queryDisPayListCount(map);


	}

    @Override
    public List<NxDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map) {

		return nxDistributerPayListDao.queryPayListListByParams(map);
    }

    @Override
    public int queryDepRecordSecondsTotal(Map<String, Object> map) {

		return nxDistributerPayListDao.queryDepRecordSecondsTotal(map);
    }

}
