package com.nongxinle.service.impl;

import com.nongxinle.entity.NxLabelEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxLabelTypeDao;
import com.nongxinle.entity.NxLabelTypeEntity;
import com.nongxinle.service.NxLabelTypeService;



@Service("nxLabelTypeService")
public class NxLabelTypeServiceImpl implements NxLabelTypeService {
	@Autowired
	private NxLabelTypeDao nxLabelTypeDao;
	
	@Override
	public NxLabelTypeEntity queryObject(Integer nxLabelTypeId){
		return nxLabelTypeDao.queryObject(nxLabelTypeId);
	}
	
	@Override
	public List<NxLabelTypeEntity> queryList(Map<String, Object> map){
		return nxLabelTypeDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxLabelTypeDao.queryTotal(map);
	}
	
	@Override
	public void save(NxLabelTypeEntity nxLabelType){
		nxLabelTypeDao.save(nxLabelType);
	}
	
	@Override
	public void update(NxLabelTypeEntity nxLabelType){
		nxLabelTypeDao.update(nxLabelType);
	}
	
	@Override
	public void delete(Integer nxLabelTypeId){
		nxLabelTypeDao.delete(nxLabelTypeId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxLabelTypeIds){
		nxLabelTypeDao.deleteBatch(nxLabelTypeIds);
	}

    @Override
    public List<NxLabelEntity> queryAllLabel() {
        return nxLabelTypeDao.queryAllLabel();
    }

}
