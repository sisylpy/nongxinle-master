package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;


@RestController
@RequestMapping("api/gbdepartment")
public class GbDepartmentController {
	@Autowired
	private GbDepartmentService gbDepartmentService;
	@Autowired
	private GbDistributerUserService gbDistributerUserService;
	@Autowired
	private GbDepartmentUserService gbDepartmentUserService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;
	@Autowired
	private GbDistributerService gbDistributerService;
	@Autowired
	private NxDistributerGbDistributerService nxDistributerGbDistributerService;
	@Autowired
	private SysBusinessTypeService sysBusinessTypeService ;
	@Autowired
	private GbDepartmentOrdersService gbDepartmentOrdersService;
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;




//	@RequestMapping(value = "/peisongGetNxDistributerCata/{depId}")
//	@ResponseBody
//	public R peisongGetNxDistributerCata (@PathVariable Integer depId) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("depId", depId);
//		List<NxGoodsEntity> nxGoodsEntities =  nxGoodsService.queryCataNxDistribterWithPeisong(map);
//		return R.ok().put("data", nxGoodsEntities);
//	}




	@RequestMapping(value = "/peisongGetYishangCata/{depId}")
	@ResponseBody
	public R peisongGetYishangCata(@PathVariable Integer depId) {

		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		List<SysBusinessTypeEntity> sysBusinessTypeEntities = sysBusinessTypeService.queryTypeNxDistribterWithPeisong(map);
		return R.ok().put("data", sysBusinessTypeEntities);
	}


	@RequestMapping(value = "/peisongDepGetNxDistributer/{depId}")
	@ResponseBody
	public R peisongDepGetNxDistributer(@PathVariable Integer depId) {
		List<NxDistributerEntity> distributerEntities = nxDistributerGbDistributerService.queryGbDistributerNxDistribtuer(depId);
		return R.ok().put("data", distributerEntities);
	}


	@RequestMapping(value = "/peisongDepDeleteNxDistributer", method = RequestMethod.POST)
	@ResponseBody
	public R peisongDepDeleteNxDistributer (Integer depId, Integer nxDisId ) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("nxDisId", nxDisId);
		NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(map);
		if(nxDistributerGbDistributerEntity != null){
			nxDistributerGbDistributerService.delete(nxDistributerGbDistributerEntity.getNxDistributerGbDistributerId());
		}

		return R.ok();
	}
	@RequestMapping(value = "/peisongDepGetNxDistributerGoods", method = RequestMethod.POST)
	@ResponseBody
	public R peisongDepGetNxDistributerGoods(Integer depId, Integer nxGoodsId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("nxGoodsId", nxGoodsId);
		List<NxDistributerEntity> distributerEntities = nxDistributerGbDistributerService.queryGbDistributerNxDistribtuerGoods(map);
		return R.ok().put("data", distributerEntities);

	}




//	@RequestMapping(value = "/getDisStockOrdersGoods", method = RequestMethod.POST)
//	@ResponseBody
//	public R getDisStockOrdersGoods (Integer disId, Integer goodsType, Integer depId) {
//
//		Map<String, Object> map = new HashMap<>();
////		map.put("disId", disId);
////		map.put("goodsType", goodsType);
//		map.put("orderType",getGbOrderTypeChuKu() );
//		map.put("depId", depId);
//		map.put("status", 3);
//		List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryApplyOutStockGoodsDeps(map);
//
//	    return R.ok().put("data", gbDepartmentEntities);
//	}
	@RequestMapping(value = "/getDepInfoGb/{depId}")
	@ResponseBody
	public R getDepInfoGb(@PathVariable Integer depId) {
		System.out.println(depId + "idiid");
		GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryDepInfoGb(depId);
		return R.ok().put("data", gbDepartmentEntity);
	}

	@RequestMapping(value = "/getSubDepartmentsGb/{depId}")
	@ResponseBody
	public R getSubDepartmentsGb(@PathVariable Integer depId) {
		System.out.println(depId);
		List<GbDepartmentEntity> departmentEntities =   gbDepartmentService.querySubDepartments(depId);
		return R.ok().put("data", departmentEntities);
	}

	@RequestMapping(value = "/getDisDepartmentGbMendian/{disId}" )
	@ResponseBody
	public R getDisDepartmentGbMendian(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeMendian());
		List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("depType", getGbDepartmentTypeJiameng());
		List<GbDepartmentEntity> gbDepartmentEntities1 = gbDepartmentService.queryGroupDepsByDisId(map1);

		Map<String, Object> map2 = new HashMap<>();
		map2.put("zhiyingArr", gbDepartmentEntities );
		map2.put("jiamengArr", gbDepartmentEntities1 );
		return R.ok().put("data",  map2);
	}

	@RequestMapping(value = "/getDisDepartmentGbMendianWithBill/{disId}" )
	@ResponseBody
	public R getDisDepartmentGbMendianWithBill(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeMendian());
		System.out.println("havbidididiid" + map);
		List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisIdWithUnPayBill(map);

		map.put("depType", getGbDepartmentTypeJiameng());
		List<GbDepartmentEntity> gbDepartmentEntities1 = gbDepartmentService.queryGroupDepsByDisIdWithUnPayBill(map);

		Map<String, Object> map2 = new HashMap<>();
		map2.put("zhiyingArr", gbDepartmentEntities );
		map2.put("jiamengArr", gbDepartmentEntities1 );
		return R.ok().put("data",  map2);
	}



	@RequestMapping(value = "/getDisDepartmentGb")
	@ResponseBody
	public R getDisDepartmentGb(Integer disId, Integer type) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", type);
		List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
		return R.ok().put("data", gbDepartmentEntities);
	}

	@RequestMapping(value = "/saveGbDepartment", method = RequestMethod.POST)
	@ResponseBody
	public R saveGbDepartment (@RequestBody GbDepartmentEntity department  ) {

		System.out.println("nudlldld" + department.getFatherGoodsIds());
		String gbDepartmentName = department.getGbDepartmentName();
		String headPinyin = getHeadStringByString(gbDepartmentName, false, null);
        department.setGbDepartmentNamePy(headPinyin);
		if(department.getGbDepartmentType().equals(getGbDepartmentTypeMendian()) || department.getGbDepartmentType().equals(getGbDepartmentTypeJiameng())){
			if(department.getCankaoDepId() > 0){
				gbDepartmentService.saveNewDepartmentGbWithDepGoods(department, department.getCankaoDepId());
			}else{
				gbDepartmentService.saveNewDepartmentGb(department);
			}
		}else{
			gbDepartmentService.saveNewDepartmentGb(department);
		}

		GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(department.getGbDepartmentDisId());
		return R.ok().put("data", gbDistributerEntity);
	}

	/**
	 * PURCHASE
	 * 采购员注册
	 * @return 群信息
	 */
	@RequestMapping(value = "/depManRegisterNewChainDepartmentGb", method = RequestMethod.POST)
	@ResponseBody
	public R depManRegisterNewChainDepartmentGb(@RequestBody GbDepartmentEntity gbDepartmentEntity) {
//wxApp
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		GbDistributerUserEntity distributerUserEntity = gbDepartmentEntity.getGbDistributerUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() +
				"&secret=" + myAPPIDConfig.getTexiansongScreat() + "&js_code=" + distributerUserEntity.getGbDiuCode() + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryDisUserByOpenIdGb(openid);

		if (gbDistributerUserEntity == null) {
			gbDepartmentEntity.getGbDistributerUserEntity().setGbDiuWxOpenId(openid);
			Integer resUserId = gbDepartmentService.saveNewChainDepartmentGb(gbDepartmentEntity);

			if (resUserId != null) {
				Map<String, Object> stringObjectMap = gbDistributerUserService.queryDisAndUserInfo(resUserId);
				return R.ok().put("data", stringObjectMap);
			}
			return R.error(-1, "注册失败");
		} else {
			return R.error(-1, "此微信号已注册过采购员");
		}
	}




	@RequestMapping(value = "/queryUnLineStore/{disId}")
	@ResponseBody
	public R queryUnLineStore(@PathVariable Integer disId) {
		List<GbDepartmentEntity> unLineDeps = gbDepartmentService.queryUnLineDepsByDisId(disId);
		return R.ok().put("data",unLineDeps);
	}

	@RequestMapping(value = "/getGroupJicai/{disId}")
	@ResponseBody
	public R getGroupJicai(@PathVariable Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeJicai());
		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
		return R.ok().put("data", departmentEntities);
	}

	@RequestMapping(value = "/getGbDisStockDepartment/{disId}")
	@ResponseBody
	public R getGbDisStockDepartment(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeKufang());
		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("depType", getGbDepartmentTypeMendian());
		List<GbDepartmentEntity> departmentEntities1 = gbDepartmentService.queryGroupDepsByDisId(map1);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("disId", disId);
		map2.put("depType", getGbDepartmentTypeJiameng());
		List<GbDepartmentEntity> departmentEntities2 = gbDepartmentService.queryGroupDepsByDisId(map2);
		Map<String, Object> map3 = new HashMap<>();
		map3.put("stock", departmentEntities);
		map3.put("mendian",departmentEntities1 );
		map3.put("jiameng",departmentEntities2 );
		return R.ok().put("data", map3);
	}
	@RequestMapping(value = "/getGbDisTypeDepartment/{disId}")
	@ResponseBody
	public R getGbDisTypeDepartment(@PathVariable Integer disId) {
		Map<String, Object> map0 = new HashMap<>();
		map0.put("disId", disId);
		map0.put("depType", getGbDepartmentTypeKufang());
		List<GbDepartmentEntity> departmentEntitiesK = gbDepartmentService.queryGroupDepsByDisId(map0);


		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeJicai());
		List<GbDepartmentEntity> departmentEntitiesJ = gbDepartmentService.queryGroupDepsByDisId(map);

		Map<String, Object> map1 = new HashMap<>();
		map1.put("disId", disId);
		map1.put("depType", getGbDepartmentTypeMendian());
		List<GbDepartmentEntity> departmentEntitiesM = gbDepartmentService.queryGroupDepsByDisId(map1);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("jicai", departmentEntitiesJ);
		map2.put("stock", departmentEntitiesK);
		map2.put("mendian",departmentEntitiesM );
		return R.ok().put("data", map2);
	}



	@RequestMapping(value = "/getGroupStockRooms/{disId}")
	@ResponseBody
	public R getGroupStockRooms(@PathVariable Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", 2);
		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
		return R.ok().put("data", departmentEntities);
	}
	@RequestMapping(value = "/getGroupTypeDeps")
	@ResponseBody
	public R getGroupTypeDeps( Integer disId, Integer type) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", type);
	    List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
	    return R.ok().put("data", departmentEntities);
	}
	@RequestMapping(value = "/getGroupDeps/{disId}")
	@ResponseBody
	public R getGroupDeps(@PathVariable Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", 1);
		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);
		return R.ok().put("data", departmentEntities);
	}


	@RequestMapping(value = "/updateGroupNameGb", method = RequestMethod.POST)
	@ResponseBody
	public R updateGroupNameGb (@RequestBody  GbDepartmentEntity departmentEntity ) {
		gbDepartmentService.update(departmentEntity);
		GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(departmentEntity.getGbDepartmentDisId());
		return R.ok().put("data", gbDistributerEntity);
	}


	@RequestMapping(value = "/deleteDepartmentSetGoods/{depId}")
	@ResponseBody
	public R deleteDepartmentSetGoods(@PathVariable Integer depId) {
		//1,yonghu
		List<GbDepartmentUserEntity> gbDepartmentUserEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
		//2,
		Map<String, Object> map = new HashMap<>();
		map.put("toDepId", depId);
		List<GbDistributerGoodsEntity> gbDisGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(map);

		if(gbDepartmentUserEntities.size() > 0){
			return R.error(-1, "已经有用户，不能删除。");
		}else if(gbDisGoodsEntities.size() > 0){
			return R.error(-1, "有商品设置采购部门，不能删除。");
		}else{
			List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
			if(departmentEntities.size() > 0){
				for (GbDepartmentEntity dep : departmentEntities) {
					gbDepartmentService.delete(dep.getGbDepartmentId());
				}
			}
			GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
			Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
			GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);

			gbDepartmentService.delete(depId);

			return R.ok().put("data",gbDistributerEntity);
		}
	}
	@RequestMapping(value = "/deleteDepartment/{depId}")
	@ResponseBody
	public R deleteDepartment(@PathVariable Integer depId) {
		List<GbDepartmentUserEntity> gbDepartmentUserEntities = gbDepartmentUserService.queryAllUsersByDepId(depId);
		Map<String, Object> map = new HashMap<>();
		map.put("depFatherId", depId);
		List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
		List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);


		if(gbDepartmentUserEntities.size() > 0 || ordersEntities.size() > 0 || departmentDisGoodsEntities.size() > 0){
			return R.error(-1, "有部门相关数据，暂无法删除。");
		}else{
			List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
			if(departmentEntities.size() > 0){
				for (GbDepartmentEntity dep : departmentEntities) {
					gbDepartmentService.delete(dep.getGbDepartmentId());
				}
			}

			GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
			Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
			GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDepartmentDisId);

			gbDepartmentService.delete(depId);

			return R.ok().put("data",gbDistributerEntity);
		}
	}



	

	
}
