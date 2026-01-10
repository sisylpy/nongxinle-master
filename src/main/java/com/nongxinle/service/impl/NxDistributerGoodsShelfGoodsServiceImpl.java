package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfGoodsDao;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;



@Service("nxDistributerGoodsShelfGoodsService")
public class NxDistributerGoodsShelfGoodsServiceImpl implements NxDistributerGoodsShelfGoodsService {
	@Autowired
	private NxDistributerGoodsShelfGoodsDao nxDistributerGoodsShelfGoodsDao;
	
	@Override
	public NxDistributerGoodsShelfGoodsEntity queryObject(Integer nxDistributerGoodsShelfGoodsId){
		return nxDistributerGoodsShelfGoodsDao.queryObject(nxDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods){
		nxDistributerGoodsShelfGoodsDao.save(nxDistributerGoodsShelfGoods);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods){
		nxDistributerGoodsShelfGoodsDao.update(nxDistributerGoodsShelfGoods);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfGoodsId){
		nxDistributerGoodsShelfGoodsDao.delete(nxDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfGoodsIds){
		nxDistributerGoodsShelfGoodsDao.deleteBatch(nxDistributerGoodsShelfGoodsIds);
	}

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsByParams(Map<String, Object> map) {

		return nxDistributerGoodsShelfGoodsDao.queryShelfForGoodsByParams(map);
    }

    @Override
    public int queryShelfForGoodsCount(Map<String, Object> map) {

		return nxDistributerGoodsShelfGoodsDao.queryShelfForGoodsCount(map);
    }

    @Override
    public int queryShelfGoodsCount(Map<String, Object> map) {

		return nxDistributerGoodsShelfGoodsDao.queryShelfGoodsCount(map);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsWithOrders(Map<String, Object> pageParams) {

		return nxDistributerGoodsShelfGoodsDao.queryShelfForGoodsWithOrders(pageParams);
    }

    @Override
    public NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsId(Integer nxDpgDisGoodsId) {

		return nxDistributerGoodsShelfGoodsDao.queryShlefGoodsByGoodsId(nxDpgDisGoodsId);
    }

    @Override
    public NxDistributerGoodsShelfGoodsEntity queryShlefGoodsByGoodsIdAndShelfId(Integer disGoodsId, Integer shelfId) {
		return nxDistributerGoodsShelfGoodsDao.queryShlefGoodsByGoodsIdAndShelfId(disGoodsId, shelfId);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsBasic(Integer shelfId) {

        return nxDistributerGoodsShelfGoodsDao.queryShelfGoodsBasic(shelfId);
    }

    @Override
    public void updateShelfLayer(Integer id, Integer layer) {

        nxDistributerGoodsShelfGoodsDao.updateShelfLayer(id, layer);
    }

    @Override
    public void updateDuplicateFlagForGoods(Integer disGoodsId) {
        nxDistributerGoodsShelfGoodsDao.updateDuplicateFlagForGoods(disGoodsId);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryUnInventoriedShelfGoods(Map<String, Object> map) {
        return nxDistributerGoodsShelfGoodsDao.queryUnInventoriedShelfGoods(map);
    }

    @Override
    public Integer queryUnInventoriedShelfGoodsCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfGoodsDao.queryUnInventoriedShelfGoodsCount(map);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryShelfForGoodsWithTraceReportByParams(Map<String, Object> map) {
        return nxDistributerGoodsShelfGoodsDao.queryShelfForGoodsWithTraceReportByParams(map);
    }

    @Override
    public int queryShelfForGoodsWithTraceReportCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfGoodsDao.queryShelfForGoodsWithTraceReportCount(map);
    }


}
