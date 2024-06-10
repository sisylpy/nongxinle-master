package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.QyGbDisCorpDao;
import com.nongxinle.entity.QyGbDisCorpEntity;
import com.nongxinle.service.QyGbDisCorpService;



@Service("qyGbDisCorpService")
public class QyGbDisCorpServiceImpl implements QyGbDisCorpService {
	@Autowired
	private QyGbDisCorpDao qyGbDisCorpDao;
	
	@Override
	public QyGbDisCorpEntity queryObject(Integer qyGbDisCorpId){
		return qyGbDisCorpDao.queryObject(qyGbDisCorpId);
	}
	
	@Override
	public List<QyGbDisCorpEntity> queryList(Map<String, Object> map){
		return qyGbDisCorpDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return qyGbDisCorpDao.queryTotal(map);
	}
	
	@Override
	public void save(QyGbDisCorpEntity qyGbDisCorp){
		qyGbDisCorpDao.save(qyGbDisCorp);
	}
	
	@Override
	public void update(QyGbDisCorpEntity qyGbDisCorp){
		qyGbDisCorpDao.update(qyGbDisCorp);
	}
	
	@Override
	public void delete(Integer qyGbDisCorpId){
		qyGbDisCorpDao.delete(qyGbDisCorpId);
	}
	
	@Override
	public void deleteBatch(Integer[] qyGbDisCorpIds){
		qyGbDisCorpDao.deleteBatch(qyGbDisCorpIds);
	}

    @Override
    public QyGbDisCorpEntity queryQyCropByCropId(String corpid) {

		return qyGbDisCorpDao.queryQyCropByCropId(corpid);
    }

    @Override
    public void deleteCropByCropId(String corpid) {
         qyGbDisCorpDao.deleteCropByCropId(corpid);
    }


}
