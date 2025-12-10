package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfStockDao;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;



@Service("nxDistributerGoodsShelfStockService")
public class NxDistributerGoodsShelfStockServiceImpl implements NxDistributerGoodsShelfStockService {
	@Autowired
	private NxDistributerGoodsShelfStockDao nxDistributerGoodsShelfStockDao;
	
	@Override
	public NxDistributerGoodsShelfStockEntity queryObject(Integer nxDistributerGoodsShelfStockId){
		return nxDistributerGoodsShelfStockDao.queryObject(nxDistributerGoodsShelfStockId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfStockEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfStockDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfStockDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockDao.save(nxDistributerGoodsShelfStock);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStock){
		nxDistributerGoodsShelfStockDao.update(nxDistributerGoodsShelfStock);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfStockId){
		nxDistributerGoodsShelfStockDao.delete(nxDistributerGoodsShelfStockId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfStockIds){
		nxDistributerGoodsShelfStockDao.deleteBatch(nxDistributerGoodsShelfStockIds);
	}

    @Override
    public List<NxDistributerGoodsShelfStockEntity> queryShelfStockListByParams(Map<String, Object> map) {

	return nxDistributerGoodsShelfStockDao.queryShelfStockListByParams(map);
    }

    @Override
    public Integer queryShelfStockCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryShelfStockCount(map);
    }

    @Override
    public Double queryShelfStockRestTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryShelfStockRestTotal(map);
    }

    @Override
    public Double queryShelfStockRestWeightTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryShelfStockRestWeightTotal(map);
    }

    @Override
    public Integer queryStockGoodsCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryStockGoodsCount(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryGoodsStockList(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryGoodsStockList(map);
    }

    @Override
    public Integer queryGoodsStockCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryGoodsStockCount(map);
    }

    @Override
    public List<NxDistributerGoodsShelfStockEntity> queryStockByDisGoodsIdAndDate(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryStockByDisGoodsIdAndDate(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryStockTreeFatherGoodsByParams(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryStockTreeFatherGoodsByParams(map);
    }

    @Override
    public List<NxDistributerGoodsShelfStockEntity> queryStockListByDepFatherId(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryStockListByDepFatherId(map);
    }

    @Override
    public Integer queryStockCountByDepFatherId(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockDao.queryStockCountByDepFatherId(map);
    }

}
