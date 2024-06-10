package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.QyNxDisCorpDao;
import com.nongxinle.entity.QyNxDisCorpEntity;
import com.nongxinle.service.QyNxDisCorpService;



@Service("qyNxDisCorpService")
public class QyNxDisCorpServiceImpl implements QyNxDisCorpService {
	@Autowired
	private QyNxDisCorpDao qyNxDisCorpDao;
	
	@Override
	public QyNxDisCorpEntity queryObject(Integer qyNxDisCorpId){
		return qyNxDisCorpDao.queryObject(qyNxDisCorpId);
	}
	
	@Override
	public List<QyNxDisCorpEntity> queryList(Map<String, Object> map){
		return qyNxDisCorpDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return qyNxDisCorpDao.queryTotal(map);
	}
	
	@Override
	public void save(QyNxDisCorpEntity qyNxDisCorp){
		qyNxDisCorpDao.save(qyNxDisCorp);
	}
	
	@Override
	public void update(QyNxDisCorpEntity qyNxDisCorp){
		qyNxDisCorpDao.update(qyNxDisCorp);
	}
	
	@Override
	public void delete(Integer qyNxDisCorpId){
		qyNxDisCorpDao.delete(qyNxDisCorpId);
	}
	
	@Override
	public void deleteBatch(Integer[] qyNxDisCorpIds){
		qyNxDisCorpDao.deleteBatch(qyNxDisCorpIds);
	}

    @Override
    public QyNxDisCorpEntity queryQyCropByCropId(String corpId) {

		return qyNxDisCorpDao.queryQyCropByCropId(corpId);
    }

    @Override
    public void deleteCropByCropId(String corpid) {
        qyNxDisCorpDao.deleteCrop(corpid);
    }

}
