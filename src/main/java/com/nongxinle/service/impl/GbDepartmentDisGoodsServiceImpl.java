package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepartmentDisGoodsDao;
import com.nongxinle.service.GbDepartmentDisGoodsService;



@Service("gbDepartmentDisGoodsService")
public class GbDepartmentDisGoodsServiceImpl implements GbDepartmentDisGoodsService {
	@Autowired
	private GbDepartmentDisGoodsDao gbDepartmentDisGoodsDao;
	
	@Override
	public GbDepartmentDisGoodsEntity queryObject(Integer gbDepartmentDisGoodsId){
		return gbDepartmentDisGoodsDao.queryObject(gbDepartmentDisGoodsId);
	}
	
	@Override
	public List<GbDepartmentDisGoodsEntity> queryList(Map<String, Object> map){
		return gbDepartmentDisGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentDisGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentDisGoodsEntity gbDepartmentDisGoods){
		gbDepartmentDisGoodsDao.save(gbDepartmentDisGoods);
	}
	
	@Override
	public void update(GbDepartmentDisGoodsEntity gbDepartmentDisGoods){
		gbDepartmentDisGoodsDao.update(gbDepartmentDisGoods);
	}
	
	@Override
	public void delete(Integer gbDepartmentDisGoodsId){
		gbDepartmentDisGoodsDao.delete(gbDepartmentDisGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentDisGoodsIds){
		gbDepartmentDisGoodsDao.deleteBatch(gbDepartmentDisGoodsIds);
	}

    @Override
    public List<GbDepartmentDisGoodsEntity> queryGbDepDisGoodsByParams(Map<String, Object> map) {

		return gbDepartmentDisGoodsDao.queryGbDepDisGoodsByParams(map);
    }



    @Override
    public List<GbDistributerFatherGoodsEntity> depQueryDepGoodsWithOrderGb(Map<String, Object> map) {
        return  gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderGb(map);
    }

	@Override
	public List<GbDepartmentDisGoodsEntity> depGetDepsGoodsGb(Map<String, Object> map) {
		return gbDepartmentDisGoodsDao.depGetDepsGoodsGb(map);
	}

    @Override
    public int queryGbDisGoodsTotal(Map<String, Object> map3) {

		return gbDepartmentDisGoodsDao.queryGbDisGoodsTotal(map3);
    }

    @Override
    public TreeSet<GbDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStrGb(Map<String, Object> map1) {
        return gbDepartmentDisGoodsDao.queryDepDisGoodsQuickSearchStrGb(map1);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> disGetDepDisGoodsCataGb(Integer depFatherId) {

		return gbDepartmentDisGoodsDao.disGetDepDisGoodsCataGb(depFatherId);
    }

    @Override
    public GbDepartmentDisGoodsEntity queryDepGoodsItemByParams(Map<String, Object> map1) {

		return gbDepartmentDisGoodsDao.queryDepGoodsItemByParams(map1);
    }

    @Override
    public List<GbDepartmentDisGoodsEntity> depQueryDepGoodsWithOrderDepGoods(Map<String, Object> map) {

		return gbDepartmentDisGoodsDao.depQueryDepGoodsWithOrderDepGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDepTypeFatherGoods(Map<String, Object> mapD) {

		return gbDepartmentDisGoodsDao.queryDepTypeFatherGoods(mapD);
    }




}
