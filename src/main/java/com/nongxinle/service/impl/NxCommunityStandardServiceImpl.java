package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityStandardDao;
import com.nongxinle.entity.NxCommunityStandardEntity;
import com.nongxinle.service.NxCommunityStandardService;



@Service("nxCommunityStandardService")
public class NxCommunityStandardServiceImpl implements NxCommunityStandardService {
	@Autowired
	private NxCommunityStandardDao nxCommunityStandardDao;
	
	@Override
	public NxCommunityStandardEntity queryObject(Integer nxDistributerStandardId){
		return nxCommunityStandardDao.queryObject(nxDistributerStandardId);
	}
	
	@Override
	public List<NxCommunityStandardEntity> queryList(Map<String, Object> map){
		return nxCommunityStandardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityStandardDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityStandardEntity nxCommunityStandard){
		nxCommunityStandardDao.save(nxCommunityStandard);
	}
	
	@Override
	public void update(NxCommunityStandardEntity nxCommunityStandard){
		nxCommunityStandardDao.update(nxCommunityStandard);
	}
	
	@Override
	public void delete(Integer nxCommunityStandardId){
		nxCommunityStandardDao.delete(nxCommunityStandardId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityStandardIds){
		nxCommunityStandardDao.deleteBatch(nxCommunityStandardIds);
	}

    @Override
    public List<NxCommunityStandardEntity> queryComGoodsStandards(Integer comGoodsId) {

		return nxCommunityStandardDao.queryComGoodsStandards(comGoodsId);
    }

}
