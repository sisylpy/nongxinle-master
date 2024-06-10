package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxGoodsDao;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.NxGoodsService;



@Service("nxGoodsService")
public class NxGoodsServiceImpl implements NxGoodsService {
	@Autowired
	private NxGoodsDao nxGoodsDao;

	@Override
	public List<NxGoodsEntity> queryQuickSearchNxGoods(Map<String, Object> map) {

		return nxGoodsDao.queryQuickSearchNxGoods(map);
	}
	@Override
	public List<NxGoodsEntity> queryQuickSearchNxCategoryGoods(Map<String, Object> map) {
		return nxGoodsDao.queryQuickSearchNxCategoryGoods(map);
	}

	@Override
	public List<NxGoodsEntity> queryNxGoodsByParams(Map<String, Object> map) {
		return nxGoodsDao.queryNxGoodsByParams(map);
	}

	@Override
	public NxGoodsEntity queryObject(Integer nxGoodsId){
		return nxGoodsDao.queryObject(nxGoodsId);
	}
	

	@Override
	public int queryTotal(Map<String, Object> map){
		return nxGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxGoodsEntity nxGoods){
		nxGoodsDao.save(nxGoods);
	}
	
	@Override
	public void update(NxGoodsEntity nxGoods){
		nxGoodsDao.update(nxGoods);
	}
	
	@Override
	public void delete(Integer nxGoodsId){
		nxGoodsDao.delete(nxGoodsId);
	}
	

	@Override
	public List<NxGoodsEntity> getiBookCoverData() {
		return nxGoodsDao.getNxGoodsCateList();
	}

    @Override
    public List<NxGoodsEntity> getAllFatherGoods(Integer fatherId) {
        return nxGoodsDao.getNxFatherGoodsByFatherId(fatherId);
    }

	@Override
	public List<NxGoodsEntity> queryListWithFatherId(Map<String, Object> map) {
		return nxGoodsDao.queryListWithFatherId(map);
	}

	@Override
	public int queryTotalByFatherId(Integer fatherId) {
		return nxGoodsDao.queryTotalByFatherId(fatherId);
	}

	@Override
	public List<NxGoodsEntity> querySubNameByFatherId(Integer nxGoodsId) {
		return nxGoodsDao.querySubNameByFatherId(nxGoodsId);

	}

    @Override
    public List<NxGoodsEntity> queryGoodsCataByType(Integer type) {
        return nxGoodsDao.queryGoodsCataByType(type);
    }


	@Override
	public List<NxGoodsEntity> queryGoodsTree() {
		return nxGoodsDao.queryGoodsTree();
	}


	@Override
	public List<NxGoodsEntity> queryIfHasSameGoods(Map<String, Object> map) {
		return nxGoodsDao.queryIfHasSameGoods(map);
	}

	@Override
	public List<NxGoodsEntity> queryNxGoodsOrderByGoodsId(Map<String, Object> map) {
		return nxGoodsDao.queryNxGoodsOrderByGoodsId(map);
	}

    @Override
    public List<NxGoodsEntity> queryNxFatherGoods() {

		return nxGoodsDao.queryNxFatherGoods();
    }

    @Override
    public List<NxGoodsEntity> queryQuickSearchAllGoodsWithNxDis(Map<String, Object> map) {

		return nxGoodsDao.queryQuickSearchAllGoodsWithNxDis(map);
    }

    @Override
    public List<NxGoodsEntity> queryQuickSearchFatherGoods(Map<String, Object> map) {

		return nxGoodsDao.queryQuickSearchFatherGoods(map);
    }

    @Override
    public int querySecondLevelMaxId() {

		return nxGoodsDao.querySecondLevelMaxId();
    }

//    @Override
//    public List<NxGoodsEntity> queryCataNxDistribterWithPeisong(Map<String, Object> map) {
//
//		return nxGoodsDao.queryCataNxDistribterWithPeisong(map);
//    }

    @Override
    public List<NxGoodsEntity> queryQuickSearchNxCategoryGoodsWithNxDis(Map<String, Object> map) {
		return nxGoodsDao.queryQuickSearchNxCategoryGoodsWithNxDis(map);
    }


}
