package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxResComGoodsWeekDao;
import com.nongxinle.entity.NxResComGoodsWeekEntity;
import com.nongxinle.service.NxResComGoodsWeekService;



@Service("nxResComGoodsWeekService")
public class NxResComGoodsWeekServiceImpl implements NxResComGoodsWeekService {
	@Autowired
	private NxResComGoodsWeekDao nxResComGoodsWeekDao;
	
	@Override
	public NxResComGoodsWeekEntity queryObject(Integer nxResComGoodsWeekId){
		return nxResComGoodsWeekDao.queryObject(nxResComGoodsWeekId);
	}
	
	@Override
	public List<NxResComGoodsWeekEntity> queryList(Map<String, Object> map){
		return nxResComGoodsWeekDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxResComGoodsWeekDao.queryTotal(map);
	}
	
	@Override
	public void save(NxResComGoodsWeekEntity nxResComGoodsWeek){
		nxResComGoodsWeekDao.save(nxResComGoodsWeek);
	}
	
	@Override
	public void update(NxResComGoodsWeekEntity nxResComGoodsWeek){
		nxResComGoodsWeekDao.update(nxResComGoodsWeek);
	}
	
	@Override
	public void delete(Integer nxResComGoodsWeekId){
		nxResComGoodsWeekDao.delete(nxResComGoodsWeekId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxResComGoodsWeekIds){
		nxResComGoodsWeekDao.deleteBatch(nxResComGoodsWeekIds);
	}

    @Override
    public NxResComGoodsWeekEntity queryWeekGoodsByParams(Map<String, Object> map2) {

		return nxResComGoodsWeekDao.queryWeekGoodsByParams(map2);
    }

}
