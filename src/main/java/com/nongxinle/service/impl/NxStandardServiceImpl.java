package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxStandardDao;
import com.nongxinle.entity.NxStandardEntity;
import com.nongxinle.service.NxStandardService;



@Service("nxStandardService")
public class NxStandardServiceImpl implements NxStandardService {
	@Autowired
	private NxStandardDao nxStandardDao;
	
	@Override
	public NxStandardEntity queryObject(Integer nxStandardId){
		return nxStandardDao.queryObject(nxStandardId);
	}
	
	@Override
	public List<NxStandardEntity> queryList(Map<String, Object> map){
		return nxStandardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxStandardDao.queryTotal(map);
	}
	
	@Override
	public void save(NxStandardEntity nxStandard){
		nxStandardDao.save(nxStandard);
	}
	
	@Override
	public void update(NxStandardEntity nxStandard){
		nxStandardDao.update(nxStandard);
	}
	
	@Override
	public void delete(Integer nxStandardId){
		nxStandardDao.delete(nxStandardId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxStandardIds){
		nxStandardDao.deleteBatch(nxStandardIds);
	}

    @Override
    public List<NxStandardEntity> queryGoodsStandardListByGoodId(Integer nxGoodsId) {

		return nxStandardDao.queryGoodsStandardListByGoodsId(nxGoodsId);

    }

    @Override
    public List<NxStandardEntity> queryList(Integer nxGoodsId) {
        return nxStandardDao.queryList(nxGoodsId);
    }

}
