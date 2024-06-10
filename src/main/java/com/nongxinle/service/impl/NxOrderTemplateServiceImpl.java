package com.nongxinle.service.impl;

import com.nongxinle.dao.NxOrderTemplateDao;
import com.nongxinle.entity.NxOrderTemplateEntity;
import com.nongxinle.service.NxOrderTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("nxOrderTemplateService")
public class NxOrderTemplateServiceImpl implements NxOrderTemplateService {
	@Autowired
	private NxOrderTemplateDao nxOrderTemplateDao;
	
	@Override
	public NxOrderTemplateEntity queryObject(Integer nxOrderTemplateId){
		return nxOrderTemplateDao.queryObject(nxOrderTemplateId);
	}
	
	@Override
	public List<NxOrderTemplateEntity> queryList(Map<String, Object> map){
		return nxOrderTemplateDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxOrderTemplateDao.queryTotal(map);
	}
	
	@Override
	public void save(NxOrderTemplateEntity nxOderTemplate){
		nxOrderTemplateDao.save(nxOderTemplate);
	}
	
	@Override
	public void update(NxOrderTemplateEntity nxOderTemplate){
		nxOrderTemplateDao.update(nxOderTemplate);
	}
	
	@Override
	public void delete(Integer nxOrderTemplateId){
		nxOrderTemplateDao.delete(nxOrderTemplateId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOrderTemplateIds){
		nxOrderTemplateDao.deleteBatch(nxOrderTemplateIds);
	}

    @Override
    public NxOrderTemplateEntity queryTemplate(Map<String, Object> map) {
       return   nxOrderTemplateDao.queryTemplate(map);
    }

}
