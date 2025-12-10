package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfStockReduceDao;
import com.nongxinle.entity.NxDistributerGoodsEntity;
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

    @Override
    public Integer queryReduceTypeCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceTypeCount(map);
    }

    @Override
    public Double queryReduceCostSubtotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceCostSubtotal(map);
    }

    @Override
    public Double queryReduceWasteSubtotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceWasteSubtotal(map);
    }

    @Override
    public Double queryReduceLossSubtotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceLossSubtotal(map);
    }

    @Override
    public Double queryReduceProduceSubtotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceProduceSubtotal(map);
    }

    @Override
    public Integer queryReduceGoodsTotalCount(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceGoodsTotalCount(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryGoodsWithReduceRecords(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryGoodsWithReduceRecords(map);
    }

    @Override
    public List<NxDistributerGoodsShelfStockReduceEntity> queryReduceRecordsByDisGoodsIdAndDate(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceRecordsByDisGoodsIdAndDate(map);
    }

    @Override
    public NxDistributerGoodsShelfStockReduceEntity queryStockInventoryRecord(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryStockInventoryRecord(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryStockProduceSubtotalTopTimes(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryStockProduceSubtotalTopTimes(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryStockLossSubtotalTopTimes(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryStockLossSubtotalTopTimes(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryStockWasteSubtotalTopTimes(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryStockWasteSubtotalTopTimes(map);
    }

    @Override
    public List<Map<String, Object>> queryNxPurchaseGoodsTopDay(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryNxPurchaseGoodsTopDay(map);
    }

    @Override
    public Double queryReduceReturnTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceReturnTotal(map);
    }

    @Override
    public Double queryReduceWasteWeightTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceWasteWeightTotal(map);
    }

    @Override
    public Double queryReduceLossWeightTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceLossWeightTotal(map);
    }

    @Override
    public Double queryReduceProduceWeightTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceProduceWeightTotal(map);
    }

    @Override
    public Double queryReduceCostWeightTotal(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceCostWeightTotal(map);
    }

    @Override
    public List<NxDepartmentEntity> queryReduceDepartment(Map<String, Object> map) {
        return nxDistributerGoodsShelfStockReduceDao.queryReduceDepartment(map);
    }

}
