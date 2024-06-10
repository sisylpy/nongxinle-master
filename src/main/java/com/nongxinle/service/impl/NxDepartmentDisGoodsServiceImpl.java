package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.NxDepartmentDisGoodsDao;
import com.nongxinle.service.NxDepartmentDisGoodsService;



@Service("nxDepartmentDisGoodsService")
public class NxDepartmentDisGoodsServiceImpl implements NxDepartmentDisGoodsService {
	@Autowired
	private NxDepartmentDisGoodsDao nxDepartmentDisGoodsDao;


	@Override
	public List<NxDepartmentEntity> queryDepartmentsByDisGoodsId(Integer disGoodsId) {
		return nxDepartmentDisGoodsDao.queryDepartmentsByDisGoodsId(disGoodsId);
	}

	@Override
	public List<NxDistributerFatherGoodsEntity> depGetDepDisGoodsCata(Integer depId) {
		return nxDepartmentDisGoodsDao.depGetDepDisGoodsCata(depId);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepGoodsByFatherId(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepGoodsByFatherId(map);
	}
	@Override
	public List<NxDepartmentDisGoodsEntity> queryAddDisDepGoods(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDisDepGoods(map);
	}

    @Override
    public int queryDepGoodsTotal(Map<String, Object> map3) {
		return nxDepartmentDisGoodsDao.queryDisGoodsTotal(map3);
    }

	@Override
	public void save(NxDepartmentDisGoodsEntity nxDepartmentDisGoods){
		nxDepartmentDisGoodsDao.save(nxDepartmentDisGoods);
	}
	@Override
	public void update(NxDepartmentDisGoodsEntity nxDepartmentDisGoods){
		nxDepartmentDisGoodsDao.update(nxDepartmentDisGoods);
	}
	@Override
	public NxDepartmentDisGoodsEntity queryObject(Integer nxDepartmentDisGoodsId){
		return nxDepartmentDisGoodsDao.queryObject(nxDepartmentDisGoodsId);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepDisSearchPinyin(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepDisSearchPinyin(map);
	}



	@Override
	public TreeSet<NxDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStr(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsQuickSearchStr (map);
	}

	@Override
	public void delete(Integer nxDepartmentDisGoodsId){
		nxDepartmentDisGoodsDao.delete(nxDepartmentDisGoodsId);
	}

	@Override
	public List<NxDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map) {
		return nxDepartmentDisGoodsDao.queryDepDisGoodsByParams(map);
	}

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetDepDisGoodsCata(Integer depFatherId) {

		return nxDepartmentDisGoodsDao.disGetDepGoodsCata(depFatherId);
    }

	@Override
	public void deleteBatch(Integer[] nxDepartmentIds){
		nxDepartmentDisGoodsDao.deleteBatch(nxDepartmentIds);
	}

    @Override
    public List<NxDistributerFatherGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depQueryDepGoodsWithOrder(map);
    }

    @Override
    public NxDepartmentEntity depFatherGetSubDepsGoods(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depFatherGetSubDepsGoods(map);
    }

    @Override
    public List<NxDepartmentDisGoodsEntity> depGetDepsGoods(Map<String, Object> map) {

		return nxDepartmentDisGoodsDao.depGetDepsGoods(map);
    }

    @Override
    public List<GbDepartmentEntity> queryGbDepartmentsByDisGoodsId(Integer disGoodsId) {

		return nxDepartmentDisGoodsDao.queryGbDepartmentsByDisGoodsId(disGoodsId);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDepDisGoodsWithOrders(Integer depFatherId) {

		return nxDepartmentDisGoodsDao.queryDepDisGoodsWithOrders(depFatherId);
    }

    @Override
    public NxDepartmentDisGoodsEntity queryDepartmentGoods(Map<String, Object> mapDep) {

		return nxDepartmentDisGoodsDao.queryDepartmentGoods(mapDep);
    }


}
