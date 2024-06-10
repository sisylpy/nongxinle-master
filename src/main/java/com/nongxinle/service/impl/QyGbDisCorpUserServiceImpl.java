package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.QyGbDisCorpUserDao;
import com.nongxinle.entity.QyGbDisCorpUserEntity;
import com.nongxinle.service.QyGbDisCorpUserService;



@Service("qyGbDisCorpUserService")
public class QyGbDisCorpUserServiceImpl implements QyGbDisCorpUserService {
	@Autowired
	private QyGbDisCorpUserDao qyGbDisCorpUserDao;
	
	@Override
	public QyGbDisCorpUserEntity queryObject(Integer qyGbDisCorpUserId){
		return qyGbDisCorpUserDao.queryObject(qyGbDisCorpUserId);
	}
	
	@Override
	public List<QyGbDisCorpUserEntity> queryList(Map<String, Object> map){
		return qyGbDisCorpUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return qyGbDisCorpUserDao.queryTotal(map);
	}
	
	@Override
	public void save(QyGbDisCorpUserEntity qyGbDisCorpUser){
		qyGbDisCorpUserDao.save(qyGbDisCorpUser);
	}
	
	@Override
	public void update(QyGbDisCorpUserEntity qyGbDisCorpUser){
		qyGbDisCorpUserDao.update(qyGbDisCorpUser);
	}
	
	@Override
	public void delete(Integer qyGbDisCorpUserId){
		qyGbDisCorpUserDao.delete(qyGbDisCorpUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] qyGbDisCorpUserIds){
		qyGbDisCorpUserDao.deleteBatch(qyGbDisCorpUserIds);
	}

    @Override
    public List<QyGbDisCorpUserEntity> queryCorpUserListByCorpId(Integer qyNxDisCorpId) {

		return qyGbDisCorpUserDao.queryCorpUserListByCorpId(qyNxDisCorpId);
    }

}
