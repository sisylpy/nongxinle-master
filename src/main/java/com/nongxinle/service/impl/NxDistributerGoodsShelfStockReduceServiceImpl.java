package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfStockReduceDao;
import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;
import com.nongxinle.service.NxDistributerGoodsShelfStockReduceService;



@Service("nxDistributerGoodsShelfStockReduceService")
public class NxDistributerGoodsShelfStockReduceServiceImpl implements NxDistributerGoodsShelfStockReduceService {
	@Autowired
	private NxDistributerGoodsShelfStockReduceDao nxDistributerGoodsShelfStockReduceDao;
	
	@Override
	public NxDistributerGoodsShelfStockReduceEntity queryObject(Integer nxDistributerGoodsShelfStockReduceId){
		return nxDistributerGoodsShelfStockReduceDao.queryObject(nxDistributerGoodsShelfStockReduceId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfStockReduceEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfStockReduceDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfStockReduceDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce){
		nxDistributerGoodsShelfStockReduceDao.save(nxDistributerGoodsShelfStockReduce);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfStockReduceEntity nxDistributerGoodsShelfStockReduce){
		nxDistributerGoodsShelfStockReduceDao.update(nxDistributerGoodsShelfStockReduce);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfStockReduceId){
		nxDistributerGoodsShelfStockReduceDao.delete(nxDistributerGoodsShelfStockReduceId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfStockReduceIds){
		nxDistributerGoodsShelfStockReduceDao.deleteBatch(nxDistributerGoodsShelfStockReduceIds);
	}

    @Override
    public List<NxDistributerGoodsShelfStockReduceEntity> queryReduceListByParams(Map<String, Object> map) {

		return nxDistributerGoodsShelfStockReduceDao.queryReduceListByParams(map);

    }

}
