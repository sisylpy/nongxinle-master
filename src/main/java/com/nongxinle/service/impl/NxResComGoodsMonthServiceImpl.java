package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxResComGoodsMonthDao;
import com.nongxinle.entity.NxResComGoodsMonthEntity;
import com.nongxinle.service.NxResComGoodsMonthService;



@Service("nxResComGoodsMonthService")
public class NxResComGoodsMonthServiceImpl implements NxResComGoodsMonthService {
	@Autowired
	private NxResComGoodsMonthDao nxResComGoodsMonthDao;
	
	@Override
	public NxResComGoodsMonthEntity queryObject(Integer nxResComGoodsMonthId){
		return nxResComGoodsMonthDao.queryObject(nxResComGoodsMonthId);
	}
	
	@Override
	public List<NxResComGoodsMonthEntity> queryList(Map<String, Object> map){
		return nxResComGoodsMonthDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxResComGoodsMonthDao.queryTotal(map);
	}
	
	@Override
	public void save(NxResComGoodsMonthEntity nxResComGoodsMonth){
		nxResComGoodsMonthDao.save(nxResComGoodsMonth);
	}
	
	@Override
	public void update(NxResComGoodsMonthEntity nxResComGoodsMonth){
		nxResComGoodsMonthDao.update(nxResComGoodsMonth);
	}
	
	@Override
	public void delete(Integer nxResComGoodsMonthId){
		nxResComGoodsMonthDao.delete(nxResComGoodsMonthId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxResComGoodsMonthIds){
		nxResComGoodsMonthDao.deleteBatch(nxResComGoodsMonthIds);
	}

    @Override
    public NxResComGoodsMonthEntity queryMonthGoodsByParams(Map<String, Object> map3) {

		return nxResComGoodsMonthDao.queryMonthGoodsByParams(map3);
    }

}
