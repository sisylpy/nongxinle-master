package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentUserDao;
import com.nongxinle.entity.NxDepartmentUserEntity;
import com.nongxinle.service.NxDepartmentUserService;



@Service("nxDepartmentUserService")
public class NxDepartmentUserServiceImpl implements NxDepartmentUserService {
	@Autowired
	private NxDepartmentUserDao nxDepartmentUserDao;
	
	@Override
	public NxDepartmentUserEntity queryObject(Integer nxDepartmentUserId){
		return nxDepartmentUserDao.queryObject(nxDepartmentUserId);
	}

	@Override
	public void save(NxDepartmentUserEntity nxDepartmentUser){

		nxDepartmentUserDao.save(nxDepartmentUser);
	}
	
	@Override
	public void update(NxDepartmentUserEntity nxDepartmentUser){
		nxDepartmentUserDao.update(nxDepartmentUser);
	}
	
	@Override
	public void delete(Integer nxDepartmentUserId){
		nxDepartmentUserDao.delete(nxDepartmentUserId);
	}

    @Override
    public List<NxDepartmentUserEntity> queryAllUsersByDepId(Integer depId) {
		return nxDepartmentUserDao.queryAllUsersByDepId(depId);
    }

    @Override
    public NxDepartmentUserEntity queryDepUserByOpenId(String openId) {
		return nxDepartmentUserDao.queryDepUserByOpenId(openId);
    }

    @Override
    public List<NxDepartmentUserEntity> queryGroupAdminUserAmount(Integer nxDuDepartmentId) {
		return nxDepartmentUserDao.queryGroupAdminUserAmount(nxDuDepartmentId);
    }

    @Override
    public List<NxDepartmentEntity> queryMultiDepartmentByWxOpenId(String openId) {

		return nxDepartmentUserDao.queryMultiDepartmentByWxOpenId(openId);
    }

    @Override
    public List<NxDepartmentUserEntity> queryDepUsersByDepId(Integer depId) {

		return nxDepartmentUserDao.queryDepUsersByDepId(depId);

    }

    @Override
    public List<NxDepartmentUserEntity> queryAllDepUsers() {
        return nxDepartmentUserDao.queryAllDepUsers();
    }

    @Override
    public List<NxDepartmentUserEntity> queryAllUsersByDepFatherId(Integer depId) {

	    return nxDepartmentUserDao.queryAllUsersByDepFatherId(depId);
    }


//	@Override
//	public List<NxDepartmentUserEntity> queryList(Map<String, Object> map){
//		return nxDepartmentUserDao.queryList(map);
//	}
//
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return nxDepartmentUserDao.queryTotal(map);
//	}
//

//	@Override
//	public void deleteBatch(Integer[] nxDepartmentUserIds){
//		nxDepartmentUserDao.deleteBatch(nxDepartmentUserIds);
//	}


}
