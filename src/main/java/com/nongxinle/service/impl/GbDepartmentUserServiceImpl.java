package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDepartmentUserDao;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDepartmentUserEntity;
import com.nongxinle.service.GbDepartmentService;
import com.nongxinle.service.GbDepartmentUserService;
import com.nongxinle.service.GbDepartmentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("gbDepartmentUserService")
public class GbDepartmentUserServiceImpl implements GbDepartmentUserService {
	@Autowired
	private GbDepartmentUserDao gbDepartmentUserDao;
	
	@Override
	public GbDepartmentUserEntity queryObject(Integer gbDepartmentUserId){
		return gbDepartmentUserDao.queryObject(gbDepartmentUserId);
	}

	@Override
	public void save(GbDepartmentUserEntity gbDepartmentUser){

		gbDepartmentUserDao.save(gbDepartmentUser);
	}
	
	@Override
	public void update(GbDepartmentUserEntity gbDepartmentUser){
		gbDepartmentUserDao.update(gbDepartmentUser);
	}
	
	@Override
	public void delete(Integer gbDepartmentUserId){
		gbDepartmentUserDao.delete(gbDepartmentUserId);
	}

    @Override
    public List<GbDepartmentUserEntity> queryAllUsersByDepId(Integer depId) {
		return gbDepartmentUserDao.queryAllUsersByDepId(depId);
    }

    @Override
    public GbDepartmentUserEntity queryDepUserByOpenId(String openId) {
		return gbDepartmentUserDao.queryDepUserByOpenId(openId);
    }

    @Override
    public List<GbDepartmentUserEntity> queryGroupAdminUserAmount(Integer gbDuDepartmentId) {
		return gbDepartmentUserDao.queryGroupAdminUserAmount(gbDuDepartmentId);
    }

    @Override
    public List<GbDepartmentEntity> queryMultiDepartmentByWxOpenIdGb(String openId) {

		return gbDepartmentUserDao.queryMultiDepartmentByWxOpenIdGb(openId);
    }

    @Override
    public List<GbDepartmentUserEntity> queryDepUsersByDepId(Integer depId) {

		return gbDepartmentUserDao.queryDepUsersByDepId(depId);

    }

    @Override
    public GbDepartmentUserEntity queryGroupDepartmentUserByParams(Map<String, Object> map) {

		return gbDepartmentUserDao.queryGroupDepartmentUserByParams(map);
    }

    @Override
    public List<GbDepartmentUserEntity> queryDepUsersByDepIdAndAdmin(Map<String, Object> map) {

		return gbDepartmentUserDao.queryDepUsersByDepIdAndAdmin(map);
    }

    @Override
    public GbDepartmentUserEntity queryDepUserInfoGb(Integer userId) {

	    return gbDepartmentUserDao.queryDepUserInfoGb(userId);
    }


//	@Override
//	public List<GbDepartmentUserEntity> queryList(Map<String, Object> map){
//		return gbDepartmentUserDao.queryList(map);
//	}
//
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return gbDepartmentUserDao.queryTotal(map);
//	}
//

//	@Override
//	public void deleteBatch(Integer[] gbDepartmentUserIds){
//		gbDepartmentUserDao.deleteBatch(gbDepartmentUserIds);
//	}


}
