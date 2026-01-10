package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxOrderOcrTrainingDataDao;
import com.nongxinle.entity.NxOrderOcrTrainingDataEntity;
import com.nongxinle.service.NxOrderOcrTrainingDataService;



@Service("nxOrderOcrTrainingDataService")
public class NxOrderOcrTrainingDataServiceImpl implements NxOrderOcrTrainingDataService {
	@Autowired
	private NxOrderOcrTrainingDataDao nxOrderOcrTrainingDataDao;
	
	@Override
	public NxOrderOcrTrainingDataEntity queryObject(Integer nxOtdId){
		return nxOrderOcrTrainingDataDao.queryObject(nxOtdId);
	}
	
	@Override
	public List<NxOrderOcrTrainingDataEntity> queryList(Map<String, Object> map){
		return nxOrderOcrTrainingDataDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxOrderOcrTrainingDataDao.queryTotal(map);
	}
	
	@Override
	public void save(NxOrderOcrTrainingDataEntity nxOrderOcrTrainingData){
		nxOrderOcrTrainingDataDao.save(nxOrderOcrTrainingData);
	}
	
	@Override
	public void update(NxOrderOcrTrainingDataEntity nxOrderOcrTrainingData){
		nxOrderOcrTrainingDataDao.update(nxOrderOcrTrainingData);
	}
	
	@Override
	public void delete(Integer nxOtdId){
		nxOrderOcrTrainingDataDao.delete(nxOtdId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOtdIds){
		nxOrderOcrTrainingDataDao.deleteBatch(nxOtdIds);
	}

	@Override
	public NxOrderOcrTrainingDataEntity queryByOrderId(Integer orderId) {
		return nxOrderOcrTrainingDataDao.queryByOrderId(orderId);
	}

	@Override
	public List<NxOrderOcrTrainingDataEntity> queryTrainingDataList(Map<String, Object> map) {
		return nxOrderOcrTrainingDataDao.queryTrainingDataList(map);
	}

	@Override
	public NxOrderOcrTrainingDataEntity queryByMatchFields(Map<String, Object> map) {
		return nxOrderOcrTrainingDataDao.queryByMatchFields(map);
	}

}
