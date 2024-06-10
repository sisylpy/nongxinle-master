package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.QyNxDisCorpUserDao;
import com.nongxinle.entity.QyNxDisCorpUserEntity;
import com.nongxinle.service.QyNxDisCropUserService;



@Service("qyNxDisCropUserService")
public class QyNxDisCropUserServiceImpl implements QyNxDisCropUserService {
	@Autowired
	private QyNxDisCorpUserDao qyNxDisCorpUserDao;
	
	@Override
	public QyNxDisCorpUserEntity queryObject(Integer qyNxDisCorpUserId){
		return qyNxDisCorpUserDao.queryObject(qyNxDisCorpUserId);
	}
	
	@Override
	public List<QyNxDisCorpUserEntity> queryList(Map<String, Object> map){
		return qyNxDisCorpUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return qyNxDisCorpUserDao.queryTotal(map);
	}
	
	@Override
	public void save(QyNxDisCorpUserEntity qyNxDisCropUser){
		qyNxDisCorpUserDao.save(qyNxDisCropUser);
	}
	
	@Override
	public void update(QyNxDisCorpUserEntity qyNxDisCropUser){
		qyNxDisCorpUserDao.update(qyNxDisCropUser);
	}
	
	@Override
	public void delete(Integer qyNxDisCorpUserId){
		qyNxDisCorpUserDao.delete(qyNxDisCorpUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] qyNxDisCorpUserIds){
		qyNxDisCorpUserDao.deleteBatch(qyNxDisCorpUserIds);
	}

    @Override
    public QyNxDisCorpUserEntity queryQyUserByUserId(String userId) {

		return qyNxDisCorpUserDao.queryQyUserByUserId(userId);
    }

    @Override
    public List<QyNxDisCorpUserEntity> queryCorpUserListByCorpId(Integer corpid) {

		return qyNxDisCorpUserDao.queryCorpUserListByCorpId(corpid);
    }

}
