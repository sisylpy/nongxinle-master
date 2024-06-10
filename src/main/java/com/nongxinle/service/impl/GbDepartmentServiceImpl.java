package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepartmentDao;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeJicai;


@Service("gbDepartmentService")
public class GbDepartmentServiceImpl implements GbDepartmentService {
	@Autowired
	private GbDepartmentDao gbDepartmentDao;
	@Autowired
	private GbDistributerUserService gbDistributerUserService;
	@Autowired
	private GbDistributerService gbDistributerService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private GbDepartmentUserService gbDepartmentUserService;
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;
	
	@Override
	public GbDepartmentEntity queryObject(Integer gbDepartmentId){
		return gbDepartmentDao.queryObject(gbDepartmentId);
	}
	
	@Override
	public List<GbDepartmentEntity> queryList(Map<String, Object> map){
		return gbDepartmentDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepartmentDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepartmentEntity gbDepartment){
		gbDepartmentDao.save(gbDepartment);
	}
	
	@Override
	public void update(GbDepartmentEntity gbDepartment){
		gbDepartmentDao.update(gbDepartment);
	}
	
	@Override
	public void delete(Integer gbDepartmentId){
		gbDepartmentDao.delete(gbDepartmentId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepartmentIds){
		gbDepartmentDao.deleteBatch(gbDepartmentIds);
	}

    @Override
    public List<GbDepartmentEntity> queryGroupDepsByDisId(Map<String,Object> map) {

		return gbDepartmentDao.queryGroupDepsByDisId(map);
    }

    @Override
    public List<GbDepartmentEntity> queryUnLineDepsByDisId(Integer disId) {

		return  gbDepartmentDao.queryUnLineDepsByDisId(disId);
    }

    @Override
    public Integer saveNewChainDepartmentGb(GbDepartmentEntity gbDepartmentEntity) {

		//1.保存distributer
		GbDistributerEntity distributerEntity = new GbDistributerEntity();
		distributerEntity.setGbDistributerName(gbDepartmentEntity.getGbDepartmentName());
		gbDistributerService.save(distributerEntity);


		//2.保存Depatment
		Integer distributerId = distributerEntity.getGbDistributerId();
		gbDepartmentEntity.setGbDepartmentDisId(distributerId);
		gbDepartmentEntity.setGbDepartmentRouteId(-1);
		gbDepartmentDao.save(gbDepartmentEntity);

		//3，保存Dis用户
		Integer gbDepartmentId = gbDepartmentEntity.getGbDepartmentId();
		GbDistributerUserEntity gbDistributerUserEntity = gbDepartmentEntity.getGbDistributerUserEntity();
		gbDistributerUserEntity.setGbDiuDistributerId(distributerId);
		gbDistributerUserEntity.setGbDiuPrintDeviceId("-1");
		gbDistributerUserEntity.setGbDiuPrintBillDeviceId("-1");
		gbDistributerUserEntity.setGbDiuLoginTimes(0);
		gbDistributerUserService.save(gbDistributerUserEntity);




		if (gbDepartmentEntity.getGbDepartmentEntityList().size() > 0) {
			//3,保存部门
			List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentEntity.getGbDepartmentEntityList();
			for (GbDepartmentEntity subDep : gbDepartmentEntities) {
				subDep.setGbDepartmentFatherId(gbDepartmentId);
				subDep.setGbDepartmentType(gbDepartmentEntity.getGbDepartmentType());
				subDep.setGbDepartmentDisId(gbDepartmentEntity.getGbDepartmentDisId());
				gbDepartmentDao.save(subDep);
				if(subDep.getGbDepartmentEntityList().size() > 0){
					for (GbDepartmentEntity deps : subDep.getGbDepartmentEntityList()) {
						deps.setGbDepartmentFatherId(subDep.getGbDepartmentId());
						deps.setGbDepartmentType(gbDepartmentEntity.getGbDepartmentType());
						deps.setGbDepartmentDisId(gbDepartmentEntity.getGbDepartmentDisId());
						gbDepartmentDao.save(deps);
					}
				}


			}
		}

		return gbDistributerUserEntity.getGbDistributerUserId();

    }

    @Override
    public List<GbDepartmentEntity> querySubDepartments(Integer depFatherId) {

		return gbDepartmentDao.querySubDepartments(depFatherId);
    }

	@Override
	public List<GbDepartmentEntity> queryMultiGroupInfoGb(String openId) {
		//采购员的全部店
		List<GbDepartmentEntity> departmentEntities = gbDepartmentUserService.queryMultiDepartmentByWxOpenIdGb(openId);
		return  departmentEntities;
	}


		@Override
		public Map<String, Object> queryDepAndUserInfoGb(Integer nxDepartmentUserId) {
			//订货群信息
			GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryDepUserInfoGb(nxDepartmentUserId);
			//用户信息
			Integer gbDuDepartmentId = gbDepartmentUserEntity.getGbDuDepartmentId();
			GbDepartmentEntity gbDepartmentEntity = gbDepartmentDao.queryDepInfoGb(gbDuDepartmentId);

			//返回
			Map<String, Object> map = new HashMap<>();
			map.put("userInfo", gbDepartmentUserEntity);
			map.put("depInfo", gbDepartmentEntity);

			return  map;
		}

	@Override
	public GbDepartmentEntity queryDepInfoGb(Integer depId) {
		return gbDepartmentDao.queryDepInfoGb(depId);
	}

	@Override
	public void saveNewDepartmentGb(GbDepartmentEntity department) {


		//0,
		Integer gbDepartmentDisId = department.getGbDepartmentDisId();
		GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDepartmentDisId);
		department.setGbDepartmentSettleFullTime(formatWhatFullTime(0));
		department.setGbDepartmentDepSettleId(-1);
		department.setGbDepartmentSettleDate(formatWhatDay(0));
		department.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
		department.setGbDepartmentSettleMonth(formatWhatMonth(0));
		department.setGbDepartmentSettleYear(formatWhatYear(0));
		department.setGbDepartmentSettleTimes("0");
		department.setGbDepartmentDepSettleId(-1);
		//1 save dep
		gbDepartmentDao.save(department);
		// 2 save subDeps
		List<GbDepartmentEntity> gbDepartmentEntityList = department.getGbDepartmentEntityList();
		if(gbDepartmentEntityList.size() > 0) {
			for (GbDepartmentEntity subDeps : gbDepartmentEntityList) {
				subDeps.setGbDepartmentSettleFullTime(formatFullTime());
				gbDistributerEntity.setGbDistributerSettleDate(formatWhatDay(0));
				subDeps.setGbDepartmentSettleDate(formatWhatDay(0));
				subDeps.setGbDepartmentSettleMonth(formatWhatMonth(0));
				subDeps.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
				subDeps.setGbDepartmentSettleYear(formatWhatYear(0));
				subDeps.setGbDepartmentSettleTimes("0");
				subDeps.setGbDepartmentSubAmount(0);
				subDeps.setGbDepartmentIsGroupDep(0);
				subDeps.setGbDepartmentAttrName(subDeps.getGbDepartmentName());
				subDeps.setGbDepartmentType(department.getGbDepartmentType());
				subDeps.setGbDepartmentDisId(department.getGbDepartmentDisId());
				subDeps.setGbDepartmentFatherId(department.getGbDepartmentId());
				subDeps.setGbDepartmentDepSettleId(-1);
				gbDepartmentDao.save(subDeps);
			}
		}
	}

    @Override
    public List<GbDepartmentEntity> queryApplyOutStockGoodsDeps(Map<String, Object> map) {

		return gbDepartmentDao.queryApplyOutStockGoodsDeps(map);
    }

    @Override
    public List<GbDepartmentEntity> queryWasteDepartment(Integer disId) {

		return gbDepartmentDao.queryWasteDepartment(disId);
    }

    @Override
    public List<GbDepartmentEntity> queryDepWithAdminUserByParams(Map<String, Object> map) {

		return gbDepartmentDao.queryDepWithAdminUserByParams(map);
    }

    @Override
    public List<GbDepartmentEntity> queryDepByDepType(Map<String, Object> mapDis) {

		return gbDepartmentDao.queryDepByDepType(mapDis);

    }

	@Override
	public void saveNewDepartmentGbWithDepGoods(GbDepartmentEntity department, Integer cankaoDepId) {

		//0,
		department.setGbDepartmentSettleFullTime(formatWhatFullTime(0));
		department.setGbDepartmentDepSettleId(-1);
		department.setGbDepartmentSettleDate(formatWhatDay(0));
		department.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
		department.setGbDepartmentSettleMonth(formatWhatMonth(0));
		department.setGbDepartmentSettleYear(formatWhatYear(0));
		department.setGbDepartmentSettleTimes("0");
		department.setGbDepartmentDepSettleId(-1);
		//1 save dep
		gbDepartmentDao.save(department);
		// 2 save subDeps
		List<GbDepartmentEntity> gbDepartmentEntityList = department.getGbDepartmentEntityList();
		if(gbDepartmentEntityList.size() > 0){
			for (GbDepartmentEntity subDeps : gbDepartmentEntityList) {
				subDeps.setGbDepartmentSettleFullTime(formatFullTime());
				subDeps.setGbDepartmentSettleDate(formatWhatDay(0));
				subDeps.setGbDepartmentSettleMonth(formatWhatMonth(0));
				subDeps.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
				subDeps.setGbDepartmentSettleYear(formatWhatYear(0));
				subDeps.setGbDepartmentSettleTimes("0");
				subDeps.setGbDepartmentSubAmount(0);
				subDeps.setGbDepartmentIsGroupDep(0);
				subDeps.setGbDepartmentAttrName(subDeps.getGbDepartmentName());
				subDeps.setGbDepartmentType(department.getGbDepartmentType());
				subDeps.setGbDepartmentDisId(department.getGbDepartmentDisId());
				subDeps.setGbDepartmentFatherId(department.getGbDepartmentId());
				subDeps.setGbDepartmentDepSettleId(-1);
				gbDepartmentDao.save(subDeps);


					Map<String, Object> map = new HashMap<>();
					map.put("depFatherId", cankaoDepId);
				List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);

				if(departmentDisGoodsEntities.size() > 0){
						for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
							//添加部门商品
							GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
							disGoodsEntity.setGbDdgDepGoodsName(departmentDisGoodsEntity.getGbDdgDepGoodsName());
							disGoodsEntity.setGbDdgDisGoodsId(departmentDisGoodsEntity.getGbDdgDisGoodsId());
							disGoodsEntity.setGbDdgDisGoodsFatherId(departmentDisGoodsEntity.getGbDdgDisGoodsFatherId());
							disGoodsEntity.setGbDdgDepGoodsPinyin(departmentDisGoodsEntity.getGbDdgDepGoodsPinyin());
							disGoodsEntity.setGbDdgDepGoodsPy(departmentDisGoodsEntity.getGbDdgDepGoodsPy());
							disGoodsEntity.setGbDdgDepGoodsStandardname(departmentDisGoodsEntity.getGbDdgDepGoodsStandardname());
							disGoodsEntity.setGbDdgDepartmentId(subDeps.getGbDepartmentId());
							disGoodsEntity.setGbDdgDepartmentFatherId(subDeps.getGbDepartmentFatherId());
							disGoodsEntity.setGbDdgGbDepartmentId(departmentDisGoodsEntity.getGbDdgGbDepartmentId());
							disGoodsEntity.setGbDdgGbDisId(departmentDisGoodsEntity.getGbDdgGbDisId());
							disGoodsEntity.setGbDdgGoodsType(departmentDisGoodsEntity.getGbDdgGoodsType());
							disGoodsEntity.setGbDdgStockTotalWeight("0.0");
							disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
							disGoodsEntity.setGbDdgShowStandardId(-1);
							disGoodsEntity.setGbDdgShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
							disGoodsEntity.setGbDdgShowStandardScale("-1");
							disGoodsEntity.setGbDdgShowStandardWeight(null);
							disGoodsEntity.setGbDdgSellingPrice(departmentDisGoodsEntity.getGbDdgSellingPrice());
							gbDepartmentDisGoodsService.save(disGoodsEntity);
						}
					}

			}

		}else{
			Map<String, Object> map = new HashMap<>();
			map.put("depFatherId", cankaoDepId);
			List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);

			if(departmentDisGoodsEntities.size() > 0){
				for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
					//添加部门商品
					GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
					disGoodsEntity.setGbDdgDepGoodsName(departmentDisGoodsEntity.getGbDdgDepGoodsName());
					disGoodsEntity.setGbDdgDisGoodsId(departmentDisGoodsEntity.getGbDdgDisGoodsId());
					disGoodsEntity.setGbDdgDisGoodsFatherId(departmentDisGoodsEntity.getGbDdgDisGoodsFatherId());
					disGoodsEntity.setGbDdgDepGoodsPinyin(departmentDisGoodsEntity.getGbDdgDepGoodsPinyin());
					disGoodsEntity.setGbDdgDepGoodsPy(departmentDisGoodsEntity.getGbDdgDepGoodsPy());
					disGoodsEntity.setGbDdgDepGoodsStandardname(departmentDisGoodsEntity.getGbDdgDepGoodsStandardname());
					disGoodsEntity.setGbDdgDepartmentId(department.getGbDepartmentId());
					disGoodsEntity.setGbDdgDepartmentFatherId(department.getGbDepartmentId());
					disGoodsEntity.setGbDdgGbDepartmentId(departmentDisGoodsEntity.getGbDdgGbDepartmentId());
					disGoodsEntity.setGbDdgGbDisId(departmentDisGoodsEntity.getGbDdgGbDisId());
					disGoodsEntity.setGbDdgGoodsType(departmentDisGoodsEntity.getGbDdgGoodsType());
					disGoodsEntity.setGbDdgStockTotalWeight("0.0");
					disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
					disGoodsEntity.setGbDdgShowStandardId(departmentDisGoodsEntity.getGbDdgShowStandardId());
					disGoodsEntity.setGbDdgShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
					disGoodsEntity.setGbDdgShowStandardScale(departmentDisGoodsEntity.getGbDdgShowStandardScale());
					disGoodsEntity.setGbDdgSellingPrice(departmentDisGoodsEntity.getGbDdgSellingPrice());
					gbDepartmentDisGoodsService.save(disGoodsEntity);
				}
			}

		}
	}

    @Override
    public List<GbDepartmentEntity> queryGroupDepsByDisIdWithUnPayBill(Map<String, Object> map) {

		return gbDepartmentDao.queryGroupDepsByDisIdWithUnPayBill(map);
    }

}
