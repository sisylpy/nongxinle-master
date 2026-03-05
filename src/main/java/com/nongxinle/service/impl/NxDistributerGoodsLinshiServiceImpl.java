package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsLinshiDao;
import com.nongxinle.entity.NxDistributerGoodsLinshiEntity;
import com.nongxinle.service.NxDistributerGoodsLinshiService;



@Service("nxDistributerGoodsLinshiService")
public class NxDistributerGoodsLinshiServiceImpl implements NxDistributerGoodsLinshiService {
	@Autowired
	private NxDistributerGoodsLinshiDao nxDistributerGoodsLinshiDao;
	
	@Override
	public NxDistributerGoodsLinshiEntity queryObject(Integer nxDistributerGoodsLsId){
		return nxDistributerGoodsLinshiDao.queryObject(nxDistributerGoodsLsId);
	}
	
	@Override
	public List<NxDistributerGoodsLinshiEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsLinshiDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsLinshiDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi){
		nxDistributerGoodsLinshiDao.save(nxDistributerGoodsLinshi);
	}
	
	@Override
	public void update(NxDistributerGoodsLinshiEntity nxDistributerGoodsLinshi){
		nxDistributerGoodsLinshiDao.update(nxDistributerGoodsLinshi);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsLsId){
		nxDistributerGoodsLinshiDao.delete(nxDistributerGoodsLsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsLsIds){
		nxDistributerGoodsLinshiDao.deleteBatch(nxDistributerGoodsLsIds);
	}

    @Override
    public List<NxDistributerGoodsLinshiEntity> disGetLinshiGoodsList(Map<String, Object> map) {

		return nxDistributerGoodsLinshiDao.disGetLinshiGoodsList(map);
    }

	@Override
	public int disGetLinshiGoodsTotal(Map<String, Object> map) {
		return nxDistributerGoodsLinshiDao.disGetLinshiGoodsTotal(map);
	}

	@Override
	public NxDistributerGoodsLinshiEntity queryLinshiByFromGoodsId(Integer fromGoodsId) {
		return nxDistributerGoodsLinshiDao.queryLinshiByFromGoodsId(fromGoodsId);
	}

	@Override
	public List<NxDistributerGoodsLinshiEntity> queryLinshiListByStatus(Map<String, Object> map) {
		return nxDistributerGoodsLinshiDao.queryLinshiListByStatus(map);
	}

	@Override
	public List<Integer> queryFromGoodsIdsByDisId(Integer disId) {
		return nxDistributerGoodsLinshiDao.queryFromGoodsIdsByDisId(disId);
	}

}
