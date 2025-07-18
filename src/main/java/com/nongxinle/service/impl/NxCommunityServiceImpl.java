package com.nongxinle.service.impl;

import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.service.NxECommerceCommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityDao;
import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.service.NxCommunityService;



@Service("nxCommunityService")
public class NxCommunityServiceImpl implements NxCommunityService {
	@Autowired
	private NxCommunityDao nxCommunityDao;
	@Autowired
	private NxECommerceCommunityService nxECommerceCommunityService;
	
	@Override
	public NxCommunityEntity queryObject(Integer nxCommunityId){
		return nxCommunityDao.queryObject(nxCommunityId);
	}
	
	@Override
	public List<NxCommunityEntity> queryList(Map<String, Object> map){
		return nxCommunityDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityEntity nxCommunity){
		nxCommunityDao.save(nxCommunity);
	}
	
	@Override
	public void update(NxCommunityEntity nxCommunity){
		nxCommunityDao.update(nxCommunity);
	}
	
	@Override
	public void delete(Integer nxCommunityId){
		nxCommunityDao.delete(nxCommunityId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityIds){
		nxCommunityDao.deleteBatch(nxCommunityIds);
	}

	@Override
	public NxCommunityEntity saveWithEcommerce(NxCommunityEntity nxCommunity) {

		nxCommunity.setNxCommunityOpenTime("06:00");
		nxCommunity.setNxCommunityCloseTime("22:00");
		 nxCommunityDao.save(nxCommunity);

		System.out.println("abcckckkckckc" + nxCommunity.getNxCommunityCommerceId());
		System.out.println("abcckckkckckc" + nxCommunity.getNxCommunityId());

		NxECommerceCommunityEntity entity = new NxECommerceCommunityEntity();
		entity.setNxEccEId(nxCommunity.getNxCommunityCommerceId());
		entity.setNxEccCommunityId(nxCommunity.getNxCommunityId());
		nxECommerceCommunityService.save(entity);


		return nxCommunity;

	}

    @Override
    public NxECommerceEntity queryCommunityByECommerceId(Integer id) {

		return nxCommunityDao.queryCommunityByECommerceId(id);
    }

    @Override
    public List<NxCommunityEntity> queryCommunityListByUserPoint(String nxCuaLocation) {

		return nxCommunityDao.queryCommunityListByUserPoint(nxCuaLocation);
    }


}
