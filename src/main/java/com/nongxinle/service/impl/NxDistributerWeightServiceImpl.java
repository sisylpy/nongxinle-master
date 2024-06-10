package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerWeightDao;
import com.nongxinle.service.NxDistributerWeightService;



@Service("nxDistributerWeightService")
public class NxDistributerWeightServiceImpl implements NxDistributerWeightService {
	@Autowired
	private NxDistributerWeightDao nxDistributerWeightDao;
	
	@Override
	public NxDistributerWeightEntity queryObject(Integer nxDistributerWeightId){
		return nxDistributerWeightDao.queryObject(nxDistributerWeightId);
	}
	
	@Override
	public List<NxDistributerWeightEntity> queryList(Map<String, Object> map){
		return nxDistributerWeightDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerWeightDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerWeightEntity nxDistributerWeight){
		nxDistributerWeightDao.save(nxDistributerWeight);
	}
	
	@Override
	public void update(NxDistributerWeightEntity nxDistributerWeight){
		nxDistributerWeightDao.update(nxDistributerWeight);
	}
	
	@Override
	public void delete(Integer nxDistributerWeightId){
		nxDistributerWeightDao.delete(nxDistributerWeightId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerWeightIds){
		nxDistributerWeightDao.deleteBatch(nxDistributerWeightIds);
	}

    @Override
    public List<NxDistributerWeightEntity> queryWeightListByParams(Map<String, Object> map) {

		return nxDistributerWeightDao.queryWeightListByParams(map);
    }

    @Override
    public NxDistributerWeightEntity queryWeightOrdersById(Integer id) {

		return nxDistributerWeightDao.queryWeightOrdersById(id);
    }

    @Override
    public int queryWeightCountByParams(Map<String, Object> map) {

		return nxDistributerWeightDao.queryWeightCountByParams(map);
    }

    @Override
    public List<NxDistributerPurchaseGoodsEntity> queryWeightGoodsById(Integer id) {

		return nxDistributerWeightDao.queryWeightGoodsById(id);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryFatherGoodsStockOrder(Map<String, Object> map) {

		return nxDistributerWeightDao.queryFatherGoodsStockOrder(map);
    }

    @Override
    public List<NxDepartmentEntity> queryWeightDepOrders(Integer id) {

		return nxDistributerWeightDao.queryWeightDepOrders(id);
    }

}
