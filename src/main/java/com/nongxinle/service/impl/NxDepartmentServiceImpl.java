package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentDao;

import static com.nongxinle.utils.DateUtils.formatWhatDate;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@Service("nxDepartmentService")
public class NxDepartmentServiceImpl implements NxDepartmentService {
	@Autowired
	private NxDepartmentDao nxDepartmentDao;
	@Autowired
	private NxDepartmentUserService nxDepartmentUserService;
	@Autowired
	private NxDistributerDepartmentService nxDistributerDepartmentService;
	@Autowired
	private NxDistributerService nxDistributerService;



	@Override
	public List<NxDepartmentEntity> queryMultiGroupInfo(String openId) {
		//采购员的全部店
		List<NxDepartmentEntity> departmentEntities = nxDepartmentUserService.queryMultiDepartmentByWxOpenId(openId);
		return  departmentEntities;
	}

	@Override
	public Map<String, Object> queryDepAndUserInfo(Integer nxDepartmentUserId) {
		//订货群信息
		Map<String, Object> mapU = new HashMap<>();
		mapU.put("userId", nxDepartmentUserId);
		NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryDepUserInfo(mapU);
		//用户信息
		Integer nxDuDepartmentId = nxDepartmentUserEntity.getNxDuDepartmentId();
		NxDepartmentEntity nxDepartmentEntity = nxDepartmentDao.queryUserDepInfo(nxDuDepartmentId);
		//返回
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", nxDepartmentUserEntity);
		map.put("depInfo", nxDepartmentEntity);
		return  map;
	}


	@Override
	public Integer saveNewDepartment(NxDepartmentEntity dep) {

		//1.保存餐馆

		dep.setNxDepartmentOrderTotal(0);
		dep.setNxDepartmentJoinDate(formatWhatDay(0));
		String s = hanziToPinyin(dep.getNxDepartmentName());
		dep.setNxDepartmentPinyin(s);
		Integer nxDepartmentDisId = dep.getNxDepartmentDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
		dep.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());

		nxDepartmentDao.save(dep);

//		//2，保存用户
		Integer nxDepartmentId = dep.getNxDepartmentId();
		NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
		nxDepartmentUserEntity.setNxDuDepartmentId(nxDepartmentId);
		nxDepartmentUserEntity.setNxDuDepartmentFatherId(nxDepartmentId);
		nxDepartmentUserEntity.setNxDuJoinDate(formatWhatDay(0));
		nxDepartmentUserEntity.setNxDuLoginTimes(0);
		nxDepartmentUserService.save(nxDepartmentUserEntity);

		if(dep.getNxDepartmentEntities().size() > 0){
			//3,保存部门
			List<NxDepartmentEntity> nxDepartmentEntities = dep.getNxDepartmentEntities();
			for (NxDepartmentEntity subDep : nxDepartmentEntities) {
				subDep.setNxDepartmentFatherId(nxDepartmentId);
				subDep.setNxDepartmentDisId(dep.getNxDepartmentDisId());
				subDep.setNxDepartmentAttrName(subDep.getNxDepartmentName());
				subDep.setNxDepartmentOrderCode(subDep.getNxDepartmentName());
				subDep.setNxDepartmentRecordMinutes(30);
				subDep.setNxDepartmentDisId(dep.getNxDepartmentDisId());
				subDep.setNxDepartmentOrderTotal(0);
				subDep.setNxDepartmentJoinDate(formatWhatDay(0));
				String ss = hanziToPinyin(subDep.getNxDepartmentName());
				subDep.setNxDepartmentPinyin(ss);
				nxDepartmentDao.save(subDep);
			}
		}

//		//3, 保存订货群的批发商
//		Integer nxDepartmentDisId = dep.getNxDepartmentDisId();
//		NxDistributerDepartmentEntity entity = new NxDistributerDepartmentEntity();
//		entity.setNxDdDistributerId(nxDepartmentDisId);
//		entity.setNxDdDepartmentId(nxDepartmentId);
//		nxDistributerDepartmentService.save(entity);

		return nxDepartmentUserEntity.getNxDepartmentUserId();
	}


	@Override
	public NxDepartmentEntity saveJustDepartment(NxDepartmentEntity nxDepartmentEntity) {
		nxDepartmentEntity.setNxDepartmentJoinDate(formatWhatDay(0));
		nxDepartmentEntity.setNxDepartmentOrderTotal(0);
		String s = hanziToPinyin(nxDepartmentEntity.getNxDepartmentName());
		nxDepartmentEntity.setNxDepartmentPinyin(s);
//		nxDepartmentEntity.setNxDepartmentAttrName(nxDepartmentEntity.getNxDepartmentName());
//		nxDepartmentEntity.setNxDepartmentOrderCode(nxDepartmentEntity.getNxDepartmentName());
		nxDepartmentEntity.setNxDepartmentRecordMinutes(30);
		Integer nxDepartmentDisId = nxDepartmentEntity.getNxDepartmentDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
        nxDepartmentEntity.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());
		nxDepartmentDao.save(nxDepartmentEntity);
		return nxDepartmentEntity;
	}

	@Override
	public List<NxDepartmentEntity> querySubDepartments(Integer depId) {
		return nxDepartmentDao.querySubDepartments(depId);
	}


	@Override
	public NxDepartmentEntity queryObject(Integer nxDepartmentId){
		return nxDepartmentDao.queryObject(nxDepartmentId);
	}

	@Override
	public void save(NxDepartmentEntity nxDepartment){
		nxDepartment.setNxDepartmentOrderTotal(0);
		nxDepartment.setNxDepartmentJoinDate(formatWhatDay(0));
		String s = hanziToPinyin(nxDepartment.getNxDepartmentName());
		nxDepartment.setNxDepartmentPinyin(s);
		Integer nxDepartmentDisId = nxDepartment.getNxDepartmentDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
		nxDepartment.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());
		nxDepartmentDao.save(nxDepartment);
		Integer nxDepartmentId = nxDepartment.getNxDepartmentId();
		Integer nxDepUserId = nxDepartment.getNxDepUserId();
		NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(nxDepUserId);
		nxDepartmentUserEntity.setNxDuDepartmentId(nxDepartmentId);
		nxDepartmentUserService.update(nxDepartmentUserEntity);

	}

	@Override
	public void update(NxDepartmentEntity nxDepartment){
		nxDepartmentDao.update(nxDepartment);
	}

	@Override
	public Integer saveNewChainDepartment(NxDepartmentEntity dep) {
//1.保存餐馆

		dep.setNxDepartmentOrderTotal(0);
		dep.setNxDepartmentJoinDate(formatWhatDay(0));
		String s = hanziToPinyin(dep.getNxDepartmentName());
		dep.setNxDepartmentPinyin(s);
		Integer nxDepartmentDisId = dep.getNxDepartmentDisId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
		dep.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());
		nxDepartmentDao.save(dep);

//		//2，保存用户
		Integer nxDepartmentId = dep.getNxDepartmentId();
		NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
		nxDepartmentUserEntity.setNxDuDepartmentId(nxDepartmentId);
		nxDepartmentUserEntity.setNxDuDepartmentFatherId(nxDepartmentId);
		nxDepartmentUserEntity.setNxDuJoinDate(formatWhatDay(0));
		nxDepartmentUserEntity.setNxDuLoginTimes(0);
		nxDepartmentUserService.save(nxDepartmentUserEntity);

		if(dep.getNxDepartmentEntities().size() > 0){
			//3,保存部门
			List<NxDepartmentEntity> nxDepartmentEntities = dep.getNxDepartmentEntities();
			for (NxDepartmentEntity subDep : nxDepartmentEntities) {
				subDep.setNxDepartmentFatherId(nxDepartmentId);
				subDep.setNxDepartmentSettleType(dep.getNxDepartmentSettleType());
				subDep.setNxDepartmentDisId(dep.getNxDepartmentDisId());
				subDep.setNxDepartmentWorkingStatus(0);
				subDep.setNxDepartmentType("分店");
				subDep.setNxDepartmentAttrName(subDep.getNxDepartmentName());
				subDep.setNxDepartmentOrderCode(subDep.getNxDepartmentName());
				subDep.setNxDepartmentRecordMinutes(30);
				subDep.setNxDepartmentOrderTotal(0);
				subDep.setNxDepartmentJoinDate(formatWhatDay(0));
				String ss = hanziToPinyin(subDep.getNxDepartmentName());
				subDep.setNxDepartmentPinyin(ss);
				subDep.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());
				nxDepartmentDao.save(subDep);
				if(subDep.getNxDepartmentEntities().size() > 0){
					for (NxDepartmentEntity grandSubDep: subDep.getNxDepartmentEntities()) {
						grandSubDep.setNxDepartmentFatherId(subDep.getNxDepartmentId());
						grandSubDep.setNxDepartmentType("分店部门");
						grandSubDep.setNxDepartmentSettleType(dep.getNxDepartmentSettleType());
						grandSubDep.setNxDepartmentDisId(subDep.getNxDepartmentDisId());
						grandSubDep.setNxDepartmentWorkingStatus(0);
						grandSubDep.setNxDepartmentOrderTotal(0);
						grandSubDep.setNxDepartmentJoinDate(formatWhatDay(0));
						String sdd = hanziToPinyin(grandSubDep.getNxDepartmentName());
						grandSubDep.setNxDepartmentPinyin(sdd);
						grandSubDep.setNxDepartmentAppId(nxDistributerEntity.getNxDistributerAppId());
						nxDepartmentDao.save(grandSubDep);
					}
				}
			}
		}

		//3, 保存订货群的批发商
//		Integer nxDepartmentDisId = dep.getNxDepartmentDisId();
		NxDistributerDepartmentEntity entity = new NxDistributerDepartmentEntity();
		entity.setNxDdDistributerId(nxDepartmentDisId);
		entity.setNxDdDepartmentId(nxDepartmentId);
		nxDistributerDepartmentService.save(entity);

		return nxDepartmentUserEntity.getNxDepartmentUserId();	}

    @Override
    public NxDepartmentEntity queryDepInfo(Integer depId) {

		return nxDepartmentDao.queryDepInfo(depId);
    }

	@Override
	public NxDepartmentEntity queryGroupInfo(Integer depId) {
		return nxDepartmentDao.queryGroupInfo(depId);
	}


//
//	@Override
//	public List<NxDepartmentEntity> queryList(Map<String, Object> map){
//		return nxDepartmentDao.queryList(map);
//	}
//
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return nxDepartmentDao.queryTotal(map);
//	}




	@Override
	public void delete(Integer nxDepartmentId){
		nxDepartmentDao.delete(nxDepartmentId);
	}

	@Override
	public void deleteBatch(Integer[] nxDepartmentIds){
		nxDepartmentDao.deleteBatch(nxDepartmentIds);
	}

    @Override
    public List<NxDepartmentEntity> queryDeliveryDepsByParams(Integer userId) {

		return nxDepartmentDao.queryDeliveryDepsByParams(userId);
    }

    @Override
    public List<NxDepartmentEntity> queryDepartmentBySettleType(Map<String, Object> map) {

		return nxDepartmentDao.queryDepartmentBySettleType(map);
    }

    @Override
    public int queryDepartmentCount(Map<String, Object> map) {

		return  nxDepartmentDao.queryDepartmentCount(map);
    }

    @Override
    public List<NxDepartmentEntity> queryDepartmentListByParams(Map<String, Object> map) {

		return nxDepartmentDao.queryDepartmentListByParams(map);
    }

    @Override
    public NxDepartmentEntity queryDepInfoAll(Integer nxDepartmentId) {

		return nxDepartmentDao.queryDepInfoAll(nxDepartmentId);
    }

    @Override
    public Map<String, Object> queryDepAndUserInfoAll(Integer nxDepartmentUserId) {

		//订货群信息
		Map<String, Object> mapU = new HashMap<>();
		mapU.put("userId", nxDepartmentUserId);
		NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryDepUserInfo(mapU);
		//用户信息
		Integer nxDuDepartmentId = nxDepartmentUserEntity.getNxDuDepartmentId();
		System.out.println("getdidididndiallll");
		NxDepartmentEntity nxDepartmentEntity = nxDepartmentDao.queryDepInfoAll(nxDuDepartmentId);
		System.out.println("depnanaamemmemememe" + nxDepartmentEntity.getNxDepartmentName());
		//返回
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", nxDepartmentUserEntity);
		map.put("depInfo", nxDepartmentEntity);
		return  map;
    }

    @Override
    public NxDepartmentEntity queryDepInfoByGbDisId(Integer disId) {

		return nxDepartmentDao.queryDepInfoByGbDisId(disId);
    }


}
